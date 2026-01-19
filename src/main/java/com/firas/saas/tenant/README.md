# Tenant Module Documentation

## 📝 Overview
The Tenant module is the heart of the multi-tenancy architecture. It handles the onboarding of new merchants (creating "Stores").

## 🔑 Key Entities

### Tenant
Represents a Merchant's Store.
- `id`: Primary Key
- `storeName`: Display name of the store.
- `storeSlug`: Unique URL identifier (e.g., `store-xyz`).
- `ownerEmail`: Email of the merchant owner.

```mermaid
classDiagram
    class Tenant {
        +Long id
        +String storeName
        +String storeSlug
        +String ownerEmail
        +boolean active
        +LocalDateTime createdAt
    }
```

## 🔄 Onboarding Flow
The following sequence describes how a new merchant is registered.

```mermaid
sequenceDiagram
    actor M as Merchant
    participant C as AuthController
    participant S as TenantService
    participant R as TenantRepository
    participant U as UserService

    M->>C: POST /api/v1/auth/register (Store Info + User Info)
    C->>S: registerMerchant(Request)
    S->>S: Check if Slug Exists
    S->>R: save(Tenant)
    S->>U: createUser(Admin User for Tenant)
    S-->>C: Return TenantResponse
    C-->>M: 200 OK
```
