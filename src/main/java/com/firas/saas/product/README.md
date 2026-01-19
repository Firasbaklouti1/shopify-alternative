# Product Module Documentation

## 📝 Overview
Manages the catalog functionality: specific products, variants (size/color), and categories.

## 🔑 Key Entities
- **Category**: Hierarchical grouping of products.
- **Product**: main item entity.
- **ProductVariant**: Specific SKU (e.g., "Red Shirt Size L").

```mermaid
classDiagram
    Product "1" *-- "many" ProductVariant
    Category "1" *-- "many" Product
    
    class Product {
        +String name
        +String slug
        +String description
        +boolean active
    }
    
    class ProductVariant {
        +String sku
        +String name
        +BigDecimal price
        +Integer stockLevel
    }
```

## 🔄 API Operations
- `GET /api/v1/products`: List products (Public/Authorized).
- `POST /api/v1/products`: Create product (Merchant).
- `DELETE /api/v1/products/{id}`: Soft delete product.
