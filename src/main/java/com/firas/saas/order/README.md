# Order Module Documentation

## 📝 Overview
Handles the checkout process, cart management, and order lifecycle.

## 🔑 Key Entities
- **Order**: Represents a completed purchase.
- **OrderItem**: Specific line items in an order (Product Variant + Quantity).
- **Cart**: Temporary holding area for items before purchase.

```mermaid
erDiagram
    ORDER ||--o{ ORDER_ITEM : "contains"
    ORDER_ITEM }|--|| PRODUCT_VARIANT : "references"
    USER ||--o{ ORDER : "places"
```

## 🔄 Checkout Flow (Simplified)
```mermaid
graph LR
    Cart[Cart Created] --> Add[Items Added]
    Add --> Checkout[Checkout Initiated]
    Checkout --> Validate[Stock Validation]
    Validate --> Payment[Payment Processing]
    Payment --> Order[Order Created]
    Order --> Stock[Inventory Deducted]
```
