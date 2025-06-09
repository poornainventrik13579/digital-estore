# Digital E-Store API

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

- Backend Framework: Spring Boot 3.2.5
- Security: Spring Security with OAuth2 Authorization Server
- Database: MySQL 8.0 with Flyway migrations
- ORM: Spring Data JPA with Hibernate
- Payment: Stripe API integration
- Email: Spring Mail with SMTP support
- Documentation: Swagger/OpenAPI 3.0
- PDF Generation: iText library
- Template Engine: Thymeleaf for email templates
- Build Tool: Maven 3.9+
- Java Version: JDK 17+

## Prerequisites

Before setting up the application, ensure you have:

- Java Development Kit 17 or higher
- Maven 3.6+ or use the included Maven wrapper
- MySQL 8.0+ database server
- SMTP server access for email functionality
- Active Stripe account for payment processing
- Git for version control

## Quick Start Guide

### Database Setup

Step 1: Create a new MySQL database
```sql
CREATE DATABASE digital_estore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Step 2: Create a database user (recommended for security)
```sql
CREATE USER 'estore_user'@'localhost' IDENTIFIED BY 'secure_password_123';
GRANT ALL PRIVILEGES ON digital_estore.* TO 'estore_user'@'localhost';
FLUSH PRIVILEGES;
```

### Installation Steps

Step 1: Clone the repository
```bash
git clone https://github.com/inventrik/digital-estore.git
cd digital-estore
```

Step 2: Configure database connection

Open the file `src/main/resources/application.properties` and update these lines with your database information:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/digital_estore
spring.datasource.username=estore_user
spring.datasource.password=secure_password_123
```

Step 3: Install dependencies and run the application
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

Step 4: Verify the installation

Once the application starts successfully, you should see a message like:
```
Started DigitalEstoreApplication in X.XXX seconds
```

Visit http://localhost:8080/swagger-ui.html to see the API documentation.

## Authentication and Security Guide

### Understanding API Authentication

API authentication is like showing your ID card before entering a secure building. Every time you want to use the API, you need to prove who you are by providing a special "access token" which acts like a digital ID card.

### How Authentication Works in This API

The API uses OAuth2 authentication system. This is a secure industry-standard method for API access. Here's what you need to know:

- You need to get an "access token" before making any API calls
- This token expires after one hour for security reasons
- You must include this token in every API request you make

### Step by Step: Getting Your Access Token

#### Method 1: Using Postman (Recommended for Beginners)

Step 1: Import the Postman Collection
- Download the file `Digital-EStore-Postman-Collection.json` from this project
- Open Postman application
- Click "Import" and select the downloaded file

Step 2: Get your access token
- In the imported collection, find the folder named "01. Authentication"
- Click on the request called "Get OAuth2 Token"
- Click the "Send" button
- You will receive a response containing your `access_token`

Step 3: Copy your token
- From the response, copy the value of `access_token`
- This token will be automatically saved for other requests in the collection

#### Method 2: Manual API Request

If you prefer to use other tools like Insomnia, curl, or any other API client:

Make a POST request with these details:
```
URL: http://localhost:8080/oauth2/token
Method: POST
Headers: 
  Content-Type: application/x-www-form-urlencoded
  Authorization: Basic d2ViLWNsaWVudDp3ZWItc2VjcmV0

Body (form-encoded):
  grant_type: client_credentials
  scope: read write
```

You will receive a response like this:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "read write"
}
```

### Using Your Access Token

For every API request you make after getting the token, you must include this header:

```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

Replace `YOUR_ACCESS_TOKEN_HERE` with the actual token you received.

For example:
```
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
```

### Default System Credentials

The application comes with these pre-configured settings:

**OAuth2 Client Credentials:**
- Client ID: web-client
- Client Secret: web-secret
- Swagger Client ID: swagger-client
- Swagger Client Secret: swagger-secret

