# Phase 2: Next.js Storefront Testing Guide

This guide walks you through testing the Phase 2 Next.js Storefront implementation.

## Prerequisites

- **Backend**: Spring Boot running on `http://localhost:8080`
- **Frontend**: Next.js running on `http://localhost:3000`
- **Database**: MySQL with the `saas_db` database
- **HTTP Client**: VS Code REST Client extension or similar

---

## Step 1: Start the Backend

```bash
cd C:\Users\firas\Downloads\shopify_alternative
java -jar target/saas-0.0.1-SNAPSHOT.jar
```

Wait until you see:
```
Started SaasApplication in X.XXX seconds
```

Verify it's running:
```
GET http://localhost:8080/api/v1/auth/health
```

---

## Step 2: Set Up Test Data

Open `frontend/storefront/setup-test-data.http` in VS Code and run each request **in order**:

### 2.1 Register a Merchant
Run the first request to create a demo merchant account.

### 2.2 Login
Run the login request. **Copy the JWT token** from the response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 2.3 Replace `{{token}}`
In the `.http` file, replace all instances of `{{token}}` with your actual token.

### 2.4 Create Categories
Run the category creation requests (requests 3 & 4).

### 2.5 Create Products
Run the product creation requests (requests 5, 6, 7).

### 2.6 Create Store Settings
Run request 8 to configure the store and **publish it**.

### 2.7 Create Page Layouts
Run requests 9, 10, 11 to create layouts for:
- HOME page
- PRODUCT page
- COLLECTION page

### 2.8 Verify API
Run the verification requests at the bottom to ensure data was created correctly.

---

## Step 3: Start the Frontend

```bash
cd C:\Users\firas\Downloads\shopify_alternative\frontend\storefront
npm run dev
```

Wait until you see:
```
✓ Ready in X.Xs
- Local: http://localhost:3000
```

---

## Step 4: Manual Testing

### 4.1 Landing Page
**URL**: http://localhost:3000

✅ **Expected**:
- See "Storefront" heading
- "View Demo Store" and "API Documentation" buttons

### 4.2 Store Home Page
**URL**: http://localhost:3000/store/demo

✅ **Expected**:
- Announcement bar at top ("Free shipping on orders over $50!")
- Hero banner with "Welcome to Demo Store"
- Featured Products section with product cards
- Shop by Category section
- About section
- Testimonials
- Newsletter signup
- Footer

### 4.3 Products List Page
**URL**: http://localhost:3000/store/demo/products

✅ **Expected**:
- "All Products" heading
- Product count displayed
- Category filter in sidebar
- Product grid with:
  - Product images (placeholders if none)
  - Product names
  - Prices
- Sorting dropdown

### 4.4 Product Detail Page
**URL**: http://localhost:3000/store/demo/products/wireless-headphones

✅ **Expected**:
- Product image gallery
- Product name and price
- Variant selector (Black/White)
- Quantity selector
- "Add to Cart" button
- Product description
- Related products section

### 4.5 Collections List Page
**URL**: http://localhost:3000/store/demo/collections

✅ **Expected**:
- "Collections" heading
- Cards for Electronics and Clothing
- Product counts shown

### 4.6 Collection Detail Page
**URL**: http://localhost:3000/store/demo/collections/electronics

✅ **Expected**:
- Collection name header
- Product grid with filtered products
- Only electronics products shown

### 4.7 Cart Page
**URL**: http://localhost:3000/store/demo/cart

✅ **Test Flow**:
1. Go to a product page
2. Click "Add to Cart"
3. Navigate to cart
4. **Expected**: See the added product, quantity controls, subtotal

### 4.8 Checkout Page
**URL**: http://localhost:3000/store/demo/checkout

✅ **Test Flow**:
1. Add product to cart
2. Go to checkout
3. Fill in customer info
4. Proceed through shipping and payment steps
5. **Expected**: Order confirmation page

---

## Step 5: Feature Checklist

| Feature | Location | Status |
|---------|----------|--------|
| Landing Page | `/` | ⬜ |
| Store Home | `/store/demo` | ⬜ |
| Hero Banner Section | Home page | ⬜ |
| Product Grid Section | Home page | ⬜ |
| Collection List Section | Home page | ⬜ |
| Testimonials Section | Home page | ⬜ |
| Newsletter Section | Home page | ⬜ |
| Footer Section | Home page | ⬜ |
| Products Page | `/store/demo/products` | ⬜ |
| Category Filtering | Products page sidebar | ⬜ |
| Sorting | Products page dropdown | ⬜ |
| Pagination | Products page (if >24 products) | ⬜ |
| Product Detail | `/store/demo/products/[slug]` | ⬜ |
| Variant Selection | Product page | ⬜ |
| Add to Cart | Product page | ⬜ |
| Collections List | `/store/demo/collections` | ⬜ |
| Collection Detail | `/store/demo/collections/[slug]` | ⬜ |
| Cart | `/store/demo/cart` | ⬜ |
| Cart Count Badge | Header | ⬜ |
| Checkout Flow | `/store/demo/checkout` | ⬜ |
| Guest/Account Toggle | Checkout (BOTH mode) | ⬜ |
| Responsive Design | All pages (mobile view) | ⬜ |
| Store Header | All store pages | ⬜ |
| 404 Page | `/store/nonexistent` | ⬜ |

---

## Step 6: Component Testing

Each section component can be tested by modifying the layout JSON:

### Test Announcement Bar
Modify `announcement` section in the HOME layout:
- Change text, colors, link
- Set `dismissible: false`

### Test Hero Banner
Modify `hero` section:
- Change height (`small`, `medium`, `large`, `full`)
- Change `text_alignment` (`left`, `center`, `right`)
- Add `bg_image` URL

### Test Product Grid
Modify `featured-products` section:
- Change `limit` (4, 8, 12)
- Change `columns` (2, 3, 4, 5)
- Add `collection_handle` to filter by category

### Test Testimonials
Modify `testimonials` section:
- Change `layout` (`grid` vs `carousel`)
- Toggle `show_rating`

---

## Troubleshooting

### "Store not found" (404)
- Ensure store is published: Check `StoreSettings.published = true`
- Verify tenant slug matches: Should be "demo"

### "Failed to fetch products"
- Check backend is running on port 8080
- Verify CORS is configured correctly
- Check `.env.local` has correct `NEXT_PUBLIC_API_URL`

### Cart not persisting
- Cart uses localStorage - check browser dev tools
- Ensure JavaScript is enabled

### Styles not loading
- Run `npm run build` to check for CSS errors
- Clear `.next` folder and restart

---

## Phase 2 Complete Criteria

✅ All 10 section components render correctly
✅ JSON-driven layout rendering works
✅ Products/Collections pages with pagination
✅ Cart with localStorage persistence
✅ Checkout flow with checkoutMode support
✅ Responsive design
✅ Store header with cart badge
✅ 404 handling for missing stores/products

---

## Next: Phase 3 - Visual Editor

After Phase 2 is validated, proceed to Phase 3:
- Visual drag-and-drop editor
- iframe preview with postMessage communication
- Section settings panel
- Save/Publish workflow
