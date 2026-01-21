# Order Sync & Auto-Fulfillment App

This is a **separate Spring Boot application** that validates the App Platform by acting as a real third-party app.

## Purpose

This app demonstrates and validates:
1. **Webhook Reception** - Receiving events from the platform (ORDER_CREATED, ORDER_PAID, etc.)
2. **API Calls with Scoped Tokens** - Calling platform APIs using app-scoped access tokens
3. **Scope Enforcement** - Verifying that missing scopes are rejected
4. **Uninstall Flow** - Confirming that token revocation works correctly

## Architecture

```
┌─────────────────────────────┐     ┌─────────────────────────────┐
│     Main Platform           │     │    Order Sync App           │
│     (Port 8080)             │     │    (Port 8081)              │
│                             │     │                             │
│  ┌─────────────────────┐    │     │  ┌─────────────────────┐    │
│  │ App Platform        │────┼─────┼──│ Webhook Controller  │    │
│  │ - App Registration  │    │     │  └─────────────────────┘    │
│  │ - Installation      │    │     │            │                │
│  │ - Token Management  │    │     │            ▼                │
│  └─────────────────────┘    │     │  ┌─────────────────────┐    │
│            │                │     │  │ Order Sync Service  │    │
│            ▼                │     │  └─────────────────────┘    │
│  ┌─────────────────────┐    │     │            │                │
│  │ App API             │◄───┼─────┼────────────┘                │
│  │ (/api/v1/app/*)     │    │     │  (calls with access token)  │
│  └─────────────────────┘    │     │                             │
│                             │     │  ┌─────────────────────┐    │
│  MySQL Database             │     │  │ H2 Database         │    │
│  (shopify_alt)              │     │  │ (in-memory)         │    │
└─────────────────────────────┘     └─────────────────────────────┘
```

## Key Features

### 1. Webhook Handling
- Receives webhooks at `POST /webhooks`
- Validates signatures (HMAC-SHA256)
- Stores all events for audit
- Processes ORDER_CREATED, ORDER_PAID, APP_INSTALLED, APP_UNINSTALLED

### 2. Order Synchronization
- Fetches full order details from platform API
- Syncs to external fulfillment (simulated)
- Updates platform with fulfillment status

### 3. Token Management
- Stores access tokens per tenant
- Validates tokens before API calls
- Removes tokens on uninstall

## Running the App

### Prerequisites
- Java 21+
- Maven 3.8+
- Main platform running on port 8080

### Start the App

```bash
cd order-sync-app
mvn spring-boot:run
```

The app will start on **port 8081**.

### Verify It's Running

```bash
curl http://localhost:8081/api/health
```

## API Endpoints

### Webhook Endpoint
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/webhooks` | Receive webhooks from platform |

### Management Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/config/token` | Configure access token for a tenant |
| GET | `/api/config/verify/{tenantId}` | Verify token is valid |
| GET | `/api/config/tenants` | List configured tenants |
| GET | `/api/orders` | Get all synced orders |
| GET | `/api/orders/platform/{id}` | Get synced order by platform ID |
| GET | `/api/webhooks` | Get all received webhooks |
| GET | `/api/health` | Health check |
| GET | `/api/status` | Full status summary |

## End-to-End Test Flow

Use `e2e-test.http` to run the complete validation:

1. **Setup** - Login with seeded admin (`admin@saas.com`), register merchant
2. **App Registration** - Create and publish the app (ADMIN role required)
3. **App Installation** - Merchant installs the app with scopes
4. **Configure Token** - Store access token in Order Sync App
5. **Create Test Data** - Category, product (with variants), customer
6. **Create Order** - Add to cart and checkout, triggers webhook
7. **Verify Webhook** - Confirm webhook received and processed
8. **API Calls** - App fetches orders/customers from platform
9. **Scope Enforcement** - Verify missing scopes are rejected (READ_PRODUCTS → 400)
10. **Update Order** - App updates order status using WRITE_ORDERS
11. **Uninstall** - Verify token revocation (returns 401)

## Database

This app uses **H2 in-memory database** - completely separate from the main platform's MySQL.

Access H2 console at: `http://localhost:8081/h2-console`
- JDBC URL: `jdbc:h2:mem:ordersyncdb`
- Username: `sa`
- Password: (empty)

## Scopes Used

This app requests:
- `READ_ORDERS` - Fetch order details
- `WRITE_ORDERS` - Update order status
- `READ_CUSTOMERS` - Fetch customer details

## Success Criteria

The App Platform is validated when:

✅ App is registered and published  
✅ Merchant installs the app  
✅ Webhooks are received on port 8081  
✅ App successfully calls platform APIs  
✅ Scope violations return 403 Forbidden  
✅ Uninstall revokes all tokens  
✅ Revoked tokens return 401 Unauthorized  

## Important Notes

1. **Complete Isolation** - This app has NO access to platform's internal classes or database
2. **HTTP-Only Communication** - All interaction via REST APIs and webhooks
3. **Separate Maven Project** - Own pom.xml, dependencies, and build
4. **Different Database** - Uses H2, not MySQL
