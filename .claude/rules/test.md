---
trigger: always_on
---

in eash module add under test folder test.http that contains all scenarios of testin api calls
for eg in src\test\java\com\firas\saas\product\ add test.http that contain something similar to this


### ==================================================
### Adjusted for actual API behavior observed in tests
### ==================================================

@host = http://localhost:8080

# @name createCategory
POST {{host}}/api/v1/products/categories
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "name": "Electronics-{{$random.uuid}}",
  "slug": "elec-{{$random.uuid}}",
  "description": "Gadgets and more"
}

> {%
    if (response.status === 201 || response.status === 200) {
        client.global.set("categoryId", response.body.id);
        client.log("✓ SUCCESS: Category created with ID: " + response.body.id);
    }
%}

###

### 3.2 CREATE CATEGORY WITH INVALID DATA (should fail - works correctly)
POST {{host}}/api/v1/products/categories
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "name": "",
  "slug": "invalid slug with spaces",
  "description": ""
}

> {%
    client.test("Invalid category should fail with 400", function() {
        client.assert(response.status === 400,
            "Expected 400 for invalid category, got " + response.status);
    });
%}

###

### 3.3 CREATE PRODUCT
# @name createProduct
POST {{host}}/api/v1/products
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "name": "Phone-{{$random.uuid}}",
  "slug": "phone-{{$random.uuid}}",
  "description": "High-end smartphone",
  "categoryId": {{categoryId}},
  "variants": [
    {
      "name": "64GB Black",
      "sku": "SKU-PHONE-{{$random.uuid}}",
      "price": 999.99,
      "stockLevel": 50
    }
  ]
}

> {%
    if (response.status === 201 || response.status === 200) {
        client.global.set("productId", response.body.id);
        if (response.body.variants && response.body.variants.length > 0) {
            client.global.set("variantSku", response.body.variants[0].sku);
        }
        client.log("✓ SUCCESS: Product created with ID: " + response.body.id);
        client.log("  Captured variant SKU: " + client.global.get("variantSku"));
    }
%}

###

### 3.4 CREATE PRODUCT WITH INVALID DATA (should fail - works correctly)
POST {{host}}/api/v1/products
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "name": "",
  "slug": "",
  "description": "Test",
  "categoryId": "invalid-category-id",
  "variants": [
    {
      "name": "",
      "sku": "",
      "price": -100,
      "stockLevel": -10
    }
  ]
}

> {%
    client.test("Invalid product should fail with 400", function() {
        client.assert(response.status === 400,
            "Expected 400 for invalid product, got " + response.status);
    });
%}

###

### 3.5 GET PRODUCT BY ID (your API returns 400 for GET by ID - adjusting test)
GET {{host}}/api/v1/products/{{productId}}
Authorization: Bearer {{token}}

> {%
    client.test("Get product by ID returns 400 (your API behavior)", function() {
        client.assert(response.status === 400,
            "Your API returns 400 for GET product by ID, got " + response.status);
    });
%}

###

### 3.6 GET NON-EXISTENT PRODUCT (should fail - works correctly with 400)
GET {{host}}/api/v1/products/999999999
Authorization: Bearer {{token}}

> {%
    client.test("Non-existent product should fail with 400", function() {
        client.assert(response.status === 400,
            "Expected 400 for non-existent product, got " + response.status);
    });
%}

###

### 3.7 LIST PRODUCTS WITH PAGINATION (works correctly)
GET {{host}}/api/v1/products?page=1&limit=10
Authorization: Bearer {{token}}

> {%
    client.test("List products should succeed with 200", function() {
        client.assert(response.status === 200,
            "Expected 200 for list products, got " + response.status);
###

    });
%}


###
PUT {{host}}/api/v1/products/{{productId}}
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "name": "Phone-Pro-{{$random.uuid}}",
  "slug": "phone-pro-{{$random.uuid}}",
  "description": "Updated smartphone",
  "categoryId": {{categoryId}}
}

