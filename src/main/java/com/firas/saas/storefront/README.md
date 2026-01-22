````# Storefront Module

This module implements the **JSON-Driven Storefront** system, enabling merchants to create customizable websites for their stores using a drag-and-drop editor without writing code.

## Architecture

The storefront follows the **Server-Driven UI** pattern (similar to Shopify Online Store 2.0):

```
┌─────────────────────────────────────────────────────────────────┐
│                    MERCHANT ADMIN                                │
│  ┌─────────────────┐  postMessage  ┌─────────────────────────┐  │
│  │ Editor Sidebar  │◄─────────────►│ Storefront (iframe)     │  │
│  └─────────────────┘               └─────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT API                               │
│  ┌─────────────────────┐     ┌─────────────────────┐            │
│  │ Public Storefront   │     │ Layout Editor API   │            │
│  │ /api/v1/storefront  │     │ /api/v1/stores      │            │
│  │ (No Auth)           │     │ (MERCHANT Role)     │            │
│  └─────────────────────┘     └─────────────────────┘            │
└─────────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                    NEXT.JS STOREFRONT                            │
│  Fetches JSON layout → Maps to React components → Renders HTML  │
└─────────────────────────────────────────────────────────────────┘
```

## Key Concepts

### JSON Layout Configuration

Store layouts are defined as JSON, not HTML. Example:

```json
{
  "sections": {
    "hero-1": {
      "type": "hero-banner",
      "settings": {
        "title": "Welcome to Our Store",
        "bg_image": "/uploads/hero.jpg",
        "cta_text": "Shop Now"
      }
    },
    "featured-products": {
      "type": "product-grid",
      "settings": {
        "title": "Featured Products",
        "limit": 8,
        "columns": 4
      }
    }
  },
  "order": ["hero-1", "featured-products"]
}
```

### Component Registry

Available section types are defined in `ComponentRegistry.java`:
- `hero-banner` - Large banner with CTA
- `product-grid` - Grid of products
- `product-main` - Product detail (for product pages)
- `collection-list` - Grid of categories
- `rich-text` - Custom text content
- `image-with-text` - Side-by-side image and text
- `newsletter` - Email signup
- `testimonials` - Customer reviews
- `announcement-bar` - Top promotional banner
- `footer` - Site footer
- `app-block` - Third-party app components

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
- `layoutJson` - Published layout (JSON)
- `draftJson` - Unpublished changes (JSON)
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

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/settings` | Store settings (branding, theme) |
| GET | `/layout?page={type}` | Page layout JSON |
| GET | `/pages/{handle}` | Custom page layout |
| GET | `/products` | Product listing |
| GET | `/products/{slug}` | Single product |
| GET | `/collections` | Category listing |
| GET | `/collections/{slug}` | Single category |
| GET | `/themes` | Available themes |
| GET | `/schema/components` | Component registry |

### Merchant Layout Editor API (`/api/v1/stores`)

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| GET | `/settings` | Get store settings | MERCHANT, STAFF |
| PUT | `/settings` | Update store settings | MERCHANT |
| POST | `/publish` | Publish store | MERCHANT |
| POST | `/unpublish` | Unpublish store | MERCHANT |
| GET | `/layouts` | List all page layouts | MERCHANT, STAFF |
| GET | `/layouts/{pageType}` | Get specific layout | MERCHANT, STAFF |
| PUT | `/layouts/{pageType}` | Update layout (draft) | MERCHANT |
| POST | `/layouts/{pageType}/publish` | Publish layout | MERCHANT |
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

## Next Steps

1. **Next.js Storefront** - Build the renderer that consumes this API
2. **Visual Editor** - Build iframe + postMessage editor UI
3. **App Blocks** - Integrate Web Components for third-party apps
4. **AI Generation** - Implement LLM-based layout generation
````