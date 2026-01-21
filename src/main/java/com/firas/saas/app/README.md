# App Platform Module

This module implements the App Store and App Platform functionality, enabling third-party apps to integrate with merchant stores.

## Overview

The App Platform allows:
- **Developers** (via ADMIN role in Phase 1) to register apps
- **Merchants** to install apps into their stores
- **Apps** to receive webhooks and call platform APIs with scoped access

## Entities

### App (Global - not tenant-scoped)
Represents an app registered in the App Store.

| Field | Description |
|-------|-------------|
| name | Display name of the app |
| description | What the app does |
| developerName | Developer/organization name |
| clientId | Public identifier (UUID format) |
| clientSecretHash | Hashed secret for authentication |
| webhookUrl | URL where app receives webhooks |
| declaredScopes | Scopes the app requests |
| status | DRAFT, PUBLISHED, or SUSPENDED |

### AppInstallation (Tenant-scoped)
Represents an app installed in a specific tenant's store.

| Field | Description |
|-------|-------------|
| app | Reference to the App |
| grantedScopes | Scopes granted by merchant |
| status | ACTIVE or REVOKED |
| installedByUserId | User who installed the app |

### AppAccessToken (Tenant-scoped)
Access token for app API calls.

| Field | Description |
|-------|-------------|
| tokenValue | UUID token value |
| installation | Reference to installation |
| scopes | Granted scopes |
| expiresAt | Token expiration (30 days) |
| revoked | Whether token is revoked |

## Scopes

| Scope | Description | Allowed Events |
|-------|-------------|----------------|
| READ_ORDERS | Read order data | ORDER_CREATED, ORDER_UPDATED, ORDER_PAID, ORDER_FULFILLED, ORDER_CANCELLED |
| WRITE_ORDERS | Update orders | Same as READ_ORDERS |
| READ_PRODUCTS | Read product data | PRODUCT_CREATED, PRODUCT_UPDATED, PRODUCT_DELETED, INVENTORY_UPDATED, INVENTORY_LOW |
| WRITE_PRODUCTS | Update products | Same as READ_PRODUCTS |
| READ_CUSTOMERS | Read customer data | CUSTOMER_CREATED, CUSTOMER_UPDATED |
| MANAGE_WEBHOOKS | Manage webhooks | None |

## API Endpoints

### App Management (ADMIN only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/apps` | Create new app |
| GET | `/api/v1/apps` | List all apps |
| GET | `/api/v1/apps/{id}` | Get app by ID |
| PUT | `/api/v1/apps/{id}` | Update app |
| PATCH | `/api/v1/apps/{id}/publish` | Publish app |
| PATCH | `/api/v1/apps/{id}/suspend` | Suspend app |
| PATCH | `/api/v1/apps/{id}/unpublish` | Unpublish app |
| POST | `/api/v1/apps/{id}/regenerate-secret` | Regenerate client secret |
| DELETE | `/api/v1/apps/{id}` | Delete app |
| GET | `/api/v1/apps/scopes` | List available scopes |

### App Installation (MERCHANT)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/app-installations/install` | Install app |
| DELETE | `/api/v1/app-installations/{id}` | Uninstall app |
| GET | `/api/v1/app-installations` | List installed apps |
| GET | `/api/v1/app-installations/active` | List active installations |
| GET | `/api/v1/app-installations/{id}` | Get installation details |
| POST | `/api/v1/app-installations/{id}/rotate-token` | Rotate access token |
| GET | `/api/v1/app-installations/{id}/tokens` | Get token info |

### App API (Apps call these with their access tokens)

| Method | Endpoint | Required Scope | Description |
|--------|----------|----------------|-------------|
| GET | `/api/v1/app/orders` | READ_ORDERS | Get all orders |
| GET | `/api/v1/app/orders/{id}` | READ_ORDERS | Get order by ID |
| PATCH | `/api/v1/app/orders/{id}/status` | WRITE_ORDERS | Update order status |
| GET | `/api/v1/app/products` | READ_PRODUCTS | Get all products |
| GET | `/api/v1/app/products/{id}` | READ_PRODUCTS | Get product by ID |
| GET | `/api/v1/app/customers` | READ_CUSTOMERS | Get all customers |
| GET | `/api/v1/app/customers/{id}` | READ_CUSTOMERS | Get customer by ID |
| GET | `/api/v1/app/me` | None | Get app info |

## Authentication Flow

### 1. App Registration (ADMIN)
```http
POST /api/v1/apps
{
  "name": "Order Sync App",
  "description": "Syncs orders to external system",
  "developerName": "My Company",
  "webhookUrl": "https://myapp.com/webhooks",
  "declaredScopes": ["READ_ORDERS", "WRITE_ORDERS", "READ_CUSTOMERS"]
}
```

Response includes `clientId` and `clientSecret` (shown only once).

### 2. Publish App (ADMIN)
```http
PATCH /api/v1/apps/{id}/publish
```

### 3. Install App (MERCHANT)
```http
POST /api/v1/app-installations/install
{
  "clientId": "app_abc123...",
  "clientSecret": "secret_xyz...",
  "grantedScopes": ["READ_ORDERS", "WRITE_ORDERS"]
}
```

Response includes `accessToken` for API calls.

### 4. App Calls Platform API
```http
GET /api/v1/app/orders
Authorization: Bearer <access_token>
```

## Webhook Delivery

When events occur (e.g., ORDER_CREATED), the platform:

1. Checks all active app installations for the tenant
2. For each app with the required scope (e.g., READ_ORDERS for ORDER_CREATED)
3. Delivers the webhook to the app's `webhookUrl`
4. Includes HMAC signature for verification

### Webhook Headers
```
X-Webhook-Event: ORDER_CREATED
X-Webhook-Signature: t=1234567890,v1=abc123...
X-Webhook-Event-Id: app_evt_abc123
X-App-Client-Id: app_xyz...
```

### Always-Delivered Events
- `APP_INSTALLED` - Delivered regardless of scopes
- `APP_UNINSTALLED` - Delivered regardless of scopes

## Security

### Token Validation
- Tokens expire after 30 days
- Revoked tokens are rejected immediately
- Uninstalled apps have all tokens revoked

### Scope Enforcement
- Scope checked at API level via `@RequiresScope` annotation
- Missing scope returns 403 Forbidden
- Webhook delivery filtered by scope

### Tenant Isolation
- Apps can only access data for the tenant they're installed in
- `tenantId` is embedded in the token and validated on every request
