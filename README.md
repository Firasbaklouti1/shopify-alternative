# SaaS E-Commerce Platform (Shopify Alternative)

## 🚀 Overview
A scalable, multi-tenant e-commerce platform built with **Spring Boot** and **Java**. This project allows merchants to create independent online stores, manage products, orders, and customers in an isolated environment.

## 🛠️ Tech Stack
- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Database**: MySQL 8.x
- **Security**: Spring Security + JWT
- **Build Tool**: Maven

## 📦 Modules
- **Tenant Management**: Onboarding and merchant isolation.
- **Identity & Access**: Role-based access (`ADMIN`, `MERCHANT`, `STAFF`, `CUSTOMER`).
- **Product Catalog**: Categories, variants, and inventory tracking.
- **Subscription Engine**: Dynamic plans and billing system (Strategy Pattern).
- **Billing**: Invoicing and payment processing.

## 🏃‍♂️ Getting Started

### Prerequisites
- Java 17 JDK
- MySQL Server
- Maven

### Installation
1.  **Clone the repository**:
    ```bash
    git clone https://github.com/your-username/shopify-alternative.git
    cd shopify-alternative
    ```
2.  **Configure Database**:
    Update `src/main/resources/application.yml` with your MySQL credentials.
3.  **Run the Application**:
    ```bash
    mvn spring-boot:run
    ```
4.  **Verify**:
    The API will be available at `http://localhost:8080/api/v1`.

## 🧪 Testing
The project includes comprehensive **HTTP Client** tests located in `src/test/java/com/firas/saas/`:
- `e2e_scenario.http`: Full End-to-End flow (Admin -> Merchant -> Customer).
- `admin.http`: Admin-specific workflows.
- `billing.http`: Subscription and payment tests.

## 🤝 Contribution
Contributions are welcome! Please follow these steps:
1.  Fork the repository.
2.  Create a feature branch (`git checkout -b feature/AmazingFeature`).
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the branch (`git push origin feature/AmazingFeature`).
5.  Open a Pull Request.

## 📚 Documentation
For detailed architectural decisions and skill guides, refer to:
- `.agent/skills/spring-boot`: Spring Boot Best Practices.
- `PROJECT_HANDOFF.md`: Context for AI assistants (Local only).

## 📄 License
Distributed under the MIT License. See `LICENSE` for more information.
