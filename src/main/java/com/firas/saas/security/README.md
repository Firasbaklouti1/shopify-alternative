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
