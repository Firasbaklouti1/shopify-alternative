# Shared Component Library

Shared React components and utilities used by both `frontend/admin/` (Puck editor) and `frontend/storefront/` (public storefront). This ensures WYSIWYG consistency: what the merchant sees in the editor is exactly what renders on the public site.

## Structure

```
frontend/shared/
├── components/
│   └── sections/           # 12 Puck-compatible section components
│       ├── HeroBanner.tsx
│       ├── AnnouncementBar.tsx
│       ├── ProductGrid.tsx
│       ├── ProductMain.tsx
│       ├── CollectionList.tsx
│       ├── CollectionFilters.tsx
│       ├── RichText.tsx
│       ├── ImageWithText.tsx
│       ├── Newsletter.tsx
│       ├── Testimonials.tsx
│       ├── Footer.tsx
│       ├── AppBlock.tsx
│       └── index.ts        # Barrel export
├── lib/
│   ├── puck-config.tsx     # Puck Config object (fields, categories, render fns)
│   ├── puck-utils.ts       # Legacy format converter
│   ├── api.ts              # Storefront API client
│   └── template-vars.ts    # Template variable resolution
├── package.json
└── tsconfig.json
```

## Component Architecture

All 12 section components follow a consistent pattern for Puck compatibility:

```typescript
interface ComponentProps {
  // Flat props (no wrapper object)
  title?: string;
  subtitle?: string;
  // ... component-specific props

  // Puck injects this automatically
  puck?: {
    isEditing: boolean;
    metadata: Record<string, unknown>;
  };
}

export default function ComponentName({
  title = 'Default',
  // ...defaults
  puck,
}: ComponentProps) {
  // Extract context from Puck metadata
  const storeSlug = (puck?.metadata?.storeSlug as string) || '';
  const apiUrl = (puck?.metadata?.apiUrl as string) || 'http://localhost:8080';

  // Self-fetch data if needed (e.g., ProductGrid fetches products)
  // Render component UI
}
```

### Key patterns:
- **Flat props**: Props are passed directly (not wrapped in `section.settings`)
- **Puck metadata**: Store context (`storeSlug`, `apiUrl`, `product`, `collections`) passed via `puck.metadata`
- **Self-fetching**: Commerce components (ProductGrid, CollectionList) fetch their own data using metadata
- **Page data**: ProductMain receives `product` from metadata, CollectionFilters receives `collections`

## Components

| Component | Category | Props | Description |
|-----------|----------|-------|-------------|
| **HeroBanner** | Hero | title, subtitle, bg_image, bg_video, cta_text, cta_link, overlay_opacity, text_color, text_alignment, height | Full-width hero with image/video background and CTA buttons |
| **AnnouncementBar** | Hero | text, link, link_text, background_color, text_color, dismissible | Top promotional banner |
| **ProductGrid** | Commerce | title, subtitle, collection_handle, limit, columns, show_price, show_vendor, show_sale_badge, image_ratio | Self-fetching product grid |
| **ProductMain** | Commerce | gallery_position, show_vendor, show_sku, show_quantity_selector | Product detail with gallery and variant picker |
| **CollectionList** | Commerce | title, subtitle, columns, limit, show_product_count, image_ratio, card_style | Self-fetching collection cards |
| **CollectionFilters** | Commerce | show_sort, show_filter, filter_type | Sort/filter controls for collection pages |
| **RichText** | Content | title, heading, content, text_alignment, max_width, padding, background_color, text_color | Custom text/HTML content block |
| **ImageWithText** | Content | image_url, image_position, image_width, title, subtitle, content, cta_text, cta_link, background_color, text_color, vertical_alignment | Side-by-side image and text |
| **Newsletter** | Content | title, subtitle, placeholder, button_text, success_message, background_color, text_color, text_alignment, layout | Email signup form |
| **Testimonials** | Social Proof | title, subtitle, layout, columns, show_rating, background_color, testimonials[] | Customer reviews grid/carousel |
| **Footer** | Layout | logo_url, tagline, show_social, social_*, show_payment_icons, copyright_text, background_color, text_color, columns[] | Site footer with link columns |
| **AppBlock** | Integrations | app_id, script_url, tag_name | Third-party Web Component loader |

## Puck Config (`lib/puck-config.tsx`)

Exports `puckConfig: Config` which defines:
- **Categories**: hero, content, commerce, socialProof, layout, integrations
- **Component definitions**: Each with `label`, `fields` (with types and options), `defaultProps`, and `render` function
- **Root config**: Page-level title field, layout wrapper `<div>`

## Legacy Format Converter (`lib/puck-utils.ts`)

Exports `convertLegacyToPuck(data)` which handles:
- **Puck format** (`{content, root, zones}`) — returned as-is
- **Legacy format** (`{sections, order}`) — converts `sections[key].settings` to flat `props` in `content[]` array
- **Unknown format** — returns `null`

Used in both the editor (load draft) and storefront (render published layout) for backward compatibility.

## API Client (`lib/api.ts`)

Exports typed fetch functions for all public storefront endpoints:
- `getStoreSettings(slug)` → `StoreSettings`
- `getPageLayout(slug, pageType)` → `PuckData` (with legacy conversion)
- `getCustomPageLayout(slug, handle)` → `PuckData` (with legacy conversion)
- `getProducts(slug, options)` → `ProductsResponse`
- `getProduct(slug, productSlug)` → `Product`
- `getCollections(slug)` → `Collection[]`
- `getCollection(slug, collectionSlug)` → `Collection`
- Cart management: `getCart()`, `addToCart()`, `removeFromCart()`, `clearCart()`

## How Apps Import Shared Code

Both `frontend/admin/` and `frontend/storefront/` import via TypeScript path alias `@shared/*`:

```typescript
// In any admin or storefront file:
import { puckConfig } from '@shared/lib/puck-config';
import { convertLegacyToPuck } from '@shared/lib/puck-utils';
import type { Product, Collection } from '@shared/lib/api';
import HeroBanner from '@shared/components/sections/HeroBanner';
```

**tsconfig.json** (in both apps):
```json
{
  "compilerOptions": {
    "paths": {
      "@shared/*": ["../shared/*"]
    }
  }
}
```

**next.config.ts** (in both apps):
```typescript
import path from "path";
const nextConfig = {
  transpilePackages: [path.resolve(__dirname, "../shared")],
  turbopack: {
    root: path.resolve(__dirname, "../.."),  // Project root for module resolution
  },
};
```

## Dependencies

```json
{
  "@measured/puck": "^0.20.2",
  "react": "19.2.3",
  "react-dom": "19.2.3",
  "tailwindcss": "^4"
}
```

These are installed in `frontend/shared/node_modules/` to satisfy Turbopack's module resolution during transpilation.
