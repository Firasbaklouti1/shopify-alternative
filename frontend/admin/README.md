# Admin Editor (Puck Visual Page Builder)

Merchant-facing admin app built with Next.js 16.1.4 and **Puck** (`@measured/puck` v0.20.2). Provides a drag-and-drop visual page builder for storefront customization.

## Architecture

```
frontend/admin/ (port 3001)
├── Login     → JWT auth via Spring Boot /api/v1/auth/login
├── Dashboard → Page type list (HOME, PRODUCT, COLLECTION, custom pages)
└── Editor    → <Puck> component with full drag-drop, field editing, viewport preview
      │
      │  Load draft:  GET  /api/v1/stores/layouts/{pageType}/draft
      │  Save draft:  PUT  /api/v1/stores/layouts/{pageType}
      │  Publish:     POST /api/v1/stores/layouts/{pageType}/publish
      ▼
Spring Boot Backend (port 8080)
```

## Tech Stack

- **Next.js** 16.1.4 (App Router, Turbopack)
- **React** 19.2.3
- **Puck** 0.20.2 (`@measured/puck`)
- **Tailwind CSS** 4
- **TypeScript** 5

## Pages

| Route | Component | Description |
|-------|-----------|-------------|
| `/` | `page.tsx` | Redirects to `/login` |
| `/login` | `login/page.tsx` | Email/password login form, stores JWT in localStorage |
| `/dashboard` | `dashboard/page.tsx` | Page type list with "Edit" links for each layout |
| `/editor/[slug]/[pageType]` | `editor/[slug]/[pageType]/page.tsx` | Puck visual editor |

## Editor Features

- **Drag & Drop**: Add, reorder, remove sections from a categorized component sidebar
- **Field Editing**: Click any component to edit its properties in the right panel
- **Auto-Save**: Drafts are auto-saved with a 2-second debounce on every change
- **Publish**: "Publish" button saves draft then pushes it live to the public storefront
- **Viewport Preview**: Switch between Mobile (360px), Tablet (768px), Desktop (1280px)
- **Undo/Redo**: Built-in history management from Puck
- **12 Components**: Shared with the storefront via `frontend/shared/`

## Auth Flow

1. User submits email/password on login page
2. `POST /api/v1/auth/login` returns JWT token
3. `GET /api/v1/tenants/my` fetches the merchant's `storeSlug`
4. Token + storeSlug stored in `localStorage` via `AuthProvider` context
5. All editor API calls include `Authorization: Bearer {token}` header

## Shared Dependencies

Components and config are imported from `frontend/shared/` via TypeScript path alias:

```typescript
import { puckConfig } from '@shared/lib/puck-config';
import { convertLegacyToPuck } from '@shared/lib/puck-utils';
```

Configured in `tsconfig.json`:
```json
{
  "compilerOptions": {
    "paths": {
      "@shared/*": ["../shared/*"],
      "@/*": ["./src/*"]
    }
  }
}
```

And `next.config.ts`:
```typescript
const nextConfig = {
  transpilePackages: [path.resolve(__dirname, "../shared")],
  turbopack: {
    root: path.resolve(__dirname, "../.."),
  },
};
```

## Puck Data Format

The editor stores layouts in Puck's native format:

```json
{
  "content": [
    {
      "type": "HeroBanner",
      "props": {
        "id": "HeroBanner-1",
        "title": "Welcome to Our Store",
        "cta_text": "Shop Now",
        "height": "large"
      }
    },
    {
      "type": "ProductGrid",
      "props": {
        "id": "ProductGrid-1",
        "title": "Featured Products",
        "columns": 4,
        "limit": 8
      }
    }
  ],
  "root": { "props": { "title": "Home Page" } },
  "zones": {}
}
```

Legacy format (`{sections, order}`) from Phase 2 is auto-converted on load via `convertLegacyToPuck()`.

## Development

```bash
cd frontend/admin
npm install
npm run dev    # Starts on http://localhost:3001
```

Requires the Spring Boot backend running on `http://localhost:8080`.

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `NEXT_PUBLIC_API_URL` | `http://localhost:8080` | Spring Boot backend URL |

## API Endpoints Used

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/login` | Authenticate merchant |
| GET | `/api/v1/tenants/my` | Get merchant's tenant info |
| GET | `/api/v1/stores/layouts/{pageType}/draft` | Load draft layout for editing |
| PUT | `/api/v1/stores/layouts/{pageType}` | Save draft layout |
| POST | `/api/v1/stores/layouts/{pageType}/publish` | Publish draft to live |
