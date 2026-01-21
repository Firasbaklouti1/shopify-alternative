# Tenant Module Documentation

## 📝 Overview

The Tenant module is the **heart of the multi-tenancy architecture**. It handles the onboarding of new merchants (creating "Stores") and serves as the root aggregate for all business data.

> **Important**: Every business entity in this system belongs to a Tenant. The Tenant is the foundation of data isolation.

## 📁 Package Structure

```
com.firas.saas.tenant/
├── entity/
│   └── Tenant.java
├── repository/
│   └── TenantRepository.java
├── dto/
│   ├── TenantCreateRequest.java
│   └── TenantResponse.java
├── service/
│   ├── TenantService.java
│   └── TenantServiceImpl.java
├── controller/
│   └── TenantController.java
└── exception/
    └── TenantNotFoundException.java
```

## 🔑 Key Entities

### Tenant

Represents a Merchant's Store. This is the root entity of the domain.

```java
@Entity
@Table(name = "tenants")
public class Tenant extends BaseEntity {
    private String name;        // Display name of the store
    private String slug;        // Unique URL identifier (e.g., "my-store")
    private String ownerEmail;  // Email of the merchant owner
    private boolean active;     // Store status
}
```

```mermaid
classDiagram
    class Tenant {
        +Long id
        +String name
        +String slug
        +String ownerEmail
        +boolean active
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }
```

## 🏛️ Tenant as Root Aggregate

All business entities reference the Tenant:

```mermaid
graph TD
    T[Tenant] --> U[Users]
    T --> C[Customers]
    T --> P[Products]
    T --> O[Orders]
    T --> S[Subscriptions]
    T --> I[Invoices]
    T --> PY[Payments]
    
    P --> CAT[Categories]
    P --> V[Variants]
    O --> OI[OrderItems]
```

### Entities Owned by Tenant

| Entity | Relationship |
|--------|--------------|
| User | `@ManyToOne Tenant tenant` |
| Customer | `extends TenantEntity` |
| Product | `extends TenantEntity` |
| Category | `extends TenantEntity` |
| ProductVariant | `extends TenantEntity` |
| Order | `extends TenantEntity` |
| Cart | `extends TenantEntity` |
| Subscription | `extends TenantEntity` |
| Invoice | `extends TenantEntity` |
| Payment | `extends TenantEntity` |

## 🔄 Onboarding Flow

The following sequence describes how a new merchant is registered.

```mermaid
sequenceDiagram
    actor M as Merchant
    participant C as AuthController
    participant T as TenantService
    participant R as TenantRepository
    participant U as UserService

    M->>C: POST /api/v1/auth/register
    Note over M,C: {storeName, storeSlug, email, password, fullName}
    
    C->>T: registerMerchant(Request)
    T->>T: Validate unique slug
    T->>T: Validate unique name
    T->>R: save(Tenant)
    R-->>T: Saved Tenant with ID
    
    T->>U: createUser(MERCHANT role, tenantId)
    U-->>T: User created
    
    T-->>C: TenantResponse
    C-->>M: 200 OK
```

## 🔒 Tenant Isolation

The Tenant module ensures data isolation through:

1. **Unique Constraints**: `slug` and `name` are unique across all tenants
2. **Ownership**: All Users have a mandatory `tenant_id` foreign key
3. **Cascade**: Deleting a tenant should cascade to all owned entities

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Create new tenant + merchant user |
| GET | `/api/v1/tenants/{slug}` | Get tenant by slug |
| GET | `/api/v1/tenants` | List all tenants (admin only) |

## 📝 Last Updated

- **Date**: January 20, 2026

