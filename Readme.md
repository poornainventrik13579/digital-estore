# Digital E-Store

A comprehensive API-driven e-commerce platform for selling digital products such as vouchers, software licenses, courses, and other downloadable content. Built with Spring Boot and designed for scalability and security.

## Overview

Digital E-Store is an enterprise-grade Spring Boot application that provides a complete backend solution for digital product sales. The platform supports multi-tenancy, advanced payment processing, and comprehensive order management with automated workflows.

## Core Features

### Product Management
- Complete CRUD operations for digital products
- Category-based organization and hierarchy
- Product bundling with dynamic pricing
- Digital product details including file metadata
- Multi-currency pricing support

### Order Management
- Full order lifecycle from creation to completion
- Order cancellation and refund processing
- Partial refund capabilities
- Order history and tracking
- Automated order fulfillment

### Payment Processing
- Stripe integration for secure payment processing
- Support for multiple payment methods
- Webhook handling for payment events
- Automated invoice generation
- Payment reconciliation

### User Management
- Support for individual and business customers
- Multi-tenant architecture
- Role-based access control
- OAuth2 authentication and authorization
- User profile and preference management

### Advanced Features
- Discount code management with usage tracking
- Product review and rating system
- Email notification system with templates
- PDF invoice generation
- Download tracking and analytics
- Comprehensive audit logging

## Technology Stack

- **Backend Framework**: Spring Boot 3.2.5
- **Security**: Spring Security with OAuth2 Authorization Server
- **Database**: MySQL 8.0 with Flyway migrations
- **ORM**: Spring Data JPA with Hibernate
- **Payment**: Stripe API integration
- **Email**: Spring Mail with SMTP support
- **Documentation**: Swagger/OpenAPI 3.0
- **PDF Generation**: iText library
- **Template Engine**: Thymeleaf for email templates
- **Build Tool**: Maven 3.9+
- **Java Version**: JDK 17+

## Prerequisites

Before setting up the application, ensure you have:

- Java Development Kit 17 or higher
- Maven 3.6+ (or use the included Maven wrapper)
- MySQL 8.0+ database server
- SMTP server access for email functionality
- Active Stripe account for payment processing
- Git for version control

## Database Setup

1. Create a new MySQL database:
```sql
CREATE DATABASE digital_estore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Create a database user (optional but recommended):
```sql
CREATE USER 'estore_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON digital_estore.* TO 'estore_user'@'localhost';
FLUSH PRIVILEGES;
```

## Application Configuration

The application uses environment variables for sensitive configuration. Set the following:

### Database Configuration
```bash
export DB_URL="jdbc:mysql://localhost:3306/digital_estore"
export DB_USERNAME="estore_user"
export DB_PASSWORD="secure_password"
```

### Email Configuration
```bash
export EMAIL_USERNAME="your_email@domain.com"
export EMAIL_PASSWORD="your_app_password"
export EMAIL_FROM="noreply@yourdomain.com"
```

### Stripe Configuration
```bash
export STRIPE_API_KEY="sk_test_your_stripe_api_key"
export STRIPE_WEBHOOK_SECRET="whsec_your_webhook_secret"
```

Alternatively, create an `application-local.properties` file for local development (ensure it's git-ignored).

## Installation and Setup

### 1. Clone the Repository
```bash
git clone https://github.com/inventrik/digital-estore.git
cd digital-estore
```

### 2. Database Setup
Follow the database setup instructions in the Database Setup section above.

### 3. Configure Environment Variables
Set the required environment variables as described in the Application Configuration section.

### 4. Install Dependencies and Run
```bash
# Using Maven wrapper (recommended)
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

For Windows systems:
```cmd
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

### 5. Database Migration
The application uses Flyway for database migrations. Migrations will run automatically on startup, creating all necessary tables and sample data.

## API Documentation

The application provides comprehensive API documentation through Swagger UI:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Specification**: http://localhost:8080/v3/api-docs

The documentation includes:
- All available endpoints with request/response schemas
- Authentication requirements
- Example requests and responses
- Interactive API testing interface

## Authentication and Security

### Default Admin Account
The application creates a default admin account on first startup:
- **Username**: `admin`
- **Password**: `admin`

**Important**: Change the default password immediately after first login for security.

### OAuth2 Implementation
The application implements OAuth2 with JWT tokens supporting:
- Authorization Code Grant
- Client Credentials Grant
- Refresh Token flow

### API Security
- All API endpoints require valid JWT tokens
- Role-based access control (RBAC)
- Request rate limiting
- CORS configuration for cross-origin requests

## Project Architecture

The application follows clean architecture principles with clear separation of concerns:

```
src/main/java/com/inventrik/digitalestore/
├── api/                 # REST controllers and API layer
├── config/              # Configuration classes
├── domain/              # Entity classes and domain models
├── dto/                 # Data Transfer Objects
├── exception/           # Custom exceptions and error handling
├── repository/          # Data access layer
├── security/            # Security configurations
├── service/             # Business logic layer
├── util/                # Utility classes
└── DigitalEstoreApplication.java
```

### Key Components

**Controllers**: Handle HTTP requests and responses, input validation, and route to appropriate services.

**Services**: Contain business logic, transaction management, and coordinate between repositories.

**Repositories**: Data access layer using Spring Data JPA for database operations.

**Entities**: Domain models representing business objects and database tables.

**DTOs**: Data transfer objects for API communication and data transformation.

## Available APIs

### Core APIs
- **Products**: `/api/v1/products` - Product management
- **Categories**: `/api/v1/categories` - Category management
- **Orders**: `/api/v1/orders` - Order processing
- **Users**: `/api/v1/users` - User management
- **Payments**: `/api/v1/payments` - Payment processing

### Advanced Features
- **Bundles**: `/api/v1/bundles` - Product bundle management
- **Discounts**: `/api/v1/discounts` - Discount code management
- **Reviews**: `/api/v1/reviews` - Product review system
- **Downloads**: `/api/v1/downloads` - Digital download tracking
- **Currencies**: `/api/v1/currencies` - Multi-currency support

### System APIs
- **Authentication**: `/oauth2/token` - Token management
- **Webhooks**: `/api/webhooks/stripe` - Payment webhooks
- **Health**: `/actuator/health` - Application health check

## Deployment

### Production Configuration
For production deployment, ensure:

1. Set appropriate database connection pooling
2. Configure SSL/TLS for secure communication
3. Set up proper logging and monitoring
4. Use production-grade SMTP service
5. Configure Stripe production keys
6. Set up backup and disaster recovery

### Environment-Specific Profiles
The application supports multiple Spring profiles:
- `dev` - Development environment
- `test` - Testing environment  
- `prod` - Production environment

Use profiles with:
```bash
java -jar digital-estore.jar --spring.profiles.active=prod
```

## Pending Features

### Multi-Language Support
The application currently supports single-language operations. Multi-language support is planned with the following approach:

- **Database Structure**: ProductTranslations and CategoryTranslations tables for storing localized content
- **Workflow**: Tenants create products in default language, admins add translations manually
- **API Enhancement**: Language parameter support for all product and category endpoints
- **Fallback Logic**: Automatic fallback to default language when translations are unavailable
- **Supported Languages**: Configurable language support with ISO language codes

### Implementation Status
- **Completed Features**: All core e-commerce functionality, payment processing, multi-currency support, product bundles, discount system, review system
- **In Progress**: Multi-language support implementation

## Support and Documentation

For additional information:
- Review the API documentation at `/swagger-ui.html`
- Check the `docs/` directory for detailed technical documentation
- Refer to the configuration guide in `README_CONFIGURATION.md`
