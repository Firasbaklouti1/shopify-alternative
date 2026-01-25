# Plan: JSON-Driven Storefront with Headless Architecture

Build a **Shopify 2.0-equivalent** storefront system using Spring Boot as a headless API, Next.js as the edge renderer, and an iframe-based visual editor with Web Components for app integrations. AI generation support will be designed in but implemented later.

## TL;DR

Your Spring Boot backend becomes a **pure Headless Storefront API** serving JSON layout configurations. A separate **Next.js App Router** application renders these layouts using React Server Components. The visual editor uses **iframe + postMessage** for real-time preview. Third-party apps integrate via **Web Components** for safe, isolated UI injection. Merchants can configure `checkoutMode` (guest/account/both) in store settings.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                         MERCHANT ADMIN                               │
│  ┌─────────────────────┐    postMessage    ┌─────────────────────┐  │
│  │   Editor Sidebar    │◄──────────────────►│   Storefront        │  │
│  │   (React App)       │                    │   (iframe preview)  │  │
│  └─────────────────────┘                    └─────────────────────┘  │
│            │                                          │              │
│            │ Save Layout                              │              │
└────────────┼──────────────────────────────────────────┼──────────────┘
             │                                          │
             ▼                                          ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT (Headless API)                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────┐  │
│  │ Storefront API  │  │ Layout API      │  │ Existing APIs       │  │
│  │ /api/storefront │  │ /api/layouts    │  │ (Products, Orders)  │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
             │                                          │
             ▼                                          ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    NEXT.JS STOREFRONT (Edge)                         │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────┐  │
│  │ Layout Renderer │  │ Section         │  │ App Block Loader    │  │
│  │ (RSC)           │  │ Components      │  │ (Web Components)    │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Steps

### Backend (Spring Boot)

1. **Create Storefront module** (`src/main/java/com/firas/saas/storefront/`): New module with entities `StoreSettings`, `PageLayout`, `Theme`. The `PageLayout` entity stores JSON configuration in a `TEXT/JSON` column.

2. **Create `StoreSettings` entity**: Extend [Tenant.java](src/main/java/com/firas/saas/tenant/entity/Tenant.java) or create separate entity with: `checkoutMode` (enum: `GUEST_ONLY`, `ACCOUNT_ONLY`, `BOTH`), `themeId`, `globalStyles` (JSON), `favicon`, `socialLinks`, `seoDefaults`.

3. **Create `PageLayout` entity**: Tenant-scoped entity storing JSON layouts per page type (`HOME`, `PRODUCT`, `COLLECTION`, `CART`, `CHECKOUT`). Fields: `tenantId`, `pageType`, `layoutJson` (TEXT), `isPublished`, `version`.

4. **Create `Theme` entity** (global): Platform-provided themes with `name`, `defaultLayoutJson`, `cssVariables`, `previewImage`. Merchants select a theme as starting point.

5. **Build Public Storefront API** (`/api/v1/storefront/{slug}/*`): 
   - `GET /layout?page={type}` → Returns JSON layout configuration
   - `GET /settings` → Returns store settings (logo, colors, checkout mode)
   - `GET /products`, `GET /products/{handle}`, `GET /collections/{handle}` → Product data
   - Update [SecurityConfig.java](src/main/java/com/firas/saas/config/SecurityConfig.java) to permit `/api/v1/storefront/**`

6. **Build Merchant Layout Editor API** (`/api/v1/stores/layouts/*`): Authenticated endpoints for MERCHANT role:
   - `GET /pages` → List all page layouts
   - `GET /pages/{type}` → Get specific page layout JSON
   - `PUT /pages/{type}` → Update page layout (receives full JSON)
   - `POST /pages/{type}/publish` → Publish draft to live

7. **Create Component Schema registry**: Define available section/block types in code or DB — used for validation and future AI generation. Store as `ComponentSchema` entity or static registry class.

### Frontend (Next.js Storefront)

8. **Initialize Next.js App Router project** in [frontend/storefront/](frontend/storefront/): Create Next.js 14+ project with App Router, TypeScript, Tailwind CSS. Dynamic routing: `/store/[slug]/[[...path]]`.

9. **Build Layout Renderer**: Server Component that fetches JSON config from Spring Boot API, maps `type` → React component, renders sections in order. Use Suspense for streaming heavy sections.

10. **Create Section Component Library** (10 core components for Phase 1):
    - `HeroBanner` — Image/video hero with CTA
    - `ProductMain` — Product detail with gallery, price, variant picker, buy button
    - `ProductGrid` — Collection/featured products grid
    - `CollectionList` — List of collections
    - `RichText` — Markdown/HTML content block
    - `ImageWithText` — Side-by-side image and text
    - `Newsletter` — Email signup form
    - `Testimonials` — Customer reviews carousel
    - `AnnouncementBar` — Top banner for promotions
    - `Footer` — Links, social, copyright

