# Plan: JSON-Driven Storefront with Headless Architecture

Build a **Shopify 2.0-equivalent** storefront system using Spring Boot as a headless API, Next.js as the edge renderer, and **Puck** (`@measured/puck`) as the visual drag-and-drop page builder. AI generation support will be designed in but implemented later.

## TL;DR

Your Spring Boot backend is a **pure Headless Storefront API** serving Puck-format JSON layout configurations. A **Next.js storefront app** (`frontend/storefront/`) renders these layouts using Puck's `<Render>` component. A separate **Next.js admin app** (`frontend/admin/`) provides the Puck visual editor for merchants. Shared section components live in `frontend/shared/`. Third-party apps integrate via **Web Components**. Merchants can configure `checkoutMode` (guest/account/both) in store settings.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                    MERCHANT ADMIN (frontend/admin, port 3001)        │
│  ┌─────────────────────┐                                            │
│  │  Login Page          │  JWT auth via Spring Boot                  │
│  ├─────────────────────┤                                            │
│  │  Dashboard           │  Page type list + custom pages             │
│  ├─────────────────────┤                                            │
│  │  Puck Editor         │  <Puck config={} data={} metadata={} />   │
│  │  - Drag & drop       │  Auto-saves draft (2s debounce)           │
│  │  - Field editing     │  Publish button → copies draft to live    │
│  │  - Viewport preview  │  Mobile / Tablet / Desktop                │
│  └─────────────────────┘                                            │
│            │ Save (PUT)          │ Publish (POST)                    │
└────────────┼─────────────────────┼──────────────────────────────────┘
             ▼                     ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT (Headless API, port 8080)             │
│  ┌─────────────────────┐  ┌─────────────────┐                      │
│  │ Public Storefront    │  │ Layout Editor    │                      │
│  │ /api/v1/storefront   │  │ /api/v1/stores   │                      │
│  │ (No Auth)            │  │ (MERCHANT Role)  │                      │
│  └─────────────────────┘  └─────────────────┘                      │
│  Stores Puck JSON as Map<String,Object> in TEXT columns              │
└─────────────────────────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    STOREFRONT (frontend/storefront, port 3000)        │
│  <Render config={puckConfig} data={layout} metadata={...} />        │
│  Uses same shared components as editor for WYSIWYG consistency       │
└─────────────────────────────────────────────────────────────────────┘

Shared Components (frontend/shared/):
  components/sections/ → 12 Puck-compatible React components
  lib/puck-config.tsx  → Puck Config with fields, categories, render fns
  lib/puck-utils.ts    → Legacy format converter
  lib/api.ts           → API client for storefront endpoints
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

## JSON Layout Schema (Puck Format)

```json
{
  "content": [
    {
      "type": "HeroBanner",
      "props": {
        "id": "HeroBanner-abc123",
        "title": "Welcome to Our Store",
        "subtitle": "Shop our latest collection",
        "bg_image": "/uploads/hero.jpg",
        "cta_text": "Shop Now",
        "cta_link": "/collections/all",
        "overlay_opacity": 0.4,
        "text_color": "light",
        "text_alignment": "center",
        "height": "large"
      }
    },
    {
      "type": "ProductGrid",
      "props": {
        "id": "ProductGrid-def456",
        "title": "Featured Products",
        "collection_handle": "featured",
        "columns": 4,
        "limit": 8,
        "show_price": true,
        "show_vendor": false,
        "image_ratio": "square"
      }
    },
    {
      "type": "AppBlock",
      "props": {
        "id": "AppBlock-ghi789",
        "app_id": "wishlist-pro",
        "script_url": "https://cdn.wishlistpro.com/widget.js",
        "tag_name": "wishlist-button"
      }
    }
  ],
  "root": { "props": { "title": "Home Page" } },
  "zones": {}
}
```

> **Legacy format** (`{sections, order}`) is auto-converted to Puck format by `convertLegacyToPuck()` in `frontend/shared/lib/puck-utils.ts`.

---

## Project Structure

```
shopify_alternative/
├── src/main/java/com/firas/saas/
│   └── storefront/                    # STOREFRONT MODULE
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
│   ├── shared/                        # SHARED COMPONENT LIBRARY
│   │   ├── components/sections/       # 12 Puck-compatible section components
│   │   │   ├── HeroBanner.tsx
│   │   │   ├── ProductGrid.tsx
│   │   │   ├── ProductMain.tsx
│   │   │   ├── CollectionList.tsx
│   │   │   ├── CollectionFilters.tsx
│   │   │   ├── RichText.tsx
│   │   │   ├── ImageWithText.tsx
│   │   │   ├── Newsletter.tsx
│   │   │   ├── Testimonials.tsx
│   │   │   ├── AnnouncementBar.tsx
│   │   │   ├── Footer.tsx
│   │   │   ├── AppBlock.tsx
│   │   │   └── index.ts
│   │   └── lib/
│   │       ├── puck-config.tsx        # Puck Config (fields, categories, render)
│   │       ├── puck-utils.ts          # Legacy → Puck format converter
│   │       ├── api.ts                 # API client for storefront endpoints
│   │       └── template-vars.ts       # Template variable resolution
│   │
│   ├── admin/                         # PUCK EDITOR APP (port 3001)
│   │   └── src/
│   │       ├── app/
│   │       │   ├── page.tsx           # Redirect to login
│   │       │   ├── login/page.tsx     # JWT auth login form
│   │       │   ├── dashboard/page.tsx # Page type list with edit links
│   │       │   └── editor/[slug]/[pageType]/page.tsx  # Puck editor
│   │       └── lib/
│   │           └── auth.tsx           # Auth context provider
│   │
│   └── storefront/                    # PUBLIC STOREFRONT (port 3000)
│       └── src/
│           ├── app/store/[slug]/
│           │   ├── page.tsx           # Home page (PuckRenderer)
│           │   ├── products/[productSlug]/page.tsx
│           │   ├── collections/[collectionSlug]/page.tsx
│           │   ├── pages/[handle]/page.tsx
│           │   ├── cart/page.tsx
│           │   ├── checkout/page.tsx
│           │   └── account/page.tsx
│           ├── components/
│           │   └── PuckRenderer.tsx   # Wrapper around Puck <Render>
│           └── lib/
│               └── api.ts            # Storefront API client (PuckData type)
```

