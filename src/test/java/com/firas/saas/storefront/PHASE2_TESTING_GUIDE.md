# Phase 2 Testing Guide - Storefront Module

This guide walks you through testing the Phase 2 implementation of the Shopify Alternative platform, which includes the Storefront Module with JSON-driven page layouts, store settings, and public APIs.

---

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Quick Start](#quick-start)
3. [Running the Demo Store Setup](#running-the-demo-store-setup)
4. [Manual Testing Steps](#manual-testing-steps)
5. [API Endpoints Reference](#api-endpoints-reference)
6. [Frontend Testing](#frontend-testing)
7. [Expected Results](#expected-results)
8. [Troubleshooting](#troubleshooting)

---

## 🔧 Prerequisites

### 1. Backend (Spring Boot)

Ensure your Spring Boot application is running:

```powershell
# Navigate to project root
cd C:\Users\firas\Downloads\shopify_alternative

# Build and run (if not already running)
mvn spring-boot:run
```

**Verify it's running:**
- Open: http://localhost:8080/actuator/health
- Should return: `{"status":"UP"}`

### 2. Database

The application should automatically create/update these tables:
- `store_settings` - Store configuration & branding
- `page_layouts` - JSON-driven page layouts
- `page_layout_versions` - Version history for rollback
- `themes` - Pre-built theme templates

### 3. Frontend (Optional - for visual testing)

```powershell
cd frontend\storefront
npm install
npm run dev
```

**Verify it's running:**
- Open: http://localhost:3000

---

## 🚀 Quick Start

### Option 1: Run the Complete Demo Setup (Recommended)

1. Open **IntelliJ IDEA** or any HTTP client that supports `.http` files
2. Open: `src/test/java/com/firas/saas/storefront/store-demo.http`
3. Run each request in order (sections 1-10)

### Option 2: Use cURL

```powershell
# 1. Register
curl -X POST http://localhost:8080/api/v1/auth/register `
  -H "Content-Type: application/json" `
  -d '{"storeName":"Demo Store","storeSlug":"demo","email":"demo@example.com","password":"password123","fullName":"Demo Merchant"}'

# 2. Login (save the token!)
curl -X POST http://localhost:8080/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"demo@example.com","password":"password123"}'
```

---

## 📝 Running the Demo Store Setup

The `store-demo.http` file creates a complete demo store with:

| Section | What It Creates |
|---------|-----------------|
| **1. Auth** | Registers & logs in demo merchant |
| **2. Categories** | Electronics, Clothing |
| **3. Products** | Smartphone, Headphones, T-Shirt (with variants) |
| **4. Store Settings** | Branding, colors, SEO, social links |
| **5. Page Layouts** | HOME, PRODUCT, COLLECTION layouts |
| **6. Custom Pages** | About Us, Contact pages |
| **7-10. Verification** | Tests public APIs and validates setup |

### Execution Order

Run requests in this exact order:

```
1.1 Register → 1.2 Login → 1.3 Get Tenant Info
↓
2.1 Create Electronics → 2.2 Create Clothing → 2.3 Verify Categories
↓
3.1 Create Smartphone → 3.2 Create Headphones → 3.3 Create T-Shirt
↓
4.1 Get Settings → 4.2 Update Settings → 4.3 Publish Store
↓
5.0 Get Themes → 5.0.1 Apply Theme (IMPORTANT!)
↓
5.1 Update Home Layout → 5.2 Publish Home → 5.3 Update Product Layout...
↓
(Continue through all sections)
```

> ⚠️ **Important:** Step 5.0.1 (Apply Theme) is required before updating layouts!

---

## 🧪 Manual Testing Steps

### Step 1: Verify Authentication

```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "email": "demo@example.com",
  "password": "password123"
}
```

**Expected:** Returns `token` and `tenantId`

### Step 2: Verify Store Settings

```http
GET http://localhost:8080/api/v1/stores/settings
Authorization: Bearer {your-token}
```

**Expected:** Returns store configuration with:
- `checkoutMode`: "BOTH"
- `globalStyles.primaryColor`: "#2563EB"
- `published`: true

### Step 3: Verify Categories & Products

> **Important:** Categories are under `/api/v1/products/categories` (NOT `/api/v1/categories`)

```http
GET http://localhost:8080/api/v1/products/categories
Authorization: Bearer {your-token}
```

**Expected:** Returns array with Electronics and Clothing categories

### Step 4: Verify Page Layouts

> **Important:** Before updating layouts, you must apply a theme first to create default layouts:
> ```http
> POST http://localhost:8080/api/v1/stores/themes/{themeId}/apply
> ```

```http
GET http://localhost:8080/api/v1/stores/layouts
Authorization: Bearer {your-token}
```

**Expected:** Returns array with layouts for:
- HOME
- PRODUCT
- COLLECTION
- CUSTOM (about-us, contact)

### Step 4: Test Public Storefront API

These endpoints work **without authentication**:

```http
# Get store settings
GET http://localhost:8080/api/v1/storefront/demo/settings

# Get home page layout
GET http://localhost:8080/api/v1/storefront/demo/layout?page=home

# Get products
GET http://localhost:8080/api/v1/storefront/demo/products?page=0&limit=10

# Get collections (categories)
GET http://localhost:8080/api/v1/storefront/demo/collections

# Get specific product
GET http://localhost:8080/api/v1/storefront/demo/products/pro-smartphone-x1
```

### Step 5: Test in Browser (Frontend)

1. Navigate to: http://localhost:3000/store/demo
2. You should see the home page with:
   - Announcement bar
   - Hero banner
   - Featured products grid
   - Categories section
   - Testimonials
   - Newsletter signup
   - Footer

---

## 📚 API Endpoints Reference

### Merchant APIs (Require Auth)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/stores/settings` | Get store settings |
| PUT | `/api/v1/stores/settings` | Update store settings |
| POST | `/api/v1/stores/publish` | Publish store |
| POST | `/api/v1/stores/unpublish` | Unpublish store |
| GET | `/api/v1/stores/layouts` | Get all layouts |
| GET | `/api/v1/stores/layouts/{pageType}` | Get specific layout (HOME, PRODUCT, COLLECTION) |
| PUT | `/api/v1/stores/layouts/{pageType}` | Update layout (saves to draft) |
| POST | `/api/v1/stores/layouts/{pageType}/publish` | Publish layout |
| GET | `/api/v1/stores/layouts/{pageType}/versions` | Get version history |
| POST | `/api/v1/stores/layouts/{pageType}/rollback/{version}` | Rollback to version |
| GET | `/api/v1/stores/themes` | Get available themes |
| POST | `/api/v1/stores/themes/{themeId}/apply` | Apply theme & create default layouts |
| POST | `/api/v1/stores/pages` | Create custom page |
| POST | `/api/v1/stores/pages/{handle}/publish` | Publish custom page |
| GET | `/api/v1/stores/schema/sections` | Get section schemas |
| GET | `/api/v1/products/categories` | Get categories |
| POST | `/api/v1/products/categories` | Create category |
| GET | `/api/v1/products` | Get products |
| POST | `/api/v1/products` | Create product |

### Public Storefront APIs (No Auth)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/storefront/{slug}/settings` | Get public store settings |
| GET | `/api/v1/storefront/{slug}/layout?page={type}` | Get page layout |
| GET | `/api/v1/storefront/{slug}/products` | Get products (paginated) |
| GET | `/api/v1/storefront/{slug}/products/{slug}` | Get product by slug |
| GET | `/api/v1/storefront/{slug}/collections` | Get collections |
| GET | `/api/v1/storefront/{slug}/collections/{slug}` | Get collection by slug |
| GET | `/api/v1/storefront/{slug}/pages/{handle}` | Get custom page |
| GET | `/api/v1/storefront/{slug}/schema/components` | Get component schemas |

---

## 🖥️ Frontend Testing

### Test the Next.js Storefront

1. **Start the frontend:**
   ```powershell
   cd frontend\storefront
   npm run dev
   ```

2. **Visit the demo store:**
   - URL: http://localhost:3000/store/demo

3. **Test these pages:**
   - Home: http://localhost:3000/store/demo
   - Products: http://localhost:3000/store/demo/products
   - Product Detail: http://localhost:3000/store/demo/products/pro-smartphone-x1
   - Collections: http://localhost:3000/store/demo/collections/electronics
   - About: http://localhost:3000/store/demo/pages/about-us

### What to Verify

- [ ] Announcement bar displays with correct text
- [ ] Hero banner shows with image and CTAs
- [ ] Product grid displays products with prices
- [ ] Categories show Electronics and Clothing
- [ ] Product detail page shows variants
- [ ] Store branding (colors) applied correctly
- [ ] Footer shows navigation links

---

## ✅ Expected Results

### After Running store-demo.http

| Item | Count | Details |
|------|-------|---------|
| Merchant | 1 | demo@example.com |
| Categories | 2 | Electronics, Clothing |
| Products | 3 | Smartphone (4 variants), Headphones (3 variants), T-Shirt (5 variants) |
| Page Layouts | 5+ | HOME, PRODUCT, COLLECTION, about-us, contact |
| Store Published | Yes | Accessible at /api/v1/storefront/demo |

### Final Verification Output

Running section 10 should show:
```
✅ Store settings: OK
   - Primary Color: #2563EB
   - Checkout Mode: BOTH
   - Published: true
✅ Products created: 3
✅ Categories created: 2
✅ Page layouts created: 5
   - HOME: Published
   - PRODUCT: Published
   - COLLECTION: Published
   - CUSTOM (about-us): Published
   - CUSTOM (contact): Published
```

---

## 🔧 Troubleshooting

### Issue: "Store not found" (404)

**Cause:** Store not published or wrong slug

**Fix:**
```http
POST http://localhost:8080/api/v1/stores/publish
Authorization: Bearer {token}
```

### Issue: "Unauthorized" (401)

**Cause:** Token expired or missing

**Fix:** Re-run the login request (1.2) and update the token

### Issue: Categories return 500 error

**Cause:** Wrong endpoint being used

**Fix:** Use `/api/v1/products/categories` NOT `/api/v1/categories`

### Issue: Categories/Products not created

**Cause:** May already exist from previous run

**Fix:** Check with GET requests first, or use unique slugs

### Issue: Layout returns 404 when updating

**Cause:** Layouts don't exist yet - must apply theme first

**Fix:** Apply a theme to create default layouts:
```http
# First get available themes
GET http://localhost:8080/api/v1/stores/themes
Authorization: Bearer {token}

# Then apply a theme
POST http://localhost:8080/api/v1/stores/themes/{themeId}/apply
Authorization: Bearer {token}
```

### Issue: Layout returns empty sections

**Cause:** Layout not published (still in draft)

**Fix:**
```http
POST http://localhost:8080/api/v1/stores/layouts/HOME/publish
Authorization: Bearer {token}
```

### Issue: "Unsubstituted variable" error

**Cause:** HTTP client treating `{{variable}}` as template variables

**Fix:** 
- Use single curly braces for template placeholders: `{product_name}`
- Only double curly braces for HTTP client variables: `{{merchantToken}}`
- Run requests in order to set variables from previous responses

### Issue: Frontend shows blank page

**Possible causes:**
1. Backend not running
2. Store not published
3. CORS issues

**Fix:** Check browser console for errors, verify backend is at http://localhost:8080

### Issue: Database tables missing

**Fix:** Restart Spring Boot application - tables are auto-created on startup

---

## 🧹 Reset Demo Store

To start fresh, you can:

1. **Delete via API** (if endpoint exists)
2. **Direct database cleanup:**
   ```sql
   -- Run in your database
   DELETE FROM page_layout_versions WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'demo');
   DELETE FROM page_layouts WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'demo');
   DELETE FROM store_settings WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'demo');
   DELETE FROM products WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'demo');
   DELETE FROM categories WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'demo');
   DELETE FROM users WHERE email = 'demo@example.com';
   DELETE FROM tenants WHERE slug = 'demo';
   ```

3. **Or simply use a different slug** in store-demo.http

---

## 📊 Phase 2 Features Checklist

| Feature | Status | Test |
|---------|--------|------|
| Store Settings API | ✅ | Section 4 |
| Global Styles (Branding) | ✅ | Section 4.2 |
| SEO Defaults | ✅ | Section 4.2 |
| Store Publish/Unpublish | ✅ | Section 4.3 |
| JSON Page Layouts | ✅ | Section 5 |
| Section-based Design | ✅ | Section 5.1 |
| Layout Versioning | ✅ | test.http Section 9 |
| Layout Rollback | ✅ | test.http Section 9 |
| Custom Pages | ✅ | Section 6 |
| Public Storefront API | ✅ | Section 8 |
| Multi-tenant Isolation | ✅ | test.http Section 8 |
| Component Schemas | ✅ | Section 7.2 |
| Products with Variants | ✅ | Section 3 |
| Categories/Collections | ✅ | Section 2 |

---

## 📞 Support

If you encounter issues:
1. Check the troubleshooting section above
2. Review server logs for errors
3. Verify database connectivity
4. Ensure all prerequisites are met

---

**Happy Testing! 🎉**