11. **Build `AppBlockLoader` component**: Handles `type: "app-block"` sections. Dynamically injects `<script>` tag from `script_url`, renders Web Component with provided `tag_name` and `props`.

12. **Implement Cart & Checkout pages**: Respect `checkoutMode` setting — show guest checkout form, login/register form, or both based on merchant configuration.

### Visual Editor (Admin Dashboard)

13. **Create Editor page in admin dashboard**: React app with sidebar (section list, settings panel) and iframe (storefront preview). Uses `postMessage` for real-time communication.

14. **Implement iframe communication protocol**:
    - Editor sends: `{ type: 'UPDATE_LAYOUT', payload: jsonConfig }`
    - Editor sends: `{ type: 'SELECT_SECTION', sectionId: 'hero-1' }`
    - Storefront receives and hot-swaps layout state without reload
    - Storefront sends: `{ type: 'SECTION_CLICKED', sectionId: 'hero-1' }` for selection sync

15. **Build drag-and-drop section reordering**: Use `dnd-kit` library for dragging sections in sidebar. Updates `order` array in JSON config, sends `UPDATE_LAYOUT` to iframe.

---

## Data Model

```
StoreSettings (extends TenantEntity)
├── tenantId (FK)
├── checkoutMode (GUEST_ONLY | ACCOUNT_ONLY | BOTH)
├── themeId (FK to Theme, nullable)
├── globalStyles (JSON: { primaryColor, secondaryColor, fontFamily, logo, favicon })
├── seoDefaults (JSON: { titleTemplate, defaultDescription, ogImage })
├── socialLinks (JSON: { facebook, instagram, twitter })
└── contactEmail

PageLayout (extends TenantEntity)
├── tenantId (FK)
├── pageType (HOME | PRODUCT | COLLECTION | CART | CHECKOUT | CUSTOM)
├── handle (for CUSTOM pages, e.g., "about-us")
├── layoutJson (TEXT - the full JSON configuration)
├── draftJson (TEXT - unpublished changes)
├── isPublished (boolean)
├── version (int - for optimistic locking)
└── updatedAt

Theme (global, no tenantId)
├── name ("Minimal", "Bold", "Dawn")
├── description
├── defaultLayoutsJson (JSON - default pages config)
├── cssVariables (JSON - theme CSS custom properties)
├── previewImageUrl
└── isActive
```

---

## JSON Layout Schema (Final)

```json
{
  "name": "Home Page",
  "type": "home",
  "sections": {
    "hero-1": {
      "type": "hero-banner",
      "settings": {
        "title": "Welcome to {{store_name}}",
        "subtitle": "Shop our latest collection",
        "bg_image": "/uploads/hero.jpg",
        "cta_text": "Shop Now",
        "cta_link": "/collections/all",
        "overlay_opacity": 0.4
      }
    },
    "featured-products": {
      "type": "product-grid",
      "settings": {
        "title": "Featured Products",
        "collection_handle": "featured",
        "columns": 4,
        "limit": 8,
        "show_price": true,
        "show_vendor": false
      }
    },
    "product-main": {
      "type": "product-main",
      "settings": { "gallery_position": "left" },
      "blocks": {
        "title": { "type": "title", "order": 0 },
        "price": { "type": "price", "order": 1 },
        "variant_picker": { "type": "variant_selector", "order": 2 },
        "buy_buttons": { "type": "buy_buttons", "order": 3 }
      },
      "block_order": ["title", "price", "variant_picker", "buy_buttons"]
    },
    "wishlist-app": {
      "type": "app-block",
      "settings": {
        "app_id": "wishlist-pro",
        "script_url": "https://cdn.wishlistpro.com/widget.js",
        "tag_name": "wishlist-button",
        "props": { "product-id": "{{product.id}}" }
      }
    }
  },
  "order": ["hero-1", "featured-products", "wishlist-app"]
}
```

---

## Project Structure