---

## Key Decisions Made

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Checkout Mode | Merchant configurable (GUEST_ONLY, ACCOUNT_ONLY, BOTH) | Flexibility for different business models |
| Frontend Framework | Next.js 16.1.4 App Router | SSR/ISR for SEO, Turbopack for fast dev |
| App Integration | Web Components (Custom Elements) | Safe isolation, Shadow DOM, CSP compatible |
| Visual Editor | **Puck** (`@measured/puck` v0.20.2) | Open-source, React-native drag-and-drop, built-in field editing, viewport preview, undo/redo |
| Component Sharing | `frontend/shared/` with TS path aliases + `transpilePackages` | WYSIWYG consistency between editor and storefront |
| Layout Format | Puck native `{content, root, zones}` | Replaced `{sections, order}`, auto-conversion for legacy data |
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


### Phase 3: Puck Visual Editor Integration ✅ COMPLETED (February 2026)

Replaced the original iframe + postMessage + dnd-kit editor plan with **Puck** (`@measured/puck` v0.20.2), an open-source React drag-and-drop page builder.

**Architecture:**
```
frontend/admin/          ← Puck editor app (merchant-facing, port 3001)
  ├── Login page         ← Auth form → gets JWT token
  ├── Dashboard          ← Page type list with edit links
  └── Editor page        ← <Puck> component with full drag-drop UI
        │
        │  Save (PUT /api/v1/stores/layouts/{pageType})
        │  Publish (POST /api/v1/stores/layouts/{pageType}/publish)
        │  Load (GET /api/v1/stores/layouts/{pageType}/draft)
        ▼
Spring Boot Backend      ← Stores Puck-format JSON (no schema validation)
        │
        │  GET /api/v1/storefront/{slug}/layout?page={type}
        ▼
frontend/storefront/     ← Uses <Render> from @measured/puck (port 3000)
  └── <Render config={puckConfig} data={puckData} metadata={...} />
```

**Puck Data Format (replaces old `{sections, order}`):**
```json
{
  "content": [
    { "type": "HeroBanner", "props": { "id": "HeroBanner-1", "title": "Welcome", ... } },
    { "type": "ProductGrid", "props": { "id": "ProductGrid-1", "title": "Featured", ... } }
  ],
  "root": { "props": { "title": "Home Page" } },
  "zones": {}
}
```

**What was built:**
- [x] Created `frontend/admin/` Next.js app with login, dashboard, and Puck editor pages
- [x] Created `frontend/shared/` with 12 section components, puck-config, api client, and puck-utils
- [x] Refactored all 12 section components from `SectionProps` wrapper to flat Puck props with `puck.metadata`
- [x] Created `PuckRenderer.tsx` in storefront using Puck's `<Render>` component
- [x] Updated all storefront pages (home, product, collection, custom) to use PuckRenderer
- [x] Added legacy `{sections, order}` → Puck `{content, root, zones}` auto-conversion (`puck-utils.ts`)
- [x] Updated all test.http and store-demo.http payloads to Puck format
- [x] Updated backend default layouts to Puck format (`PageLayoutServiceImpl.java`)
- [x] Deleted deprecated files: `LayoutRenderer.tsx`, `editor-bridge.ts`, `AppBlockLoader.tsx`
- [x] Configured monorepo module resolution with `turbopack.root` and `transpilePackages`

**12 Puck Components (shared between editor and storefront):**
| Component | Category | Description |
|-----------|----------|-------------|
| HeroBanner | Hero | Image/video hero with CTA buttons |
| AnnouncementBar | Hero | Top promotional banner |
| ProductGrid | Commerce | Product grid with collection filtering |
| ProductMain | Commerce | Product detail with gallery and variants |
| CollectionList | Commerce | Collection cards grid |
| CollectionFilters | Commerce | Sort and filter controls |
| RichText | Content | Custom text/HTML content |
| ImageWithText | Content | Side-by-side image and text |
| Newsletter | Content | Email signup form |
| Testimonials | Social Proof | Customer reviews grid/carousel |
| Footer | Layout | Links, social, copyright |
| AppBlock | Integrations | Third-party Web Component loader |

### Phase 4: Polish & AI (Future)
- [ ] Add PageLayoutVersion for undo/rollback
- [ ] Implement AI generation endpoint
- [ ] Add custom domain support
- [ ] Performance optimization (caching, CDN)
