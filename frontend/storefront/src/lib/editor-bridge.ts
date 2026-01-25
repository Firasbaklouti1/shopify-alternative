/**
 * Editor Bridge - postMessage communication with the visual editor
 * Handles real-time layout updates from the admin editor iframe
 */

export type EditorMessageType =
  | 'UPDATE_LAYOUT'
  | 'SELECT_SECTION'
  | 'DESELECT_SECTION'
  | 'PREVIEW_MODE_ON'
  | 'PREVIEW_MODE_OFF';

export type StorefrontMessageType =
  | 'SECTION_CLICKED'
  | 'LAYOUT_LOADED'
  | 'READY';

export interface EditorMessage {
  type: EditorMessageType;
  payload?: unknown;
  sectionId?: string;
}

export interface StorefrontMessage {
  type: StorefrontMessageType;
  sectionId?: string;
  layout?: unknown;
}

type LayoutUpdateHandler = (layout: unknown) => void;
type SectionSelectHandler = (sectionId: string | null) => void;
type PreviewModeHandler = (enabled: boolean) => void;

interface EditorBridgeOptions {
  onLayoutUpdate?: LayoutUpdateHandler;
  onSectionSelect?: SectionSelectHandler;
  onPreviewModeChange?: PreviewModeHandler;
  allowedOrigins?: string[];
}

class EditorBridge {
  private onLayoutUpdate?: LayoutUpdateHandler;
  private onSectionSelect?: SectionSelectHandler;
  private onPreviewModeChange?: PreviewModeHandler;
  private allowedOrigins: string[];
  private isInitialized = false;

  constructor(options: EditorBridgeOptions = {}) {
    this.onLayoutUpdate = options.onLayoutUpdate;
    this.onSectionSelect = options.onSectionSelect;
    this.onPreviewModeChange = options.onPreviewModeChange;
    this.allowedOrigins = options.allowedOrigins || [
      'http://localhost:3001', // Admin dev server
      'http://localhost:8080', // Spring Boot admin
      window.location.origin,
    ];
  }

  /**
   * Initialize the editor bridge listener
   */
  init(): void {
    if (this.isInitialized || typeof window === 'undefined') return;

    window.addEventListener('message', this.handleMessage.bind(this));
    this.isInitialized = true;

    // Notify parent that storefront is ready
    this.sendToEditor({ type: 'READY' });
  }

  /**
   * Cleanup the listener
   */
  destroy(): void {
    if (typeof window === 'undefined') return;
    window.removeEventListener('message', this.handleMessage.bind(this));
    this.isInitialized = false;
  }

  /**
   * Handle incoming messages from the editor
   */
  private handleMessage(event: MessageEvent): void {
    // Security: Verify origin
    if (!this.allowedOrigins.includes(event.origin)) {
      console.warn('Rejected message from untrusted origin:', event.origin);
      return;
    }

    const message = event.data as EditorMessage;

    if (!message || !message.type) return;

    switch (message.type) {
      case 'UPDATE_LAYOUT':
        if (this.onLayoutUpdate && message.payload) {
          this.onLayoutUpdate(message.payload);
        }
        break;

      case 'SELECT_SECTION':
        if (this.onSectionSelect && message.sectionId) {
          this.onSectionSelect(message.sectionId);
        }
        break;

      case 'DESELECT_SECTION':
        if (this.onSectionSelect) {
          this.onSectionSelect(null);
        }
        break;

      case 'PREVIEW_MODE_ON':
        if (this.onPreviewModeChange) {
          this.onPreviewModeChange(true);
        }
        break;

      case 'PREVIEW_MODE_OFF':
        if (this.onPreviewModeChange) {
          this.onPreviewModeChange(false);
        }
        break;
    }
  }

  /**
   * Send message to the editor (parent window)
   */
  sendToEditor(message: StorefrontMessage): void {
    if (typeof window === 'undefined' || !window.parent) return;

    // Send to all allowed origins
    this.allowedOrigins.forEach((origin) => {
      try {
        window.parent.postMessage(message, origin);
      } catch {
        // Silently fail for mismatched origins
      }
    });
  }

  /**
   * Notify editor that a section was clicked
   */
  notifySectionClicked(sectionId: string): void {
    this.sendToEditor({
      type: 'SECTION_CLICKED',
      sectionId,
    });
  }

  /**
   * Notify editor that layout has loaded
   */
  notifyLayoutLoaded(layout: unknown): void {
    this.sendToEditor({
      type: 'LAYOUT_LOADED',
      layout,
    });
  }

  /**
   * Update handlers after initialization
   */
  setHandlers(options: Partial<EditorBridgeOptions>): void {
    if (options.onLayoutUpdate) this.onLayoutUpdate = options.onLayoutUpdate;
    if (options.onSectionSelect) this.onSectionSelect = options.onSectionSelect;
    if (options.onPreviewModeChange) this.onPreviewModeChange = options.onPreviewModeChange;
  }
}

// Singleton instance
let bridgeInstance: EditorBridge | null = null;

export function getEditorBridge(options?: EditorBridgeOptions): EditorBridge {
  if (!bridgeInstance) {
    bridgeInstance = new EditorBridge(options);
  } else if (options) {
    bridgeInstance.setHandlers(options);
  }
  return bridgeInstance;
}

export function isInEditorMode(): boolean {
  if (typeof window === 'undefined') return false;
  // Check if we're in an iframe (editor preview)
  return window.self !== window.top;
}