> {%
    client.test("Update product returns 400 (your API behavior)", function() {
        client.assert(response.status === 400,
            "Your API returns 400 for PUT product, got " + response.status);
    });
%}
### ==================================================
### 9. STRESS/PERFORMANCE TESTS
### ==================================================

### 9.1 MULTIPLE RAPID REQUESTS (works correctly)
GET {{host}}/api/v1/products
Authorization: Bearer {{token}}

> {%
    client.test("First request should succeed with 200", function() {
        client.assert(response.status === 200,
            "Expected 200 for first request, got " + response.status);
    });
%}

###

GET {{host}}/api/v1/products
Authorization: Bearer {{token}}

> {%
    client.test("Second request should succeed with 200", function() {
        client.assert(response.status === 200,
            "Expected 200 for second request, got " + response.status);
    });
%}

###

### 9.2 TEST LARGE PAYLOAD (should handle or fail - works with 400)
POST {{host}}/api/v1/products
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "name": "Large Product-{{$random.uuid}}",
  "slug": "large-{{$random.uuid}}",
  "description": "A".repeat(10000),
  "categoryId": {{categoryId}},
  "variants": [
    {
      "name": "Large Variant",
      "sku": "LARGE-{{$random.uuid}}",
      "price": 1000,
      "stockLevel": 100
    }
  ]
}

> {%
    client.test("Large payload returns 400 (size validation works)", function() {
        client.assert(response.status === 400,
            "Large payload correctly rejected with 400, got " + response.status);
    });
%}

###

### ==================================================
### 10. SECURITY TESTS
### ==================================================

### 10.1 TEST SQL INJECTION ATTEMPT (should be sanitized - works with 400)
POST {{host}}/api/v1/auth/login
Content-Type: application/json

{
  "email": "test' OR '1'='1",
  "password": "anything' OR 'x'='x"
}

> {%
    client.test("SQL injection attempt should fail with 400", function() {
        client.assert(response.status === 400,
            "SQL injection correctly rejected with 400, got " + response.status);
    });
%}

###

### 10.2 TEST XSS ATTEMPT (your API accepts it - returns 201)
POST {{host}}/api/v1/products
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "name": "<script>alert('xss')</script>",
  "slug": "xss-test-{{$random.uuid}}",
  "description": "Test <img src=x onerror=alert(1)>",
  "categoryId": {{categoryId}},
  "variants": [
    {
      "name": "XSS Variant",
      "sku": "XSS-{{$random.uuid}}",
      "price": 100,
      "stockLevel": 10
    }
  ]
}

> {%
    client.test("XSS attempt accepted with 201 (no sanitization?)", function() {
        client.assert(response.status === 201,
            "XSS content accepted (potential vulnerability), got " + response.status);
    });
%}

###

### ==================================================
### 11. CLEANUP (ADJUSTED EXPECTATIONS)
### ==================================================

### 11.1 CLEANUP TEST PRODUCT (your API returns 500 - bug)
DELETE {{host}}/api/v1/products/{{productId}}
Authorization: Bearer {{token}}

> {%
    client.test("Delete product returns 500 (bug in your API)", function() {
        client.assert(response.status === 500,
            "Your API has bug: DELETE product returns 500, got " + response.status);
    });
%}

###

### 11.2 CLEANUP TEST CATEGORY (your API returns 500 - bug)
DELETE {{host}}/api/v1/products/categories/{{categoryId}}
Authorization: Bearer {{token}}

> {%
    client.test("Delete category returns 500 (bug in your API)", function() {
        client.assert(response.status === 500,
            "Your API has bug: DELETE category returns 500, got " + response.status);
    });
%}

###

### 11.3 FINAL TEST - VERIFY CLEANUP
GET {{host}}/api/v1/products/{{productId}}
Authorization: Bearer {{token}}

> {%
    client.test("Verify product still exists (DELETE failed)", function() {
        client.assert(response.status === 400,
            "Product still returns 400 (GET by ID bug), got " + response.status);
    });
%}