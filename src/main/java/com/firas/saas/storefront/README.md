````# Storefront Module

This module implements the **JSON-Driven Storefront** system, enabling merchants to create customizable websites for their stores using the **Puck visual editor** (`@measured/puck` v0.20.2) without writing code.

## Architecture

The storefront follows the **Server-Driven UI** pattern with Puck as the visual editor:

```
┌─────────────────────────────────────────────────────────────────┐
│              MERCHANT ADMIN (frontend/admin, port 3001)          │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  Puck Editor: <Puck config={} data={} metadata={} />   │    │
│  │  - Drag & drop sections from categorized sidebar        │    │
│  │  - Field editing panel for component properties          │    │
│  │  - Viewport preview (Mobile / Tablet / Desktop)          │    │
│  └─────────────────────────────────────────────────────────┘    │
│       │ Save Draft (PUT)          │ Publish (POST)              │
└───────┼───────────────────────────┼─────────────────────────────┘
        ▼                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT API (port 8080)                   │
│  ┌──────────────────────┐     ┌──────────────────────┐          │
│  │ Public Storefront     │     │ Layout Editor API    │          │
│  │ /api/v1/storefront    │     │ /api/v1/stores       │          │
│  │ (No Auth)             │     │ (MERCHANT Role)      │          │
│  └──────────────────────┘     └──────────────────────┘          │
│  Stores Puck JSON as Map<String,Object> in TEXT columns          │
└─────────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────────┐
│              STOREFRONT (frontend/storefront, port 3000)         │
│  <Render config={puckConfig} data={layout} metadata={...} />    │
│  Same shared components as editor → WYSIWYG consistency          │
└─────────────────────────────────────────────────────────────────┘
```

## Key Concepts

### Puck Layout Format

Store layouts are stored as Puck-format JSON (replaced the old `{sections, order}` format in Phase 3):

```json
{
  "content": [
    {
      "type": "HeroBanner",
      "props": {
        "id": "HeroBanner-1",
        "title": "Welcome to Our Store",
        "bg_image": "/uploads/hero.jpg",
        "cta_text": "Shop Now",
        "overlay_opacity": 0.4,
        "height": "large"
      }
    },
    {
      "type": "ProductGrid",
      "props": {
        "id": "ProductGrid-1",
        "title": "Featured Products",
        "limit": 8,
        "columns": 4
      }
    }
  ],
  "root": { "props": { "title": "Home Page" } },
  "zones": {}
}
```

> **Legacy format** (`{sections, order}`) from Phase 2 is auto-converted by the frontend via `convertLegacyToPuck()` in `frontend/shared/lib/puck-utils.ts`.

### Component Registry

Available section types defined in `ComponentRegistry.java` (PascalCase keys for Puck compatibility):
- `HeroBanner` - Large banner with CTA
- `ProductGrid` - Grid of products
- `ProductMain` - Product detail (for product pages)
- `CollectionList` - Grid of categories
- `CollectionFilters` - Sort/filter controls
- `RichText` - Custom text content
- `ImageWithText` - Side-by-side image and text
- `Newsletter` - Email signup
- `Testimonials` - Customer reviews
- `AnnouncementBar` - Top promotional banner
- `Footer` - Site footer
- `AppBlock` - Third-party app components

### Layout Data Flow

```
Editor Save:
  Puck <Puck> → onChange → PUT /api/v1/stores/layouts/{type}
    Request body: { "layoutJson": { content, root, zones }, "name": "..." }
    Backend stores request.getLayoutJson() in layout.draftJson

Editor Publish:
  POST /api/v1/stores/layouts/{type}/publish
    Backend copies draftJson → layoutJson, clears draftJson, sets published=true

Storefront Load:
  GET /api/v1/storefront/{slug}/layout?page={type}
    Backend returns layout.getLayoutJson() directly (raw Puck JSON)
    Frontend passes to <Render config={puckConfig} data={layout} />

Editor Load Draft:
  GET /api/v1/stores/layouts/{type}/draft
    Backend returns draftJson ?? layoutJson directly (raw Puck JSON)
    Editor validates and restores into <Puck data={...} />
```

## Entities

### StoreSettings
Per-tenant store configuration:
- `checkoutMode` - GUEST_ONLY, ACCOUNT_ONLY, or BOTH
- `themeId` - Selected theme
- `globalStyles` - Colors, fonts, logo (JSON)
- `seoDefaults` - Default SEO settings (JSON)
- `socialLinks` - Social media URLs (JSON)
- `published` - Whether store is publicly accessible

### PageLayout
Page-specific layout configuration:
- `pageType` - HOME, PRODUCT, COLLECTION, CART, CHECKOUT, CUSTOM
- `layoutJson` - Published layout (Puck-format JSON)
- `draftJson` - Unpublished changes (Puck-format JSON)
- `handle` - URL slug for custom pages

### Theme
Platform-provided themes:
- `name` - Theme name
- `defaultLayoutsJson` - Starter layouts for each page type
- `cssVariables` - Theme color variables