```
shopify_alternative/
├── src/main/java/com/firas/saas/
│   └── storefront/                    # NEW MODULE
│       ├── controller/
│       │   ├── StorefrontController.java      # Public API (no auth)
│       │   └── LayoutEditorController.java    # Merchant API (auth required)
│       ├── entity/
│       │   ├── StoreSettings.java
│       │   ├── PageLayout.java
│       │   └── Theme.java
│       ├── dto/
│       │   ├── LayoutResponse.java
│       │   ├── StoreSettingsResponse.java
│       │   ├── UpdateLayoutRequest.java
│       │   └── PublicProductResponse.java
│       ├── repository/
│       ├── service/
│       └── schema/
│           └── ComponentRegistry.java         # Available section types
│
├── frontend/
│   ├── storefront/                    # Next.js Storefront App
│   │   ├── app/
│   │   │   ├── store/[slug]/
│   │   │   │   ├── page.tsx           # Home page
│   │   │   │   ├── products/[handle]/page.tsx
│   │   │   │   ├── collections/[handle]/page.tsx
│   │   │   │   ├── cart/page.tsx
│   │   │   │   └── checkout/page.tsx
│   │   │   └── layout.tsx
│   │   ├── components/
│   │   │   ├── sections/              # Section components
│   │   │   │   ├── HeroBanner.tsx
│   │   │   │   ├── ProductGrid.tsx
│   │   │   │   ├── ProductMain.tsx
│   │   │   │   └── ...
│   │   │   ├── blocks/                # Block components
│   │   │   ├── AppBlockLoader.tsx     # Web Component loader
│   │   │   └── LayoutRenderer.tsx     # JSON → Components mapper
│   │   └── lib/
│   │       ├── api.ts                 # Spring Boot API client
│   │       └── editor-bridge.ts       # postMessage handler
│   │
│   └── admin/                         # Admin Dashboard (separate or same app)
│       └── editor/
│           ├── EditorPage.tsx
│           ├── SectionSidebar.tsx
│           ├── SettingsPanel.tsx
│           └── PreviewIframe.tsx
```

---

## Key Decisions Made

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Checkout Mode | Merchant configurable (GUEST_ONLY, ACCOUNT_ONLY, BOTH) | Flexibility for different business models |
| Frontend Framework | Next.js App Router | SSR/ISR for SEO, React Server Components for performance |
| App Integration | Web Components (Custom Elements) | Safe isolation, Shadow DOM, CSP compatible |
| Visual Editor | iframe + postMessage | Real-time preview without page reload, Shopify-standard approach |
| Drag-and-Drop Library | dnd-kit | Lightweight, accessible, full control over UX |
| AI Generation | Designed in, implemented later | Placeholder endpoint ready for LLM integration |

---

## Further Considerations

1. **Storefront deployment strategy?** (A) Single Next.js instance with dynamic `[slug]` routing, (B) Separate Next.js instance per tenant (expensive), (C) Vercel/Edge deployment with ISR. *Recommendation: Option A for MVP, Option C for production scale.*

2. **Custom domain support?** Requires: DNS configuration UI, SSL certificate provisioning (Let's Encrypt), reverse proxy mapping. *Recommendation: Defer to Phase 2, use `{slug}.yourplatform.com` subdomains first.*

3. **AI Generation placeholder?** Add `POST /api/v1/stores/layouts/generate` endpoint now (returns 501 Not Implemented), define request schema (`{ prompt, pageType }`), implement later with LLM integration. *Recommendation: Yes, design the contract now.*

4. **Version control for layouts?** Store layout history for undo/rollback? *Recommendation: Add `PageLayoutVersion` entity for audit trail, limit to last 10 versions per page.*

---

## Implementation Phases

### Phase 1: Backend Foundation ✅ COMPLETED (January 21, 2026)
- [x] Create Storefront module structure
- [x] Implement `StoreSettings`, `PageLayout`, `Theme` entities (with Hibernate 6 JSON mapping)
- [x] Build Public Storefront API endpoints (`/api/v1/storefront/{slug}/*`)
- [x] Build Merchant Layout Editor API endpoints (`/api/v1/stores/*`)
- [x] Update SecurityConfig for public access
- [x] Create Component Schema registry (10 core sections)

### Phase 2: Next.js Storefront ✅ COMPLETED (January 22, 2026)
- [x] Initialize Next.js project with App Router
- [x] Build Layout Renderer (JSON → Components)
- [x] Implement 10 core Section components
- [x] Build Cart & Checkout with checkoutMode support
- [x] Implement AppBlockLoader for Web Components
- [x] Create Store Header with navigation
- [x] Add products listing with pagination & filtering
- [x] Add collections listing page
- [x] Implement editor bridge for postMessage communication

### Phase 3: Visual Editor
- [ ] Create Editor page with iframe preview
- [ ] Implement postMessage communication protocol
- [ ] Build drag-and-drop with dnd-kit
- [ ] Create settings panel for section configuration
- [ ] Add Save/Publish workflow

### Phase 4: Polish & AI (Future)
- [ ] Add PageLayoutVersion for undo/rollback
- [ ] Implement AI generation endpoint
- [ ] Add custom domain support
- [ ] Performance optimization (caching, CDN)