**Default Admin Account:**
- Username: admin
- Password: admin
- IMPORTANT: Change this password immediately when using in production

### Testing Your Authentication

#### Using Swagger UI (Built-in Testing Tool)

Step 1: Open your web browser and go to: http://localhost:8080/swagger-ui.html

Step 2: Click the "Authorize" button at the top of the page

Step 3: Enter these credentials:
- Client ID: swagger-client
- Client Secret: swagger-secret

Step 4: Click "Authorize" and then "Close"

Step 5: You can now test any API endpoint by clicking "Try it out"

#### Using the Postman Collection

Step 1: Import the provided Postman collection as described above

Step 2: Run the "Get OAuth2 Token" request first

Step 3: All other requests in the collection will automatically use your token

### Security Features

The API includes several security features to protect your data:

- All endpoints require valid authentication tokens
- Tokens automatically expire to prevent unauthorized access
- Different users have different permission levels
- All sensitive data is encrypted
- Rate limiting prevents system abuse
- Comprehensive audit logging tracks all activities

## API Endpoints Guide

### Authentication Endpoints
```
POST /oauth2/token              # Get your access token
POST /api/v1/auth/signup        # Register a new user account
```

### Category Management Endpoints
```
GET    /api/v1/tenants/1/categories     # List all categories
POST   /api/v1/tenants/1/categories     # Create a new category
GET    /api/v1/tenants/1/categories/1   # Get details of specific category
PUT    /api/v1/tenants/1/categories/1   # Update category information
DELETE /api/v1/tenants/1/categories/1   # Delete a category
```

### Product Management Endpoints
```
GET    /api/v1/tenants/1/products       # List all products
POST   /api/v1/tenants/1/products       # Create a new product
GET    /api/v1/tenants/1/products/1     # Get details of specific product
PUT    /api/v1/tenants/1/products/1     # Update product information
DELETE /api/v1/tenants/1/products/1     # Delete a product
GET    /api/v1/tenants/1/products/active # Get only active products
```

### User Management Endpoints
```
GET    /api/v1/tenants/1/users          # List all users
POST   /api/v1/tenants/1/users          # Create a new user
GET    /api/v1/tenants/1/users/1        # Get details of specific user
PUT    /api/v1/tenants/1/users/1        # Update user information
DELETE /api/v1/tenants/1/users/1        # Delete a user
```

### Order Management Endpoints
```
GET    /api/v1/tenants/1/orders         # List all orders
POST   /api/v1/tenants/1/orders         # Create a new order
GET    /api/v1/tenants/1/orders/1       # Get details of specific order
PUT    /api/v1/tenants/1/orders/1       # Update order information
DELETE /api/v1/tenants/1/orders/1       # Delete an order
POST   /api/v1/tenants/1/orders/1/cancel # Cancel an order
```

### Payment Management Endpoints
```
GET    /api/v1/tenants/1/payments       # List all payments
POST   /api/v1/tenants/1/payments       # Process a new payment
GET    /api/v1/tenants/1/payments/1     # Get details of specific payment
PUT    /api/v1/tenants/1/payments/1     # Update payment information
POST   /api/v1/tenants/1/payments/1/refund # Process a refund
```

Note: Replace "1" in the URLs with actual IDs from your system.

## API Documentation

### Interactive Documentation
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Specification: http://localhost:8080/v3/api-docs

### What you will find in the documentation:
- Complete list of all available endpoints
- Detailed descriptions of what each endpoint does
- Examples of request and response data
- Authentication requirements for each endpoint
- Interactive testing interface where you can try the APIs
- Data validation rules and requirements

## Email Configuration

### Setting up Gmail for Email Notifications

Step 1: Enable 2-Factor Authentication on your Gmail account

Step 2: Generate an App Password
- Go to your Google Account settings
- Navigate to Security then 2-Step Verification then App passwords
- Select "Mail" as the app and generate a password

