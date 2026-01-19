# User Module Documentation

## 📝 Overview
Manages all users in the system. Users are scoped to a specific `Tenant` (except System Admins who might manage the platform).

## 🔑 Key Entities

### User
- `email`: Login Identifier.
- `password`: BCrypt hashed.
- `role`: Enum (`ADMIN`, `MERCHANT`, `STAFF`, `CUSTOMER`).
- `tenant`: Reference to the Tenant (Store).

```mermaid
erDiagram
    TENANT ||--o{ USER : "contains"
    USER {
        Long id
        String email
        String password
        Role role
    }
```

## ⚙️ Role Hierarchy
- **ADMIN**: System Administrator (Can manage plans).
- **MERCHANT**: Owner of a Tenant (Can manage staff, products, settings).
- **STAFF**: Employee of a Tenant (Limited access).
- **CUSTOMER**: Shopper account (Can view orders).

## 🔄 Staff Creation Flow (Merchant -> Staff)

```mermaid
sequenceDiagram
    actor M as Merchant
    participant C as UserController
    participant S as UserService
    participant D as DB

    M->>C: POST /api/v1/users (Staff Info)
    Note right of C: Controller injects TenantID from Merchant's Context
    C->>S: createUser(Request)
    S->>D: save(User with Role=STAFF)
    D-->>S: User Entity
    S-->>M: 201 Created
```
