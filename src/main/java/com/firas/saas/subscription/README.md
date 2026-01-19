# Subscription & Billing Module

## 📝 Overview
Manages platform revenue. Allows Admins to define Plans and Merchants to subscribe to them. Handles payment processing via Strategy Pattern.

## 🔑 Key Entities

- **SubscriptionPlan**: The product being sold (e.g., "Pro Plan").
- **Subscription**: The relationship between a Tenant and a Plan.
- **Invoice**: Record of payment.

```mermaid
erDiagram
    TENANT ||--o| SUBSCRIPTION : "has_active"
    SUBSCRIPTION }|--|| SUBSCRIPTION_PLAN : "is_for"
    TENANT ||--o{ INVOICE : "receives"
```

## 💸 Billing Strategy Pattern
The module uses the Strategy Pattern to support multiple payment gateways.

```mermaid
classDiagram
    class PaymentStrategy {
        <<interface>>
        +processPayment(PaymentRequest): boolean
    }
    
    class MockPaymentStrategy {
        +processPayment(): boolean
    }
    
    class StripePaymentStrategy {
        +processPayment(): boolean
    }
    
    PaymentStrategy <|-- MockPaymentStrategy
    PaymentStrategy <|-- StripePaymentStrategy
    
    class SubscriptionService {
        -PaymentService paymentService
    }
    
    SubscriptionService ..> PaymentStrategy : Delegated via PaymentService
```

## 🔄 Subscription Flow
1. **Admin** creates Plan (`POST /plans`).
2. **Merchant** selects Plan (`POST /subscribe`).
3. **PaymentService** processes payment (MOCK/STRIPE).
4. If Success:
   - **Subscription** created/updated.
   - **Invoice** generated (`PAID`).
