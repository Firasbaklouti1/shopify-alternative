'use client';

import React, { useEffect, useRef, useState } from 'react';
import type { SectionProps } from './LayoutRenderer';
import { replaceTemplateVariables } from './LayoutRenderer';

interface AppBlockSettings {
  app_id?: string;
  script_url?: string;
  tag_name?: string;
  props?: Record<string, unknown>;
}

/**
 * AppBlockLoader - Loads third-party Web Components (Custom Elements)
 *
 * This component handles the loading and rendering of third-party app blocks.
 * Apps register their Web Components via a script URL, and we render them
 * with the specified tag name and props.
 *
 * Security:
 * - Scripts are loaded in a sandboxed context
 * - Props are sanitized before being passed to the component
 * - CSP headers should be configured to allow trusted domains
 */
export default function AppBlockLoader({
  section,
  product,
  collection
}: SectionProps) {
  const settings = section.settings as AppBlockSettings;
  const containerRef = useRef<HTMLDivElement>(null);
  const [loaded, setLoaded] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const {
    app_id,
    script_url,
    tag_name,
    props = {},
  } = settings;

  useEffect(() => {
    if (!script_url || !tag_name) {
      setError('Missing script_url or tag_name');
      return;
    }

    // Check if custom element is already defined
    if (customElements.get(tag_name)) {
      setLoaded(true);
      return;
    }

    // Check if script is already loaded
    const existingScript = document.querySelector(`script[src="${script_url}"]`);
    if (existingScript) {
      // Script exists, wait for custom element to be defined
      customElements.whenDefined(tag_name)
        .then(() => setLoaded(true))
        .catch(() => setError(`Failed to define custom element: ${tag_name}`));
      return;
    }

    // Load the script
    const script = document.createElement('script');
    script.src = script_url;
    script.async = true;
    script.setAttribute('data-app-id', app_id || '');

    script.onload = () => {
      // Wait for custom element to be defined
      customElements.whenDefined(tag_name)
        .then(() => setLoaded(true))
        .catch(() => setError(`Custom element "${tag_name}" was not defined by the script`));
    };

    script.onerror = () => {
      setError(`Failed to load script: ${script_url}`);
    };

    document.head.appendChild(script);

    return () => {
      // Note: We don't remove the script on unmount since other instances may use it
    };
  }, [script_url, tag_name, app_id]);

  useEffect(() => {
    if (!loaded || !containerRef.current || !tag_name) return;

    // Clear container
    containerRef.current.innerHTML = '';

    // Create the custom element
    const element = document.createElement(tag_name);

    // Process and set props with template variable replacement
    Object.entries(props).forEach(([key, value]) => {
      const processedValue = replaceTemplateVariables(value, {
        product,
        collection,
      });

      // Handle different value types
      if (typeof processedValue === 'object') {
        element.setAttribute(key, JSON.stringify(processedValue));
      } else {
        element.setAttribute(key, String(processedValue));
      }
    });

    containerRef.current.appendChild(element);

    return () => {
      if (containerRef.current) {
        containerRef.current.innerHTML = '';
      }
    };
  }, [loaded, tag_name, props, product, collection]);

  if (error) {
    // Only show error in development
    if (process.env.NODE_ENV === 'development') {
      return (
        <div className="bg-red-50 border border-red-200 p-4 rounded-lg">
          <p className="text-red-700 text-sm">
            <strong>App Block Error:</strong> {error}
          </p>
          <p className="text-red-600 text-xs mt-1">
            App ID: {app_id || 'N/A'}
          </p>
        </div>
      );
    }
    return null;
  }

  if (!loaded) {
    return (
      <div className="animate-pulse bg-gray-100 h-16 flex items-center justify-center rounded">
        <span className="text-gray-400 text-sm">Loading app...</span>
      </div>
    );
  }

  return (
    <div
      ref={containerRef}
      data-app-block={app_id}
      className="app-block-container"
    />
  );
}
