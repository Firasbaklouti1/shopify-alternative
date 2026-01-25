# Storefront - Next.js E-Commerce Renderer

A Next.js 14+ storefront application that renders JSON-driven layouts from the Spring Boot backend. This is **Phase 2** of the JSON-Driven Storefront project.

## Features

- **JSON-Driven Layouts**: Pages are rendered based on JSON configurations from the API
- **React Server Components**: Optimal performance with server-side rendering
- **10 Core Section Components**:
  - `HeroBanner` - Hero images/videos with CTA
  - `ProductGrid` - Product listings with filtering
  - `ProductMain` - Product detail page
  - `CollectionList` - Category browsing
  - `RichText` - Custom content blocks
  - `ImageWithText` - Side-by-side layouts
  - `Newsletter` - Email signup forms
  - `Testimonials` - Customer reviews
  - `AnnouncementBar` - Promotional banners
  - `Footer` - Site footer with links
- **App Block Support**: Third-party Web Components integration
- **Cart & Checkout**: Full checkout flow with checkoutMode support
- **Editor Bridge**: postMessage communication for visual editor integration

## Getting Started

### Prerequisites

- Node.js 18+
- npm or yarn
- Spring Boot backend running on port 8080

### Installation

```bash
# Install dependencies
npm install

# Copy environment file
cp .env.example .env.local

# Start development server
npm run dev
```

The storefront will be available at `http://localhost:3000`.

### Environment Variables

```env
# Spring Boot API URL
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## Project Structure

```
src/
├── app/
│   ├── page.tsx                 # Landing page
│   ├── layout.tsx               # Root layout
│   └── store/[slug]/            # Dynamic store routes
│       ├── layout.tsx           # Store layout with header
│       ├── page.tsx             # Home page
│       ├── products/
│       │   ├── page.tsx         # Product listing
│       │   └── [productSlug]/
│       │       └── page.tsx     # Product detail
│       ├── collections/
│       │   ├── page.tsx         # Collections listing
│       │   └── [collectionSlug]/
│       │       └── page.tsx     # Collection products
│       ├── cart/
│       │   └── page.tsx         # Shopping cart
│       ├── checkout/
│       │   └── page.tsx         # Checkout flow
│       ├── account/
│       │   └── page.tsx         # Customer login/register/orders (Phase 2)
│       └── pages/
│           └── [handle]/
│               └── page.tsx     # Custom pages (about-us, etc.) (Phase 2)
├── components/
│   ├── LayoutRenderer.tsx       # JSON → React mapper
│   ├── StoreHeader.tsx          # Store navigation
│   ├── SortDropdown.tsx         # Product sorting (client component) (Phase 2)
│   ├── AppBlockLoader.tsx       # Web Components loader
│   └── sections/                # Section components
│       ├── HeroBanner.tsx
│       ├── ProductGrid.tsx
│       ├── ProductMain.tsx
│       └── ... (10 total)
└── lib/
    ├── api.ts                   # API client
    └── editor-bridge.ts         # Visual editor communication
```

## API Endpoints Used

| Endpoint | Description |
|----------|-------------|
| `GET /api/v1/storefront/{slug}/settings` | Store settings & branding |
| `GET /api/v1/storefront/{slug}/layout?page=` | Page layout JSON |
| `GET /api/v1/storefront/{slug}/pages/{handle}` | Custom page layout (Phase 2) |
| `GET /api/v1/storefront/{slug}/products` | Product listings |
| `GET /api/v1/storefront/{slug}/products/{slug}` | Single product |
| `GET /api/v1/storefront/{slug}/collections` | All collections |
| `GET /api/v1/storefront/{slug}/collections/{slug}` | Single collection |
| `POST /api/v1/auth/customer/{storeSlug}/register` | Customer registration (Phase 2) |
| `POST /api/v1/auth/login` | Customer/User login |
| `GET /api/v1/orders/my` | Customer's orders |

## Build & Deploy

```bash
# Build for production
npm run build

# Start production server
npm start
```

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.
