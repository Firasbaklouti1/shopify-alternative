# Security Module Documentation

## 📝 Overview
Handles authentication (Login) and authorization (Role-based access). Uses JWT (JSON Web Tokens) for stateless security.

## 🔑 Key Components

- **JwtUtils**: Generates and validates JWT tokens.
- **UserDetailsServiceImpl**: Loads user data from the database.
- **AuthEntryPointJwt**: Handles 401 Unauthorized errors.
- **AuthTokenFilter**: Intercepts requests to validate the `Authorization: Bearer <token>` header.

## 🛡️ Authentication Flow

```mermaid
sequenceDiagram
    actor U as User
    participant F as AuthTokenFilter
    participant C as AuthController
    participant M as AuthenticationManager
    participant J as JwtUtils

    U->>C: POST /api/v1/auth/login (Email, Pass)
    C->>M: authenticate()
    M-->>C: UserDetails (Authenticated)
    C->>J: generateJwtToken(UserDetails)
    J-->>C: JWT String
    C-->>U: Return JSON { "token": "..." }

    Note over U, F: Subsequent Requests
    U->>F: Request + Header [Authorization: Bearer ...]
    F->>J: validateJwtToken()
    F->>F: Set SecurityContext
    F->>C: Proceed to Controller
```

## 🔄 API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/auth/register` | Register new merchant (creates Tenant + User) | Public |
| POST | `/api/v1/auth/login` | Login for any user | Public |
| POST | `/api/v1/auth/customer/{storeSlug}/register` | Customer self-registration on a store | Public |

## 👤 Customer Registration Flow (Added January 2026)

Customers can self-register on a specific store:

```mermaid
sequenceDiagram
    actor C as Customer
    participant A as AuthController
    participant T as TenantRepository
    participant U as UserRepository
    participant CR as CustomerRepository

    C->>A: POST /api/v1/auth/customer/{storeSlug}/register
    A->>T: findBySlug(storeSlug)
    T-->>A: Tenant
    A->>U: save(User with Role.CUSTOMER)
    A->>CR: save(Customer CRM record)
    A-->>C: JWT Token (auto-login)
```

**Request Body:**
```json
{
  "email": "customer@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "555-1234"
}
```

**Response:** Returns JWT token for immediate login (201 Created).