### PageLayoutVersion
Version history for rollback:
- `versionNumber` - Sequential version
- `layoutSnapshot` - Layout at this version
- `changedBy` - User who made the change

## API Endpoints

### Public Storefront API (`/api/v1/storefront/{slug}`)

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/settings` | Store settings (branding, theme) | StoreSettings JSON |
| GET | `/layout?page={type}` | Page layout JSON | Raw Puck data `{content, root, zones}` |
| GET | `/pages/{handle}` | Custom page layout | Raw Puck data |
| GET | `/products` | Product listing with pagination | `{products, totalPages, ...}` |
| GET | `/products/{slug}` | Single product | Product JSON |
| GET | `/collections` | Category listing | Collection[] |
| GET | `/collections/{slug}` | Single category with products | Collection JSON |
| GET | `/themes` | Available themes | Theme[] |
| GET | `/schema/components` | Component registry | ComponentSchema[] |

### Merchant Layout Editor API (`/api/v1/stores`)

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| GET | `/settings` | Get store settings | MERCHANT, STAFF |
| PUT | `/settings` | Update store settings | MERCHANT |
| POST | `/publish` | Publish store | MERCHANT |
| POST | `/unpublish` | Unpublish store | MERCHANT |
| GET | `/layouts` | List all page layouts | MERCHANT, STAFF |
| GET | `/layouts/{pageType}` | Get specific layout | MERCHANT, STAFF |
| GET | `/layouts/{pageType}/draft` | Get draft (or published fallback) | MERCHANT, STAFF |
| PUT | `/layouts/{pageType}` | Update layout (saves to draftJson) | MERCHANT |
| POST | `/layouts/{pageType}/publish` | Publish: draftJson → layoutJson | MERCHANT |
| DELETE | `/layouts/{pageType}/draft` | Discard draft | MERCHANT |
| POST | `/layouts/{pageType}/rollback/{version}` | Rollback to version | MERCHANT |
| POST | `/pages` | Create custom page | MERCHANT |
| GET | `/pages/{handle}` | Get custom page | MERCHANT, STAFF |
| PUT | `/pages/{handle}` | Update custom page | MERCHANT |
| DELETE | `/pages/{handle}` | Delete custom page | MERCHANT |
| GET | `/themes` | List themes | MERCHANT, STAFF |
| POST | `/themes/{id}/apply` | Apply theme | MERCHANT |
| GET | `/schema/sections` | Component schemas | MERCHANT, STAFF |
| POST | `/layouts/generate` | AI generation (future) | MERCHANT |

## Hibernate 6 JSON Mapping (MariaDB Compatible)

All JSON fields use a custom JPA Converter for MariaDB compatibility:

```java
@Convert(converter = JsonMapConverter.class)
@Column(columnDefinition = "TEXT")
private Map<String, Object> layoutJson;
```

**Note**: We use `JsonMapConverter` instead of `@JdbcTypeCode(SqlTypes.JSON)` because MariaDB doesn't support the `CAST(? as json)` syntax that Hibernate 6 generates. The converter stores JSON as TEXT and handles serialization/deserialization using Jackson.

## Performance Optimizations

### 1. Database-Level Pagination
The `/products` endpoint uses proper Spring Data pagination:

```java
Page<Product> productPage = productRepository.findByTenantId(tenantId, pageable);
```

**Response format:**
```json
{
  "products": [...],
  "currentPage": 0,
  "totalPages": 5,
  "totalProducts": 120,
  "hasNext": true,
  "hasPrevious": false
}
```

### 2. N+1 Select Prevention
The `/collections` endpoint fetches all product counts in a single batch query:

```java
@Query("SELECT p.category.id, COUNT(p) FROM Product p WHERE p.tenantId = :tenantId GROUP BY p.category.id")
List<Object[]> countProductsByCategory(@Param("tenantId") Long tenantId);
```

This replaces N+1 queries (1 for categories + N for counts) with exactly 2 queries.

## Checkout Modes

Merchants can configure checkout behavior:

| Mode | Description |
|------|-------------|
| `GUEST_ONLY` | Customers checkout with email only |
| `ACCOUNT_ONLY` | Customers must create/login to account |
| `BOTH` | Customers choose guest or account checkout |

## Frontend Apps

| App | Port | Purpose |
|-----|------|---------|
| `frontend/admin/` | 3001 | Puck visual editor for merchants |
| `frontend/storefront/` | 3000 | Public storefront using Puck `<Render>` |
| `frontend/shared/` | N/A | Shared components, puck-config, API client |

## Completed Phases

1. **Phase 1: Backend Foundation** - Entities, APIs, Component Registry
2. **Phase 2: Next.js Storefront** - Layout rendering, 12 section components, cart/checkout
3. **Phase 3: Puck Visual Editor** - Admin app, Puck editor, shared component lib, legacy conversion

## Next Steps

1. **AI Generation** - Implement LLM-based layout generation (`POST /layouts/generate`)
2. **Version Control** - Add `PageLayoutVersion` for undo/rollback UI
3. **Custom Domains** - DNS configuration, SSL provisioning
4. **Performance** - CDN, image optimization, caching strategy
````
