# Digital E-Store API Testing Guide

## Table of Contents
1. [Quick Start](#quick-start)
2. [Environment Setup](#environment-setup)
3. [Complete API Reference](#complete-api-reference)
4. [Detailed Testing Flows](#detailed-testing-flows)
5. [Code Examples](#code-examples)
6. [Troubleshooting](#troubleshooting)

---

## Quick Start

### Prerequisites
- Backend API running at `http://localhost:8080`
- MySQL database running
- Redis server running

### Import Collections
1. Open Postman
2. Click "Import" and import all 3 collection files:
   - `1-Platform-Admin.postman_collection.json`
   - `2-Tenant-Admin.postman_collection.json`
   - `3-User-Customer.postman_collection.json`

### Initial Environment Variables
Set these in Postman environment:
| Variable | Value | Description |
|----------|---------|-------------|
| `base_url` | `http://localhost:8080` | API base URL |

---

## Environment Setup

### Create a New Environment
1. In Postman, click on the environment dropdown (top right)
2. Select "Manage Environments"
3. Click "Add"
4. Name it: `Digital E-Store Dev`
5. Add initial variables:
   ```
   base_url: http://localhost:8080
   ```
6. Save and select the environment

---

## Complete API Reference

### Platform Admin Endpoints

| Method | Endpoint | Purpose | Auth |
|---------|-----------|---------|------|
| POST | `/api/v1/auth/platform/login` | Login as platform admin | x-www-form-urlencoded |
| POST | `/api/v1/auth/platform/forgot-password` | Reset platform admin password | x-www-form-urlencoded |
| GET | `/api/v1/auth/platform/me` | Get current platform admin | Bearer |
| GET | `/api/v1/tenants` | List all tenants | Bearer |
| GET | `/api/v1/tenants/{id}` | Get tenant details | Bearer |
| POST | `/api/v1/tenants` | Create new tenant | Bearer |
| PUT | `/api/v1/tenants/{id}` | Update tenant | Bearer |
| DELETE | `/api/v1/tenants/{id}` | Delete tenant | Bearer |

---

### Tenant Admin Auth Endpoints

| Method | Endpoint | Purpose | Auth |
|---------|-----------|---------|------|
| POST | `/api/v1/auth/tenant/signup` | Create tenant and admin | x-www-form-urlencoded |
| POST | `/api/v1/auth/tenant/login` | Login as tenant admin | x-www-form-urlencoded |
| POST | `/api/v1/auth/tenant/forgot-password` | Reset tenant admin password | x-www-form-urlencoded |
| GET | `/api/v1/auth/tenant/me` | Get current tenant admin | Bearer |

**TenantSignupRequest Fields:**
```java
shopName (String) - Required
shopEmail (String) - Required
shopPhone (String) - Required
shopLogo (String) - Required
domainName (String) - Required
adminUsername (String) - Required
adminPassword (String) - Required
adminEmail (String) - Required
subdomain (String) - Optional
countryRegion (String) - Optional
baseCurrency (String) - Optional
timezone (String) - Optional
taxId (String) - Optional
```

---

### Category Management

| Method | Endpoint | Purpose | Auth |
|---------|-----------|---------|------|
| GET | `/api/v1/tenants/{tenantId}/categories` | List all categories | Bearer |
| GET | `/api/v1/tenants/{tenantId}/categories/{categoryId}` | Get category by ID | Bearer |
| POST | `/api/v1/tenants/{tenantId}/categories` | Create category | Bearer |
| PUT | `/api/v1/tenants/{tenantId}/categories/{categoryId}` | Update category | Bearer |
| DELETE | `/api/v1/tenants/{tenantId}/categories/{categoryId}` | Delete category | Bearer |

**CategoryRequest Fields:**
```java
categoryName (String) - Required
description (String) - Optional
```

---

### Product Management

| Method | Endpoint | Purpose | Auth |
|---------|-----------|---------|------|
| GET | `/api/v1/tenants/{tenantId}/products` | List all products | Bearer |
| GET | `/api/v1/tenants/{tenantId}/products/{productId}` | Get product by ID | Bearer |
| POST | `/api/v1/tenants/{tenantId}/products` | Create product | Bearer |
| PUT | `/api/v1/tenants/{tenantId}/products/{productId}` | Update product | Bearer |
| DELETE | `/api/v1/tenants/{tenantId}/products/{productId}` | Delete product | Bearer |

**Query Parameters:**
- `page` (int) - Page number (default: 0)
- `size` (int) - Items per page (default: 10)
- `categoryId` (String) - Filter by category
- `status` (String) - Filter by status (e.g., "ACTIVE", "0", "-1")

**ProductRequest Fields:**
```java
productName (String) - Required
description (String) - Optional
defaultPrice (BigDecimal) - Required
defaultCurrency (String) - Required, must be exactly 3 characters (e.g., "USD")
image1Url (String) - Optional, max 256 chars
image2Url (String) - Optional, max 256 chars
image3Url (String) - Optional, max 256 chars
image4Url (String) - Optional, max 256 chars
image5Url (String) - Optional, max 256 chars
banner (String) - Optional, max 256 chars
thumbnail (String) - Optional, max 256 chars
metadata (String) - Optional, can be JSON string
categoryId (String) - Optional
```

---

### Order Management

| Method | Endpoint | Purpose | Auth |
|---------|-----------|---------|------|
| GET | `/api/v1/tenants/{tenantId}/orders` | List all orders | Bearer |
| GET | `/api/v1/tenants/{tenantId}/orders/{orderId}` | Get order by ID | Bearer |
| POST | `/api/v1/tenants/{tenantId}/orders` | Create order | Bearer |
| PUT | `/api/v1/tenants/{tenantId}/orders/{orderId}` | Update order status | Bearer |
| POST | `/api/v1/tenants/{tenantId}/orders/{orderId}/complete` | Complete order | Bearer |
| POST | `/api/v1/tenants/{tenantId}/orders/{orderId}/cancel` | Cancel order | Bearer |
| POST | `/api/v1/tenants/{tenantId}/orders/{orderId}/refund` | Refund order | Bearer |
| DELETE | `/api/v1/tenants/{tenantId}/orders/{orderId}` | Delete order | Bearer |

**OrderRequest Fields:**
```java
userId (String) - Required
currency (String) - Required, must be 3 uppercase letters (e.g., "USD")
totalAmount (BigDecimal) - Required, must be > 0
exchangeRate (BigDecimal) - Required, must be > 0
orderItems (List<OrderItemRequest>) - Required, at least 1 item
discountCode (String) - Optional
```

**OrderItemRequest Fields:**
```java
productId (String) - Required
price (BigDecimal) - Required
licenseKey (String) - Optional
```

**Order Status Values:**
- `Pending` - Initial state
- `Processing` - Payment initiated
- `Completed` - Order fulfilled
- `Cancelled` - Order cancelled
- `Refunded` - Money refunded
- `Partially Refunded` - Partial refund

---

### Payment Management

| Method | Endpoint | Purpose | Auth |
|---------|-----------|---------|------|
| GET | `/api/v1/tenants/{tenantId}/payments` | List all payments | Bearer |
| GET | `/api/v1/tenants/{tenantId}/payments/{paymentId}` | Get payment by ID | Bearer |
| POST | `/api/v1/tenants/{tenantId}/payments` | Create payment | Bearer |
| POST | `/api/v1/tenants/{tenantId}/payments/{paymentId}/confirm` | Confirm payment | Bearer |
| POST | `/api/v1/tenants/{tenantId}/payments/{paymentId}/cancel` | Cancel payment | Bearer |
| POST | `/api/v1/tenants/{tenantId}/payments/{paymentId}/refund` | Full refund | Bearer |
| POST | `/api/v1/tenants/{tenantId}/payments/{paymentId}/partial-refund` | Partial refund | Bearer |

**PaymentRequest Fields:**
```java
orderId (String) - Required
amount (BigDecimal) - Required, must be > 0
currency (String) - Required, must be 3 uppercase letters (e.g., "USD")
paymentMethod (String) - Required (e.g., "CREDIT_CARD", "PAYPAL")
paymentToken (String) - Optional (Stripe token/payment method ID)
```

**Payment Confirm:**
- URL: `/api/v1/tenants/{tenantId}/payments/{paymentId}/confirm?transactionId={stripeTransactionId}`
- Query Parameter: `transactionId` (String) - Required
- **NO REQUEST BODY** - Transaction ID must be in query params!

---

### Discount Management

| Method | Endpoint | Purpose | Auth |
|---------|-----------|---------|------|
| GET | `/api/v1/tenants/{tenantId}/discounts` | List all discounts | Bearer |
| GET | `/api/v1/tenants/{tenantId}/discounts/{discountId}` | Get discount by ID | Bearer |
| POST | `/api/v1/tenants/{tenantId}/discounts` | Create discount | Bearer |
| PUT | `/api/v1/tenants/{tenantId}/discounts/{discountId}` | Update discount | Bearer |
| DELETE | `/api/v1/tenants/{tenantId}/discounts/{discountId}` | Delete discount | Bearer |
| POST | `/api/v1/tenants/{tenantId}/discounts/validate` | Validate discount code | Bearer |
| POST | `/api/v1/tenants/{tenantId}/discounts/cleanup-expired` | Clean expired discounts | Bearer |
| GET | `/api/v1/tenants/{tenantId}/discounts/usage-stats` | Get usage stats | Bearer |

**DiscountCodeRequest Fields:**
```java
code (String) - Required, 3-50 chars, uppercase letters, numbers, hyphens, underscores
discountType (DiscountType) - Required, "PERCENTAGE" or "FIXED"
discountValue (BigDecimal) - Required, > 0
validFrom (LocalDateTime) - Optional
validUntil (LocalDateTime) - Optional
minOrderAmount (BigDecimal) - Optional, default: 0.00
maxUses (Integer) - Optional, 0 means unlimited, default: 0
```

**ValidateDiscountRequest Fields:**
```java
discountCode (String) - Required
orderAmount (BigDecimal) - Required
userId (String) - Optional
```

---

### Tax Management

| Method | Endpoint | Purpose | Auth |
|---------|-----------|---------|------|
| GET | `/api/v1/tenants/{tenantId}/taxes` | List all taxes | Bearer |
| GET | `/api/v1/tenants/{tenantId}/taxes/{taxId}` | Get tax by ID | Bearer |
| POST | `/api/v1/tenants/{tenantId}/taxes` | Create tax | Bearer |
| PUT | `/api/v1/tenants/{tenantId}/taxes/{taxId}` | Update tax | Bearer |
| DELETE | `/api/v1/tenants/{tenantId}/taxes/{taxId}` | Delete tax | Bearer |

**TaxRequest Fields:**
```java
code (String) - Required
description (String) - Optional
value (BigDecimal) - Required
defaultFlag (String) - Optional, "true" or "false"
startDate (LocalDate) - Optional
endDate (LocalDate) - Optional
```

---

### Review Management

| Method | Endpoint | Purpose | Auth |
|---------|-----------|---------|------|
| GET | `/api/v1/tenants/{tenantId}/reviews` | List all reviews (admin) | Bearer |
| GET | `/api/v1/public/tenants/{tenantId}/reviews` | Public: Get product reviews | None |
| GET | `/api/v1/public/tenants/{tenantId}/reviews/product/{productId}` | Get reviews for product | None |
| GET | `/api/v1/public/tenants/{tenantId}/reviews/product/{productId}/rating` | Get product rating | None |
| GET | `/api/v1/public/tenants/{tenantId}/reviews/verified` | Get verified reviews | None |
| POST | `/api/v1/tenants/{tenantId}/reviews` | Create review | Bearer |
| PUT | `/api/v1/tenants/{tenantId}/reviews/{reviewId}` | Update review | Bearer |
| DELETE | `/api/v1/tenants/{tenantId}/reviews/{reviewId}` | Delete review | Bearer |
| POST | `/api/v1/tenants/{tenantId}/reviews/{reviewId}/verify` | Verify review (admin) | Bearer |

**ReviewRequest Fields:**
```java
productId (String) - Required
rating (Integer) - Required, 1-5
comment (String) - Optional, max 1000 chars
```

---

### Bundle Management

| Method | Endpoint | Purpose | Auth |
|---------|-----------|---------|------|
| GET | `/api/v1/tenants/{tenantId}/bundles` | List all bundles | Bearer |
| GET | `/api/v1/tenants/{tenantId}/bundles/{bundleId}` | Get bundle by ID | Bearer |
| POST | `/api/v1/tenants/{tenantId}/bundles` | Create bundle | Bearer |
| PUT | `/api/v1/tenants/{tenantId}/bundles/{bundleId}` | Update bundle | Bearer |
| DELETE | `/api/v1/tenants/{tenantId}/bundles/{bundleId}` | Delete bundle | Bearer |
| POST | `/api/v1/tenants/{tenantId}/bundles/calculate-price` | Calculate bundle price | Bearer |
| POST | `/api/v1/tenants/{tenantId}/bundles/{bundleId}/products/{productId}` | Add product to bundle | Bearer |
| DELETE | `/api/v1/tenants/{tenantId}/bundles/{bundleId}/products/{productId}` | Remove product from bundle | Bearer |
| PUT | `/api/v1/tenants/{tenantId}/bundles/{bundleId}/products/{productId}/quantity` | Update product quantity in bundle | Bearer |
| GET | `/api/v1/tenants/{tenantId}/bundles/count` | Get bundle count | Bearer |

**BundleRequest Fields:**
```java
bundleName (String) - Required
description (String) - Optional, max 1000 chars
bundlePrice (BigDecimal) - Required, > 0
discountPercent (BigDecimal) - Optional, default: 0, 0-100
currency (String) - Required, 3 uppercase letters (e.g., "USD")
bundleItems (List<BundleItemRequest>) - Required, at least 1 item
```

**BundleItemRequest Fields:**
```java
productId (String) - Required
quantity (Integer) - Required, 1-100, default: 1
```

---

### Currency Management

| Method | Endpoint | Purpose | Auth |
|---------|-----------|---------|------|
| GET | `/api/v1/tenants/{tenantId}/currencies` | List all currencies | Bearer |
| GET | `/api/v1/tenants/{tenantId}/currencies/{currencyCode}` | Get currency by code | Bearer |
| POST | `/api/v1/tenants/{tenantId}/currencies` | Create currency | Bearer |
| PUT | `/api/v1/tenants/{tenantId}/currencies/{currencyCode}` | Update currency | Bearer |
| DELETE | `/api/v1/tenants/{tenantId}/currencies/{currencyCode}` | Delete currency | Bearer |
| GET | `/api/v1/tenants/{tenantId}/currencies/default` | Get default currency | Bearer |
| GET | `/api/v1/tenants/{tenantId}/currencies/convert` | Convert currency | Bearer |
| GET | `/api/v1/tenants/{tenantId}/currencies/exchange-rate` | Get exchange rate | Bearer |

**CurrencyRequest Fields:**
```java
currencyCode (String) - Required, exactly 3 uppercase letters
currencyName (String) - Required, max 50 chars
symbol (String) - Required, max 10 chars
exchangeRate (BigDecimal) - Required, > 0
isDefault (String) - Optional, "true" or "false"
```

**Currency Convert:**
- URL: `/api/v1/tenants/{tenantId}/currencies/convert?amount={amount}&fromCurrency={from}&toCurrency={to}`
- Query Parameters:
  - `amount` (BigDecimal) - Required, amount to convert
  - `fromCurrency` (String) - Required, source currency code
  - `toCurrency` (String) - Required, target currency code

---

### Page Management

| Method | Endpoint | Purpose | Auth |
|---------|-----------|---------|------|
| GET | `/api/v1/tenants/{tenantId}/pages` | List all pages | Bearer |
| GET | `/api/v1/tenants/{tenantId}/pages/{pageId}` | Get page by ID | Bearer |
| GET | `/api/v1/public/tenants/{tenantId}/pages` | Public: Get pages | None |
| GET | `/api/v1/public/tenants/{tenantId}/pages/slug/{slug}` | Get page by slug | None |
| POST | `/api/v1/tenants/{tenantId}/pages` | Create page | Bearer |
| PUT | `/api/v1/tenants/{tenantId}/pages/{pageId}` | Update page | Bearer |
| DELETE | `/api/v1/tenants/{tenantId}/pages/{pageId}` | Delete page | Bearer |

**PageRequest Fields:**
```java
title (String) - Required
slug (String) - Required
content (String) - Optional
metaTitle (String) - Optional
metaDescription (String) - Optional
template (String) - Optional
status (String) - Optional, "0" for active, "-1" for inactive
visibility (String) - Optional
isDefault (Boolean) - Optional
language (String) - Optional
```

---

### Store Theme Management

| Method | Endpoint | Purpose | Auth |
|---------|-----------|---------|------|
| GET | `/api/v1/tenants/{tenantId}/themes` | List all themes | Bearer |
| GET | `/api/v1/tenants/{tenantId}/themes/{themeId}` | Get theme by ID | Bearer |
| POST | `/api/v1/tenants/{tenantId}/themes` | Create theme | Bearer |
| PUT | `/api/v1/tenants/{tenantId}/themes/{themeId}` | Update theme | Bearer |
| DELETE | `/api/v1/tenants/{tenantId}/themes/{themeId}` | Delete theme | Bearer |

**StoreThemeRequest Fields:**
```java
themeName (String) - Required
tagline (String) - Optional
description (String) - Optional
bannerImage (String) - Optional, max 256 chars
joinCta (String) - Optional
copyrightText (String) - Optional
heroTitle (String) - Optional
heroDescription (String) - Optional
```

---

### User Management (Tenant)

| Method | Endpoint | Purpose | Auth |
|---------|-----------|---------|------|
| GET | `/api/v1/tenants/{tenantId}/users` | List all users | Bearer |
| GET | `/api/v1/tenants/{tenantId}/users/{userId}` | Get user by ID | Bearer |
| POST | `/api/v1/tenants/{tenantId}/users` | Create user | Bearer |
| PUT | `/api/v1/tenants/{tenantId}/users/{userId}` | Update user | Bearer |
| DELETE | `/api/v1/tenants/{tenantId}/users/{userId}` | Delete user | Bearer |

**UserRequest Fields:**
```java
username (String) - Required, 3-50 chars
password (String) - Required, 8-100 chars
firstName (String) - Optional, max 50 chars
lastName (String) - Optional, max 50 chars
image (String) - Optional, max 256 chars
phone (String) - Required, regex: ^\+?[\d\s\-()]+$, max 15 chars
email (String) - Required, email format, max 320 chars
userType (UserType) - Optional, "INDIVIDUAL" or "COMPANY", default: "INDIVIDUAL"
userRole (UserRole) - Optional, "USER", "ADMIN", or "TENANT", default: "USER"
companyName (String) - Optional, required if userType == "COMPANY"
companyRegistrationNumber (String) - Optional
companyAddress1 (String) - Optional
companyAddress2 (String) - Optional
companyCountry (String) - Optional
companyPincode (String) - Optional
taxId (String) - Optional
```

---

### Download Management

| Method | Endpoint | Purpose | Auth |
|---------|-----------|---------|------|
| POST | `/api/v1/tenants/{tenantId}/order-items/{orderItemId}/record-download` | Record download | Bearer |
| GET | `/api/v1/tenants/{tenantId}/order-items/{orderItemId}/download-history` | Get download history | Bearer |
| GET | `/api/v1/tenants/{tenantId}/users/{userId}/download-history` | Get user downloads | Bearer |
| POST | `/api/v1/tenants/{tenantId}/digital-product-details` | Create digital product details | Bearer |
| PUT | `/api/v1/tenants/{tenantId}/digital-product-details/{productId}` | Update digital product details | Bearer |
| GET | `/api/v1/tenants/{tenantId}/digital-product-details` | List all digital product details | Bearer |
| DELETE | `/api/v1/tenants/{tenantId}/digital-product-details/{productId}` | Delete digital product details | Bearer |

**DigitalProductDetailsRequest Fields:**
```java
productId (String) - Required
fileUrl (String) - Required, max 255 chars
fileSize (Integer) - Optional, file size in KB
fileFormat (String) - Optional, max 20 chars
licenseInfo (String) - Optional, max 500 chars
version (String) - Optional, max 20 chars
status (String) - Optional, "0" for active, "-1" for inactive
```

---

### Public Endpoints (No Auth Required)

| Method | Endpoint | Purpose |
|---------|-----------|---------|
| GET | `/api/v1/public/tenants/{tenantId}` | List all tenants (public) |
| GET | `/api/v1/public/tenants/{tenantId}` | Get tenant details (public) |
| GET | `/api/v1/public/tenants/{tenantId}/products` | Browse products (paginated) |
| GET | `/api/v1/public/tenants/{tenantId}/products/{productId}` | Get product details |
| GET | `/api/v1/public/tenants/{tenantId}/products/category/{categoryId}` | Get products by category |
| GET | `/api/v1/public/tenants/{tenantId}/products/active` | Get active products |
| GET | `/api/v1/public/tenants/{tenantId}/products/search` | Search products |
| GET | `/api/v1/public/tenants/{tenantId}/categories` | Browse categories |
| GET | `/api/v1/public/tenants/{tenantId}/categories/{categoryId}` | Get category details |
| GET | `/api/v1/public/tenants/{tenantId}/pages` | Browse pages |
| GET | `/api/v1/public/tenants/{tenantId}/pages/{pageId}` | Get page by ID |
| GET | `/api/v1/public/tenants/{tenantId}/pages/slug/{slug}` | Get page by slug |

---

### User/Customer Auth Endpoints

| Method | Endpoint | Purpose | Auth |
|---------|-----------|---------|------|
| POST | `/api/v1/auth/signup` | User signup | x-www-form-urlencoded |
| POST | `/api/v1/auth/login` | User login | x-www-form-urlencoded |
| POST | `/api/v1/auth/forgot-password` | Reset password | x-www-form-urlencoded |
| GET | `/api/v1/tenants/{tenantId}/users/me` | Get current user | Bearer |
| DELETE | `/api/v1/tenants/{tenantId}/users/me/logout` | Logout | Bearer |

**SignupRequest Fields:**
```java
username (String) - Required, 3-50 chars
password (String) - Required, 8-100 chars
phone (String) - Required, regex: ^\+?[\d\s\-()]+$, max 15 chars
email (String) - Required, email format, max 320 chars
firstName (String) - Optional, max 50 chars
lastName (String) - Optional, max 50 chars
tenantId (Integer) - Optional, for tenant-scoped users
```

**LoginRequest Fields:**
```java
username (String) - Required
password (String) - Required
privateDevice (Boolean) - Optional, default: false
```

**ForgotPasswordRequest Fields:**
```java
email (String) - Required, email format
```

---

### Customer My Orders

| Method | Endpoint | Purpose | Auth |
|---------|-----------|---------|------|
| GET | `/api/v1/tenants/{tenantId}/orders/me` | Get my orders | Bearer |
| GET | `/api/v1/tenants/{tenantId}/orders/{orderId}` | Get order details | Bearer |

---

### Customer My Reviews

| Method | Endpoint | Purpose | Auth |
|---------|-----------|---------|------|
| GET | `/api/v1/tenants/{tenantId}/reviews/me` | Get my reviews | Bearer |
| POST | `/api/v1/tenants/{tenantId}/reviews` | Create review | Bearer |
| PUT | `/api/v1/tenants/{tenantId}/reviews/{reviewId}` | Update review | Bearer |
| DELETE | `/api/v1/tenants/{tenantId}/reviews/{reviewId}` | Delete review | Bearer |

---

## Detailed Testing Flows

### Flow 1: Platform Admin → Create Tenant → Tenant Admin Setup

**Step 1: Platform Admin Login**
```
Request:
POST {{base_url}}/api/v1/auth/platform/login
Content-Type: application/x-www-form-urlencoded

Body (form-data):
username: admin
password: admin
privateDevice: false

Expected Response (200):
{
  "access_token": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "username": "admin"
}

Environment Variable Set:
access_token = eyJhbGciOiJIUzI1NiIs...
```

**Step 2: Create New Tenant**
```
Request:
POST {{base_url}}/api/v1/tenants
Authorization: Bearer {{access_token}}
Content-Type: application/json

Body:
{
  "shopName": "TechWorld Electronics",
  "shopEmail": "contact@techworld.com",
  "shopPhone": "+1-555-1000",
  "shopLogo": "https://techworld.com/logo.png",
  "domainName": "techworld.com",
  "subdomain": "techworld",
  "countryRegion": "United States",
  "baseCurrency": "USD",
  "timezone": "America/New_York",
  "taxId": "US123456789",
  "adminUsername": "techworld_admin",
  "adminPassword": "Admin@2024",
  "adminEmail": "admin@techworld.com"
}

Expected Response (201):
{
  "tenantId": 1,
  "shopName": "TechWorld Electronics",
  "status": "ACTIVE",
  "created": "2024-01-15T10:30:00"
}

Environment Variable Set:
tenant_id = 1
```

**Step 3: Switch to Tenant Admin Collection**
- Select `2-Tenant-Admin.postman_collection.json`

**Step 4: Tenant Admin Login**
```
Request:
POST {{base_url}}/api/v1/auth/tenant/login
Content-Type: application/x-www-form-urlencoded

Body (form-data):
tenantId: 1
username: techworld_admin
password: Admin@2024

Expected Response (200):
{
  "access_token": "eyJhbGciOiJIUzI1NiIs...",
  "tenantId": 1,
  "username": "techworld_admin"
}
```

**Step 5: Create First Category**
```
Request:
POST {{base_url}}/api/v1/tenants/1/categories
Authorization: Bearer {{access_token}}
Content-Type: application/json

Body:
{
  "categoryName": "Electronics",
  "description": "Electronic devices and gadgets including smartphones, laptops, tablets, wearables, and accessories. Browse our extensive collection of cutting-edge technology from top brands worldwide."
}

Expected Response (201):
{
  "categoryId": 1,
  "categoryName": "Electronics",
  "status": "0"
}

Environment Variable Set:
category_id = 1
```

**Step 6: Create First Product**
```
Request:
POST {{base_url}}/api/v1/tenants/1/products
Authorization: Bearer {{access_token}}
Content-Type: application/json

Body:
{
  "categoryId": 1,
  "productName": "iPhone 15 Pro Max 256GB",
  "description": "The most powerful iPhone ever with A17 Pro chip, 256GB storage, and titanium design. Features a stunning 6.7-inch Super Retina XDR display, 48MP camera system with 5x optical zoom, and all-day battery life.",
  "defaultPrice": 1199.99,
  "defaultCurrency": "USD",
  "image1Url": "https://example.com/products/iphone15pro-front.jpg",
  "image2Url": "https://example.com/products/iphone15pro-side.jpg",
  "image3Url": "https://example.com/products/iphone15pro-back.jpg",
  "image4Url": "https://example.com/products/iphone15pro-camera.jpg",
  "image5Url": "https://example.com/products/iphone15pro-box.jpg",
  "thumbnail": "https://example.com/products/iphone15pro-thumb.jpg",
  "banner": "https://example.com/banners/iphone-launch.jpg",
  "metadata": "{\"brand\": \"Apple\", \"model\": \"iPhone 15 Pro Max\", \"color\": \"Space Black\", \"storage\": \"256GB\", \"ram\": \"8GB\", \"released\": \"2024-09-20\"}"
}

Expected Response (201):
{
  "productId": 1,
  "productName": "iPhone 15 Pro Max 256GB",
  "status": "0"
}

Environment Variable Set:
product_id = 1
```

**Step 7: Create First Bundle**
```
Request:
POST {{base_url}}/api/v1/tenants/1/bundles
Authorization: Bearer {{access_token}}
Content-Type: application/json

Body:
{
  "bundleName": "Photographer Starter Kit",
  "description": "Everything you need to start your photography journey. Includes essential editing software, presets, and tutorials for beginners.",
  "bundlePrice": 299.99,
  "discountPercent": 20.0,
  "currency": "USD",
  "bundleItems": [
    { "productId": 1, "quantity": 1 },
    { "productId": 2, "quantity": 1 },
    { "productId": 3, "quantity": 1 }
  ]
}

Expected Response (201):
{
  "bundleId": 1,
  "bundleName": "Photographer Starter Kit"
}

Environment Variable Set:
bundle_id = 1
```

**Step 8: Create First Discount**
```
Request:
POST {{base_url}}/api/v1/tenants/1/discounts
Authorization: Bearer {{access_token}}
Content-Type: application/json

Body:
{
  "code": "SUMMER2024",
  "discountType": "PERCENTAGE",
  "discountValue": 20.0,
  "validFrom": "2024-06-01T00:00:00",
  "validUntil": "2024-09-30T23:59:59",
  "minOrderAmount": 50.0,
  "maxUses": 100
}

Expected Response (201):
{
  "discountId": 1,
  "code": "SUMMER2024"
}
```

**Step 9: Create First Currency**
```
Request:
POST {{base_url}}/api/v1/tenants/1/currencies
Authorization: Bearer {{access_token}}
Content-Type: application/json

Body:
{
  "currencyCode": "EUR",
  "currencyName": "Euro",
  "symbol": "€",
  "exchangeRate": 0.85,
  "isDefault": "false"
}

Expected Response (201):
{
  "currencyCode": "EUR"
}
```

---

### Flow 2: Customer Signup → Browse Products → Place Order → Download

**Step 1: Customer Signup**
```
Request:
POST {{base_url}}/api/v1/auth/signup
Content-Type: application/x-www-form-urlencoded

Body (form-data):
username: johndoe2024
password: SecurePass@123
phone: +1-555-1234567
email: john.doe@example.com
firstName: John
lastName: Doe
tenantId: 1

Expected Response (200):
{
  "access_token": "eyJhbGciOiJIUzI1NiIs...",
  "userId": 1,
  "username": "johndoe2024"
}

Environment Variables Set:
access_token = eyJhbGciOiJIUzI1NiIs...
user_id = 1
```

**Step 2: Browse Products (Public)**
```
Request:
GET {{base_url}}/api/v1/public/tenants/1/products?page=0&size=10

Expected Response (200):
{
  "products": [
    {
      "productId": "1",
      "productName": "iPhone 15 Pro Max",
      "defaultPrice": 1199.99,
      "thumbnail": "https://example.com/thumb.jpg"
    },
    ...
  ],
  "total": 50,
  "page": 0,
  "size": 10
}

Tip: Copy productId from response for purchase
```

**Step 3: Browse Products by Category**
```
Request:
GET {{base_url}}/api/v1/public/tenants/1/products/category/{{category_id}}

Expected Response (200):
{
  "products": [
    {
      "productId": "1",
      "productName": "iPhone 15 Pro Max",
      ...
    }
  ]
}
```

**Step 4: Get Product Details**
```
Request:
GET {{base_url}}/api/v1/public/tenants/1/products/{{product_id}}

Expected Response (200):
{
  "productId": "1",
  "productName": "iPhone 15 Pro Max",
  "description": "The most powerful iPhone...",
  "defaultPrice": 1199.99,
  "categoryId": 1,
  ...
}
```

**Step 5: Create Order**
```
Request:
POST {{base_url}}/api/v1/tenants/1/orders
Authorization: Bearer {{access_token}}
Content-Type: application/json

Body:
{
  "userId": 1,
  "currency": "USD",
  "totalAmount": 1199.99,
  "exchangeRate": 1.0,
  "orderItems": [
    {
      "productId": 1,
      "price": 1199.99,
      "licenseKey": "LICENSE-KEY-12345"
    }
  ],
  "discountCode": "SUMMER2024"
}

Expected Response (201):
{
  "orderId": 1001,
  "status": "Pending",
  "totalAmount": 1199.99,
  "orderItems": [...]
}

Environment Variable Set:
order_id = 1001
```

**Step 6: Create Payment**
```
Request:
POST {{base_url}}/api/v1/tenants/1/payments
Authorization: Bearer {{access_token}}
Content-Type: application/json

Body:
{
  "orderId": 1001,
  "amount": 1199.99,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "paymentToken": "pm_card_visa"
}

Expected Response (201):
{
  "paymentId": 5001,
  "status": "Pending",
  "amount": 1199.99
}
```

**Step 7: Confirm Payment**
```
Request:
POST {{base_url}}/api/v1/tenants/1/payments/5001/confirm?transactionId=txn_123456789
Authorization: Bearer {{access_token}}

Expected Response (200):
{
  "paymentId": 5001,
  "status": "Completed",
  "transactionId": "txn_123456789"
}

⚠️ IMPORTANT: transactionId goes in QUERY PARAMETER, not request body!
```

**Step 8: View Order Details**
```
Request:
GET {{base_url}}/api/v1/tenants/1/orders/1001
Authorization: Bearer {{access_token}}

Expected Response (200):
{
  "orderId": 1001,
  "status": "Completed",
  "totalAmount": 1199.99,
  "orderItems": [...],
  "payment": {
    "paymentId": 5001,
    "status": "Completed"
  }
}
```

**Step 9: Record Download**
```
Request:
POST {{base_url}}/api/v1/tenants/1/order-items/{{order_item_id}}/record-download
Authorization: Bearer {{access_token}}

Expected Response (200):
{
  "downloadId": 1,
  "productId": 1,
  "ipAddress": "192.168.1.100"
}

Note: ipAddress is automatically extracted by backend, no body needed
```

---

### Flow 3: Complete E-Commerce Cycle

This flow demonstrates full order lifecycle with bundles, discounts, taxes, and refunds.

**Step 1: Create Discount Code**
```
Request:
POST {{base_url}}/api/v1/tenants/1/discounts
Authorization: Bearer {{access_token}}
Content-Type: application/json

Body:
{
  "code": "NEWCUSTOMER10",
  "discountType": "PERCENTAGE",
  "discountValue": 15.0,
  "validFrom": "2024-01-01T00:00:00",
  "validUntil": "2024-12-31T23:59:59",
  "minOrderAmount": 25.0,
  "maxUses": 50
}
```

**Step 2: Validate Discount**
```
Request:
POST {{base_url}}/api/v1/tenants/1/discounts/validate
Authorization: Bearer {{access_token}}
Content-Type: application/json

Body:
{
  "discountCode": "NEWCUSTOMER10",
  "orderAmount": 119.99,
  "userId": 1
}

Expected Response (200):
{
  "valid": true,
  "discountValue": 15.0,
  "discountedAmount": 179.99,
  "finalAmount": 1079.99
}
```

**Step 3: Create Bundle**
```
Request:
POST {{base_url}}/api/v1/tenants/1/bundles
Authorization: Bearer {{access_token}}
Content-Type: application/json

Body:
{
  "bundleName": "Premium Software Bundle",
  "description": "Professional editing suite including photo editor, color grading tools, and batch processor",
  "bundlePrice": 499.99,
  "discountPercent": 10.0,
  "currency": "USD",
  "bundleItems": [
    { "productId": 1, "quantity": 1 },
    { "productId": 2, "quantity": 2 }
  ]
}
```

**Step 4: Calculate Bundle Price**
```
Request:
POST {{base_url}}/api/v1/tenants/1/bundles/calculate-price
Authorization: Bearer {{access_token}}
Content-Type: application/json

Body:
[
  { "productId": 1, "quantity": 2 },
  { "productId": 2, "quantity": 1 }
]

Expected Response (200):
{
  "originalPrice": 3599.97,
  "discountAmount": 359.97,
  "finalPrice": 3239.97
}
```

**Step 5: Create Order with Bundle**
```
Request:
POST {{base_url}}/api/v1/tenants/1/orders
Authorization: Bearer {{access_token}}
Content-Type: application/json

Body:
{
  "userId": 1,
  "currency": "USD",
  "totalAmount": 3239.97,
  "exchangeRate": 1.0,
  "orderItems": [
    {
      "productId": 3,  // Using bundle
      "price": 3239.97
    }
  ],
  "discountCode": "NEWCUSTOMER10"
}

Expected Response (201):
{
  "orderId": 1002,
  "status": "Pending"
}
```

**Step 6: Process Payment**
```
Request:
POST {{base_url}}/api/v1/tenants/1/payments
Authorization: Bearer {{access_token}}
Content-Type: application/json

Body:
{
  "orderId": 1002,
  "amount": 3239.97,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "paymentToken": "pm_card_visa"
}
```

**Step 7: Confirm Payment**
```
Request:
POST {{base_url}}/api/v1/tenants/1/payments/{{payment_id}}/confirm?transactionId=txn_final_123456
Authorization: Bearer {{access_token}}

Expected Response (200):
{
  "paymentId": 5002,
  "status": "Completed",
  "transactionId": "txn_final_123456"
}
```

**Step 8: Complete Order**
```
Request:
POST {{base_url}}/api/v1/tenants/1/orders/1002/complete
Authorization: Bearer {{access_token}}

Expected Response (200):
{
  "orderId": 1002,
  "status": "Completed"
}
```

---

## Code Examples

### Platform Login Request (Form Data)
```
POST {{base_url}}/api/v1/auth/platform/login
Content-Type: application/x-www-form-urlencoded

username=admin&password=admin&privateDevice=false
```

### Create Order Request
```json
POST {{base_url}}/api/v1/tenants/1/orders
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
  "userId": "1",
  "currency": "USD",
  "totalAmount": "199.98",
  "exchangeRate": 1.0,
  "orderItems": [
    {
      "productId": "1",
      "price": "99.99,
      "licenseKey": "LICENSE-123"
    }
  ],
  "discountCode": "WELCOME10"
}
```

---

## Common Response Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created successfully |
| 204 | No content (delete successful) |
| 400 | Bad request (validation error) |
| 401 | Unauthorized (invalid/missing token) |
| 403 | Forbidden (no permission) |
| 404 | Not found |
| 409 | Conflict (duplicate resource) |
| 422 | Unprocessable entity (validation error) |
| 500 | Internal server error |

**Common 400/422 Error Responses:**
```json
// Invalid credentials
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid username or password"
}

// Validation error
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Password must be between 8 and 40 characters",
  "errors": [
    { "field": "password", "message": "Password must be between 8 and 40 characters" }
  ]
}

// Discount validation error
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Discount code has expired",
  "errors": [
    { "field": "validUntil", "message": "Valid until date must be in the future" }
  ]
}
```

---

## Troubleshooting

### Common Issues

| Issue | Solution |
|--------|----------|
| 401 Unauthorized | Verify `Authorization: Bearer {{access_token}}` header is set |
| | Verify token is valid (not expired) |
| 400 Bad Request | Check Content-Type matches expected format |
| | Check field names match DTO exactly |
| | Check all required fields are present |
| 404 Not Found | Verify resource ID exists and tenant_id is correct |
| 422 Unprocessable | Check validation rules (email format, password length, etc.) |
| 500 Internal Server | Check backend logs for errors |

### Payment Confirm Issues

**Issue:** Transaction ID in request body instead of query parameter

**Wrong:**
```
POST /api/v1/tenants/1/payments/5001/confirm
Body: { "transactionId": "txn_123" }
```

**Correct:**
```
POST /api/v1/tenants/1/payments/5001/confirm?transactionId=txn_123
No body needed
```

### Content Type Reference

| Content-Type | Used By Endpoints |
|--------------|-------------------|
| application/x-www-form-urlencoded | Platform Admin Login, Forgot Password, Tenant Admin Signup, Login, Forgot Password, User Signup, Login, Forgot Password |
| application/json | All other endpoints |

### Authentication Flow

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. Platform Admin Login                                    │
│    → access_token, tenant_id (optional)                    │
├─────────────────────────────────────────────────────────────────┤
│ 2. User/Platform Admin Login                               │
│    → access_token, user_id                             │
├─────────────────────────────────────────────────────────────────┤
│ 3. Use access_token in Authorization header               │
│    Authorization: Bearer {{access_token}}                   │
└─────────────────────────────────────────────────────────────────┘
```

### Token Management

Access tokens are automatically set by login endpoints. Use the returned token in subsequent requests:

```
GET {{base_url}}/api/v1/tenants/1/products
Authorization: Bearer {{access_token}}
```

The token will be available as an environment variable `access_token` after login.

---

## Testing Checklist

Use this checklist to verify all endpoints work:

### Platform Admin
- [ ] Platform Admin Login
- [ ] Forgot Password
- [ ] Get Me
- [ ] Get All Tenants
- [ ] Get Tenant by ID
- [ ] Create Tenant
- [ ] Update Tenant
- [ ] Delete Tenant

### Tenant Admin Auth
- [ ] Tenant Signup
- [ ] Tenant Login
- [ ] Tenant Forgot Password
- [ ] Get Me (Tenant Admin)

### User Management
- [ ] Get All Users
- [ ] Get User by ID
- [ ] Create User
- [ ] Update User
- [ ] Delete User

### Category Management
- [ ] Get All Categories
- [ ] Get Category by ID
- [ ] Create Category
- [ ] Update Category
- [ ] Delete Category

### Product Management
- [ ] Get All Products
- [ ] Get Product by ID
- [ ] Create Product
- [ ] Update Product
- [ ] Delete Product
- [ ] Test pagination (?page=0&size=10)
- [ ] Test category filter (?categoryId=CAT-001)

### Order Management
- [ ] Get All Orders
- [ ] Get Order by ID
- [ ] Create Order
- [ ] Update Order Status
- [ ] Complete Order
- [ ] Cancel Order
- [ ] Refund Order
- [ ] Delete Order

### Payment Management
- [ ] Get All Payments
- [ ] Get Payment by ID
- [ ] Create Payment
- [ ] Confirm Payment (use query param, not body!)
- [ ] Cancel Payment
- [ ] Full Refund
- [ ] Partial Refund

### Discount Management
- [ ] Get All Discounts
- [ ] Get Discount by ID
- [ ] Create Discount
- [ ] Update Discount
- [ ] Delete Discount
- [ ] Validate Discount Code
- [ ] Clean Expired Discounts
- [ ] Get Usage Stats

### Tax Management
- [ ] Get All Taxes
- [ ] Get Tax by ID
- [ ] Create Tax
- [ ] Update Tax
- [ ] Delete Tax

### Review Management
- [ ] Get All Reviews
- [ ] Get Product Reviews (public)
- [ ] Get Product Rating
- [ ] Get Verified Reviews
- [ ] Create Review
- [ ] Update Review
- [ ] Delete Review
- [ ] Verify Review

### Bundle Management
- [ ] Get All Bundles
- [ ] Get Bundle by ID
- [ ] Create Bundle
- [ ] Update Bundle
- [ ] Delete Bundle
- [ ] Calculate Bundle Price
- [ ] Add Product to Bundle
- [ ] Remove Product from Bundle
- [ ] Update Product Quantity in Bundle
- [ ] Get Bundle Count

### Currency Management
- [ ] Get All Currencies
- [ ] Get Currency by Code
- [ ] Create Currency
- [ ] Update Currency
- [ ] Delete Currency
- [ ] Get Default Currency
- [ ] Convert Currency (query params: fromCurrency, toCurrency)
- [ ] Get Exchange Rate

### Page Management
- [ ] Get All Pages
- [ ] Get Page by ID
- [ ] Create Page
- [ ] Update Page
- [ ] Delete Page

### Theme Management
- [ ] Get All Themes
- [ ] Get Theme by ID
- [ ] Create Theme
- [ ] Update Theme
- [ ] Delete Theme

### Download Management
- [ ] Record Download
- [ ] Get Download History
- [ ] Get User Download History
- [ ] Create Digital Product Details
- [ ] Update Digital Product Details
- [ ] Get Digital Product Details
- [ ] Delete Digital Product Details

### Public Endpoints
- [ ] Browse Products (public)
- [ ] Browse Products by Category (public)
- [ ] Get Active Products (public)
- [ ] Search Products (public)
- [ ] Browse Categories (public)
- [ ] Browse Pages (public)
- [ ] Get Page by Slug (public)

### User/Customer Auth
- [ ] User Signup
- [ ] User Login
- [ ] User Forgot Password
- [ ] Get Me (User)
- [ ] User Logout

### Customer My Orders
- [ ] Get My Orders
- [ ] Get Order Details

### Customer My Reviews
- [ ] Get My Reviews
- [ ] Create Review
- [ ] Update Review
- [ ] Delete Review

### Customer My Downloads
- [ ] Get My Downloads
- [ ] Record Download