Step 3: Update the configuration file
```properties
spring.mail.username=your_gmail@gmail.com
spring.mail.password=your_generated_app_password
```

## Payment Processing Setup (Stripe)

### Creating Your Stripe Account

Step 1: Visit https://stripe.com and create an account

Step 2: Get your API keys
- Go to Dashboard then Developers then API keys
- Copy both your "Publishable key" and "Secret key"

Step 3: Update the configuration
```properties
stripe.api.key=sk_test_your_secret_key_here
```

### Setting up Webhooks for Payment Notifications

Step 1: In your Stripe Dashboard
- Go to Developers then Webhooks then "Add endpoint"
- Set Endpoint URL to: http://your-domain.com/api/webhooks/stripe
- Select the event: payment_intent.succeeded

Step 2: Copy the webhook secret
- After creating the webhook, copy the "Signing secret"

Step 3: Update your configuration
```properties
stripe.webhook.secret=whsec_your_webhook_secret_here
```

## Testing Your API

### Recommended Testing Order

1. **Authentication**: Always start by getting your access token
2. **Categories**: Create some product categories first
3. **Products**: Add products and assign them to categories
4. **Users**: Create user accounts
5. **Orders**: Create orders with the products
6. **Payments**: Process payments for the orders

### Sample API Testing Flow

Step 1: Get authentication token
Step 2: Create a category (e.g., "Software Licenses")
Step 3: Create a product in that category
Step 4: Create a user account
Step 5: Create an order for that user with the product
Step 6: Process payment for the order

## Troubleshooting Common Issues

### Application Won't Start

**Check these things:**
- Is Java 17 or higher installed on your system?
- Is MySQL running and accessible?
- Does the database exist?
- Is port 8080 available (not used by another application)?
- Are the database credentials correct in application.properties?

### Database Connection Problems

**Try these solutions:**
- Verify MySQL service is running
- Check your username and password in the configuration file
- Make sure the database name exists
- Check if firewall is blocking the database connection

### Authentication Not Working

**Common solutions:**
- Verify you're using the correct client credentials
- Make sure you include "Bearer " before your token
- Check if your token has expired (get a new one)
- Verify the API endpoint URL is correct

### Emails Not Sending

**Check these settings:**
- Are your SMTP server details correct?
- Is your email password correct (use app password for Gmail)?

### Useful Resources

- Interactive API Documentation: http://localhost:8080/swagger-ui.html
- Postman Collection for testing: Digital-EStore-Postman-Collection.json
- Configuration Examples: README_CONFIGURATION.md
- Email Templates: src/main/resources/templates/email/

## Project Structure

```
src/main/java/com/inventrik/digitalestore/
├── api/                 # REST controllers that handle HTTP requests
├── config/              # Configuration classes for the application
├── domain/              # Database entity classes
├── dto/                 # Data transfer objects for API communication
├── exception/           # Custom error handling classes
├── repository/          # Database access layer
├── security/            # Authentication and authorization logic
├── service/             # Business logic implementation
└── util/               # Helper utility classes
```

## Deployment Considerations

### Before Deploying to Production

- Change the default admin password from "admin"
- Use production Stripe API keys instead of test keys
- Set up SSL/HTTPS for secure communication
- Configure a production-grade database with proper backup
- Set up monitoring and logging systems
- Test all critical user flows thoroughly

### Running with Different Environments

You can run the application with different configuration profiles:

```bash
# For development environment
./mvnw spring-boot:run -Dspring.profiles.active=dev

# For production environment
java -jar digital-estore.jar --spring.profiles.active=prod
```

## Future Enhancements

**Multi-Language Support**
- Support for multiple languages in product descriptions
- Localized email templates
- API parameter for language selection
- Automatic language detection based on user preferences

### Current Status

All core e-commerce functionality is partially complete:
- Product and category management
- User account management
- Order processing and tracking
- Payment processing with Stripe
- Email notifications
- PDF invoice generation
- Discount code system
- Product review system

