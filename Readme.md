# Digital E-Store

A comprehensive API-driven e-commerce platform for selling digital products such as vouchers, software, and other downloadable content.

## Overview

Digital E-Store is a Spring Boot application that provides a complete backend solution for digital product sales, including:

- Product management
- User management
- Order processing
- Secure payment integration with Stripe
- Email notifications
- PDF invoice generation
- Multi-tenancy support

## Features

- **Product Management**: Create, read, update, and delete digital products
- **Category Management**: Organize products into categories
- **User Management**: Support for both individual and business customers
- **Order Processing**: Complete order lifecycle management
- **Payment Processing**: Integrated with Stripe for secure payments
- **Email Notifications**: Automated emails for order confirmations, downloads, etc.
- **PDF Invoices**: Automatic generation of professional PDF invoices
- **OAuth2 Security**: Role-based access control with token-based authentication
- **Multi-tenancy**: Support for multiple stores on a single platform

## Technologies

- Java 17
- Spring Boot 3.2.5
- Spring Security with OAuth2
- Spring Data JPA
- MySQL
- Stripe API for payments
- iText PDF for invoice generation
- Thymeleaf for email templates
- Swagger/OpenAPI for API documentation

## Prerequisites

- JDK 17 or higher
- Maven 3.6+ (or use the included Maven wrapper)
- MySQL 8.0+ database
- SMTP server access for email functionality
- Stripe account for payment processing

## Configuration

Before running the application, you need to configure the following in your `application.properties` file:

### Database Configuration
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/digital_estore
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### Email Configuration
```properties
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=your_email@example.com
spring.mail.password=your_email_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
email.from=noreply@yourdomain.com
email.sender-name=Digital E-Store
```

### Stripe Configuration
```properties
stripe.api.key=your_stripe_api_key
stripe.webhook.secret=your_stripe_webhook_secret
```

## Running the Application

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/digital-estore.git
   cd digital-estore
   ```

2. Create a MySQL database:
   ```sql
   CREATE DATABASE digital_estore;
   ```

3. Configure the application as described in the Configuration section

4. Run the application:

   **On Linux/Mac**:
   ```bash
   ./mvnw spring-boot:run
   ```
   
   **On Windows** (using Maven wrapper):
   ```bash
   mvnw.cmd spring-boot:run
   ```
   
   **On Windows** (if Maven is installed):
   ```bash
   mvn spring-boot:run
   ```

## API Documentation

Once the application is running, you can access the Swagger UI to explore and test the APIs:

```
http://localhost:8080/swagger-ui.html
```

## Default Admin Account

The application sets up a default admin account on first run:

- Username: `admin`
- Password: `admin`

It's recommended to change the password after first login.

## Project Structure

The application follows a standard multi-layered architecture:

- `api` - REST controllers
- `config` - Configuration classes
- `domain` - Entity classes
- `dto` - Data Transfer Objects
- `exception` - Custom exceptions and error handling
- `repository` - Data access interfaces
- `security` - Security configurations and utilities
- `service` - Business logic

## Authentication

The application uses OAuth2 for authentication with the following grant types:
- Authorization Code
- Password
- Refresh Token

Two clients are pre-configured:
- Mobile client (`mobile-client`)
- Web client (`web-client`)
