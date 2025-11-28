# Digital E-Store API Testing Guide

Complete step-by-step testing guide for all Postman collections with sample data.

---

## Prerequisites

### 1. Environment Setup
Create a Postman environment with these variables:
```
base_url = http://localhost:8080
tenant_id = (will be auto-set)
auth_token = (will be auto-set)
user_token = (will be auto-set)
admin_username = (will be auto-set)
user_id = (will be auto-set)
product_id = (will be auto-set)
category_id = (will be auto-set)
order_id = (will be auto-set)
payment_id = (will be auto-set)
bundle_id = (will be auto-set)
discount_id = (will be auto-set)
review_id = (will be auto-set)
theme_id = (will be auto-set)
page_id = (will be auto-set)
tax_id = (will be auto-set)
currency_code = (will be auto-set)
```

### 2. Application Setup
- Ensure MySQL is running
- Ensure Redis is running
- Start the Spring Boot application: `mvn spring-boot:run`
- Verify application is running: `http://localhost:8080`

---

## Collection 1: Tenant Operations (Platform Admin)

### Step 1: Tenant Signup with Admin
**Endpoint:** `POST /api/v1/auth/tenant/signup`
**Auth:** None
**Purpose:** Create a new tenant/store with admin user in one call

**Sample Data:**
```json
{
    "shopName": "TechWorld Electronics",
    "shopEmail": "contact@techworld.com",
    "shopPhone": "+1-555-1000",
    "adminUsername": "techworld_admin",
    "adminPassword": "Admin@2024",
    "adminEmail": "admin@techworld.com",
    "adminPhone": "+1-555-1001",
    "adminFirstName": "David",
    "adminLastName": "Wilson",
    "subdomain": "techworld",
    "countryRegion": "United States",
    "baseCurrency": "USD"
}
```

**Required Fields:**
- `shopName` ✅
- `shopEmail` ✅ (must be valid email)
- `adminUsername` ✅
- `adminPassword` ✅
- `adminEmail` ✅ (must be valid email)
- `adminPhone` ✅ (must be unique)

**Optional Fields:**
- `adminFirstName` (recommended)
- `adminLastName` (recommended)
- `shopPhone`
- `subdomain`
- `countryRegion`
- `baseCurrency` (defaults to "USD")

**Expected Result:**
- Status: 201 Created
- Response includes `tenantId`, `shopName`, and `adminUsername`
- Environment variables `tenant_id` and `admin_username` are auto-set

---

### Step 2: Login as System Admin
**Endpoint:** `POST /api/v1/auth/login`
**Auth:** None
**Purpose:** Login as platform/system admin (manages ALL tenants)

**Sample Data:**
```json
{
    "username": "admin",
    "password": "admin123"
}
```

**Note:** Platform admins do NOT need `tenantId` - they're above all tenants!

**Expected Result:**
- Status: 200 OK
- Response includes JWT `access_token`
- Environment variable `auth_token` is auto-set

---

### Step 3: Create Tenant - TechHub Store
**Endpoint:** `POST /api/v1/tenants`
**Auth:** Bearer Token ({{auth_token}})
**Purpose:** Create another tenant manually

**Sample Data:**
```json
{
    "shopName": "TechHub Store",
    "shopEmail": "contact@techhub.com",
    "shopPhone": "+1-555-0101",
    "shopLogo": "https://techhub.com/logo.png",
    "domainName": "techhub.com",
    "subdomain": "techhub",
    "countryRegion": "United States",
    "storePassword": "TechHub@2024",
    "baseCurrency": "USD",
    "multiCurrency": "1",
    "taxId": "US-TAX-12345",
    "timezone": "America/New_York"
}
```

**Expected Result:**
- Status: 201 Created
- Environment variable `tenant_id` is updated

---

### Step 4: Get All Tenants
**Endpoint:** `GET /api/v1/tenants`
**Auth:** Bearer Token
**Purpose:** List all tenants in the system

**Expected Result:**
- Status: 200 OK
- Returns array of all tenants

---

### Step 5: Get Tenant by ID
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}`
**Auth:** Bearer Token
**Purpose:** Get specific tenant details

**Expected Result:**
- Status: 200 OK
- Returns tenant details

---

### Step 6: Update Tenant
**Endpoint:** `PUT /api/v1/tenants/{{tenant_id}}`
**Auth:** Bearer Token
**Purpose:** Update tenant configuration

**Sample Data:**
```json
{
    "shopName": "TechHub Pro Store",
    "shopEmail": "contact@techhub.com",
    "shopPhone": "+1-555-0101",
    "shopLogo": "https://techhub.com/logo-new.png",
    "domainName": "techhub.com",
    "subdomain": "techhub",
    "countryRegion": "United States",
    "storePassword": "TechHub@2024",
    "baseCurrency": "USD",
    "multiCurrency": "1",
    "taxId": "US-TAX-12345",
    "timezone": "America/New_York"
}
```

**Expected Result:**
- Status: 200 OK
- Returns updated tenant

---

## Collection 2: Admin Operations (Store Admin)

### Step 7: Login as Store Admin (Tenant 1)
**Endpoint:** `POST /api/v1/auth/login`
**Auth:** None
**Purpose:** Login as store admin for a specific tenant/store

**Sample Data:**
```json
{
    "tenantId": 1,
    "username": "admin",
    "password": "admin123"
}
```

**Note:** Store admins NEED `tenantId` because they belong to a specific tenant!
- Platform Admin: NO tenantId (manages all stores)
- Store Admin: YES tenantId (manages one store)
- Customer: YES tenantId (shops at one store)

**Expected Result:**
- Status: 200 OK
- Environment variables `auth_token` and `tenant_id` (set to 1) are updated

---

### Step 8: Create Store Theme
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/themes`
**Auth:** Bearer Token
**Purpose:** Create store theme

**Sample Data:**
```json
{
    "themeName": "Modern Dark",
    "tagline": "Experience Technology in Style",
    "description": "A sleek, modern dark theme optimized for tech products",
    "bannerImage": "https://techhub.com/banners/dark-theme.jpg",
    "joinCta": "Join TechHub Community",
    "copyrightText": "© 2024 TechHub Store. All rights reserved."
}
```

**Expected Result:**
- Status: 201 Created
- Environment variable `theme_id` is set

---

### Step 9: Get All Themes
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/themes`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK
- Returns array of themes

---

### Step 10: Update Theme
**Endpoint:** `PUT /api/v1/tenants/{{tenant_id}}/themes/{{theme_id}}`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "themeName": "Modern Dark Pro",
    "tagline": "Experience Technology in Style - Premium",
    "description": "Enhanced sleek, modern dark theme with premium features",
    "bannerImage": "https://techhub.com/banners/dark-pro.jpg",
    "joinCta": "Join Elite Community",
    "copyrightText": "© 2024 TechHub Store. Premium Edition."
}
```

**Expected Result:**
- Status: 200 OK

---

### Step 11: Create Page - About Us
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/pages`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "title": "About TechHub Store",
    "slug": "about-us",
    "content": "<h1>Welcome to TechHub</h1><p>We are a leading technology store committed to providing the latest gadgets and electronics at competitive prices.</p>",
    "metaTitle": "About TechHub - Leading Technology Store",
    "metaDescription": "Learn about TechHub Store, your trusted source for technology.",
    "status": "PUBLISHED",
    "visibility": "PUBLIC"
}
```

**Expected Result:**
- Status: 201 Created
- Environment variable `page_id` is set

---

### Step 12: Create Page - Privacy Policy
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/pages`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "title": "Privacy Policy",
    "slug": "privacy-policy",
    "content": "<h1>Privacy Policy</h1><p>Last updated: November 2024</p><h2>Information We Collect</h2><p>We collect information you provide directly to us...</p>",
    "metaTitle": "Privacy Policy - TechHub Store",
    "metaDescription": "Read our privacy policy.",
    "status": "PUBLISHED",
    "visibility": "PUBLIC"
}
```

**Expected Result:**
- Status: 201 Created

---

### Step 13: Get All Pages
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/pages`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK
- Returns array of pages

---

### Step 14: Get Published Pages
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/pages/published`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK
- Returns only published pages

---

### Step 15: Get Page by Slug
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/pages/slug/about-us`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK
- Returns specific page

---

### Step 16: Update Page
**Endpoint:** `PUT /api/v1/tenants/{{tenant_id}}/pages/{{page_id}}`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "title": "About TechHub Store - Updated",
    "slug": "about-us",
    "content": "<h1>Welcome to TechHub</h1><p>We are a leading technology store - Now serving 50+ countries!</p>",
    "metaTitle": "About TechHub - Leading Technology Store Worldwide",
    "metaDescription": "Learn about TechHub Store.",
    "status": "PUBLISHED",
    "visibility": "PUBLIC"
}
```

**Expected Result:**
- Status: 200 OK

---

### Step 17: Create Tax - Sales Tax
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/taxes`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "code": "US-SALES-TAX",
    "description": "Standard Sales Tax for United States",
    "value": 7.50,
    "defaultFlag": "1",
    "startDate": "2024-01-01",
    "endDate": "2024-12-31"
}
```

**Expected Result:**
- Status: 201 Created
- Environment variable `tax_id` is set

---

### Step 18: Create Tax - VAT Standard
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/taxes`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "code": "VAT-STANDARD",
    "description": "Standard VAT Rate",
    "value": 20.00,
    "defaultFlag": "0",
    "startDate": "2024-01-01",
    "endDate": "2024-12-31"
}
```

**Expected Result:**
- Status: 201 Created

---

### Step 19: Get All Taxes
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/taxes`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 20: Get Active Taxes
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/taxes/active`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 21: Get Default Tax
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/taxes/default`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK
- Returns default tax configuration

---

### Step 22: Update Tax
**Endpoint:** `PUT /api/v1/tenants/{{tenant_id}}/taxes/{{tax_id}}`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "code": "US-SALES-TAX",
    "description": "Updated Sales Tax",
    "value": 8.00,
    "defaultFlag": "1",
    "startDate": "2024-01-01",
    "endDate": "2024-12-31"
}
```

**Expected Result:**
- Status: 200 OK

---

### Step 23: Create Category - Electronics
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/categories`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "categoryName": "Electronics",
    "description": "Latest electronic devices and gadgets"
}
```

**Expected Result:**
- Status: 201 Created
- Environment variable `category_id` is set

---

### Step 24: Get All Categories
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/categories`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 25: Get Category by ID
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/categories/{{category_id}}`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 26: Get Active Categories
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/categories?status=ACTIVE`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 27: Update Category
**Endpoint:** `PUT /api/v1/tenants/{{tenant_id}}/categories/{{category_id}}`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "categoryName": "Electronics & Gadgets",
    "description": "Latest electronic devices, gadgets, and accessories"
}
```

**Expected Result:**
- Status: 200 OK

---

### Step 28: Create Product - iPhone 15 Pro Max
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/products`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "productName": "iPhone 15 Pro Max",
    "description": "Latest flagship iPhone with A17 Pro chip",
    "categoryId": "{{category_id}}",
    "defaultPrice": 1199.00,
    "defaultCurrency": "USD",
    "stockQuantity": 50
}
```

**Expected Result:**
- Status: 201 Created
- Environment variable `product_id` is set

---

### Step 29: Get All Products
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/products`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 30: Get Product by ID
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/products/{{product_id}}`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 31: Get Active Products
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/products?status=ACTIVE`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 32: Update Product
**Endpoint:** `PUT /api/v1/tenants/{{tenant_id}}/products/{{product_id}}`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "productName": "iPhone 15 Pro Max - Updated",
    "description": "Latest flagship iPhone with A17 Pro chip - Now with more storage",
    "categoryId": "{{category_id}}",
    "defaultPrice": 1299.00,
    "defaultCurrency": "USD",
    "stockQuantity": 75
}
```

**Expected Result:**
- Status: 200 OK

---

### Step 33: Create Discount - WELCOME10
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/discounts`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "code": "WELCOME10",
    "discountType": "PERCENTAGE",
    "discountValue": 10.00,
    "minOrderAmount": 50.00,
    "maxUses": 100,
    "validFrom": "2024-01-01T00:00:00",
    "validTo": "2024-12-31T23:59:59"
}
```

**Expected Result:**
- Status: 201 Created
- Environment variable `discount_id` is set

---

### Step 34: Get All Discounts
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/discounts`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 35: Get Discount by ID
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/discounts/{{discount_id}}`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 36: Get Discount by Code
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/discounts?code=WELCOME10`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 37: Get Active Discounts
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/discounts?status=ACTIVE`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 38: Update Discount
**Endpoint:** `PUT /api/v1/tenants/{{tenant_id}}/discounts/{{discount_id}}`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "code": "WELCOME10",
    "discountType": "PERCENTAGE",
    "discountValue": 15.00,
    "minOrderAmount": 50.00,
    "maxUses": 200,
    "validFrom": "2024-01-01T00:00:00",
    "validTo": "2024-12-31T23:59:59"
}
```

**Expected Result:**
- Status: 200 OK

---

### Step 39: Get Discount Usage Statistics
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/discounts/{{discount_id}}/usage`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 40: Cleanup Expired Discounts
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/discounts/cleanup-expired`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 41: Get All Currencies
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/currencies`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 42: Get Currency by Code
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/currencies/USD`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 43: Create Currency - EUR
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/currencies`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "currencyCode": "EUR",
    "currencyName": "Euro",
    "isDefault": "0",
    "exchangeRate": 0.85,
    "symbol": "€",
    "status": "0"
}
```

**Expected Result:**
- Status: 201 Created
- Environment variable `currency_code` is set

---

### Step 44: Update Currency
**Endpoint:** `PUT /api/v1/tenants/{{tenant_id}}/currencies/EUR`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "currencyCode": "EUR",
    "currencyName": "Euro",
    "isDefault": "0",
    "exchangeRate": 0.90,
    "symbol": "€",
    "status": "0"
}
```

**Expected Result:**
- Status: 200 OK

---

### Step 45: Get Default Currency
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/currencies/default`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 46: Convert Amount
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/currencies/convert?amount=100&fromCurrency=USD&toCurrency=EUR`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK
- Returns converted amount

---

### Step 47: Get All Bundles
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/bundles`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 48: Create Bundle
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/bundles`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "bundleName": "Starter Pack",
    "description": "Everything you need to get started",
    "bundlePrice": 99.99,
    "discount": 10.00,
    "items": [
        {
            "productId": "{{product_id}}",
            "quantity": 1
        }
    ]
}
```

**Expected Result:**
- Status: 201 Created
- Environment variable `bundle_id` is set

---

### Step 49: Get Bundle by ID
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/bundles/{{bundle_id}}`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 50: Update Bundle
**Endpoint:** `PUT /api/v1/tenants/{{tenant_id}}/bundles/{{bundle_id}}`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "bundleName": "Starter Pack Pro",
    "description": "Everything you need to get started - Premium Edition",
    "bundlePrice": 149.99,
    "discount": 15.00,
    "items": [
        {
            "productId": "{{product_id}}",
            "quantity": 2
        }
    ]
}
```

**Expected Result:**
- Status: 200 OK

---

### Step 51: Calculate Bundle Price
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/bundles/calculate-price`
**Auth:** Bearer Token

**Sample Data:**
```json
[
    {
        "productId": "{{product_id}}",
        "quantity": 1
    }
]
```

**Expected Result:**
- Status: 200 OK
- Returns calculated price

---

### Step 52: Add Product to Bundle
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/bundles/{{bundle_id}}/products/{{product_id}}?quantity=1`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 53: Update Product Quantity in Bundle
**Endpoint:** `PUT /api/v1/tenants/{{tenant_id}}/bundles/{{bundle_id}}/products/{{product_id}}/quantity?quantity=2`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 54: Remove Product from Bundle
**Endpoint:** `DELETE /api/v1/tenants/{{tenant_id}}/bundles/{{bundle_id}}/products/{{product_id}}`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 55: Get Bundle Count
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/bundles/count`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK
- Returns count

---

### Step 56: Get All Orders (Admin)
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/orders`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 57: Get Orders by Status
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/orders?status=PENDING`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 58: Get All Payments (Admin)
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/payments`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 59: Get Payments by Status
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/payments?status=COMPLETED`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 60: Get All Users (Admin)
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/users`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 61: Get Active Users
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/users?status=ACTIVE`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 62: Get Verified Reviews (Admin)
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/reviews/verified`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 63: Create Digital Product Details
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/digital-product-details`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "productId": "{{product_id}}",
    "fileUrl": "https://downloads.example.com/product123.zip",
    "fileSize": 524288000,
    "fileFormat": "ZIP",
    "version": "1.0.0",
    "licenseType": "Single User"
}
```

**Expected Result:**
- Status: 201 Created

---

### Step 64: Get All Digital Product Details
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/digital-product-details`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 65: Get Digital Product Details by ID
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/digital-product-details/{{product_id}}`
**Auth:** Bearer Token

**Expected Result:**
- Status: 200 OK

---

### Step 66: Update Digital Product Details
**Endpoint:** `PUT /api/v1/tenants/{{tenant_id}}/digital-product-details/{{product_id}}`
**Auth:** Bearer Token

**Sample Data:**
```json
{
    "fileUrl": "https://downloads.example.com/product123-v2.zip",
    "fileSize": 628145152,
    "fileFormat": "ZIP",
    "version": "2.0.0",
    "licenseType": "Single User"
}
```

**Expected Result:**
- Status: 200 OK

---

## Collection 3: User Operations (Customer)

### Step 67: Register via Auth Endpoint
**Endpoint:** `POST /api/v1/auth/signup`
**Auth:** None
**Content-Type:** multipart/form-data

**Sample Data (Form Data):**
```
tenantId: {{tenant_id}}
username: john.doe
password: John@123
firstName: John
lastName: Doe
email: john.doe@example.com
```

**Expected Result:**
- Status: 201 Created
- Environment variable `user_id` is set

---

### Step 68: User Login
**Endpoint:** `POST /api/v1/auth/login`
**Auth:** None

**Sample Data:**
```json
{
    "username": "john.doe",
    "password": "John@123"
}
```

**Expected Result:**
- Status: 200 OK
- Environment variables `user_token` and `tenant_id` are set

**Note:** Update Collection 3 auth to use `{{user_token}}` instead of `{{auth_token}}`

---

### Step 69: Get Current User Info
**Endpoint:** `GET /api/v1/auth/me`
**Auth:** Bearer Token ({{user_token}})

**Expected Result:**
- Status: 200 OK
- Returns current user details

---

### Step 70: Browse Products (Public - No Auth)
**Endpoint:** `GET /api/v1/public/tenants/{{tenant_id}}/products?page=0&size=20`
**Auth:** None

**Expected Result:**
- Status: 200 OK
- Returns paginated products

---

### Step 71: Search Products (Public)
**Endpoint:** `GET /api/v1/public/tenants/{{tenant_id}}/products/search?query=phone`
**Auth:** None

**Expected Result:**
- Status: 200 OK
- Returns matching products

---

### Step 72: Get Product by ID (Public)
**Endpoint:** `GET /api/v1/public/tenants/{{tenant_id}}/products/{{product_id}}`
**Auth:** None

**Expected Result:**
- Status: 200 OK

---

### Step 73: Get All Categories (Public)
**Endpoint:** `GET /api/v1/public/tenants/{{tenant_id}}/categories`
**Auth:** None

**Expected Result:**
- Status: 200 OK

---

### Step 74: Get Category by ID (Public)
**Endpoint:** `GET /api/v1/public/tenants/{{tenant_id}}/categories/{{category_id}}`
**Auth:** None

**Expected Result:**
- Status: 200 OK

---

### Step 75: Get All Products (Authenticated)
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/products`
**Auth:** Bearer Token ({{user_token}})

**Expected Result:**
- Status: 200 OK

---

### Step 76: Get Product by ID (Authenticated)
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/products/{{product_id}}`
**Auth:** Bearer Token ({{user_token}})

**Expected Result:**
- Status: 200 OK

---

### Step 77: Get Products by Category
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/products?categoryId={{category_id}}`
**Auth:** Bearer Token ({{user_token}})

**Expected Result:**
- Status: 200 OK

---

### Step 78: Search Products
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/products?keyword=iphone`
**Auth:** Bearer Token ({{user_token}})

**Expected Result:**
- Status: 200 OK

---

### Step 79: Validate Discount Code
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/discounts/validate`
**Auth:** Bearer Token ({{user_token}})

**Sample Data:**
```json
{
    "discountCode": "WELCOME10",
    "orderAmount": 100.00,
    "userId": "{{user_id}}"
}
```

**Expected Result:**
- Status: 200 OK
- Returns validation result with discount details

---

### Step 80: Create Order
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/orders`
**Auth:** Bearer Token ({{user_token}})

**Sample Data:**
```json
{
    "userId": "{{user_id}}",
    "currency": "USD",
    "totalAmount": 1199.00,
    "exchangeRate": 1.0,
    "discountCode": "WELCOME10",
    "orderItems": [
        {
            "productId": "{{product_id}}",
            "priceAtPurchase": 1199.00,
            "licenseKey": "IPHONE-15-PRO-12345"
        }
    ]
}
```

**Expected Result:**
- Status: 201 Created
- Environment variable `order_id` is set

---

### Step 81: Get My Orders
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/orders?userId={{user_id}}`
**Auth:** Bearer Token ({{user_token}})

**Expected Result:**
- Status: 200 OK
- Returns user's orders

---

### Step 82: Get Order by ID
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/orders/{{order_id}}`
**Auth:** Bearer Token ({{user_token}})

**Expected Result:**
- Status: 200 OK

---

### Step 83: Create Payment
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/payments`
**Auth:** Bearer Token ({{user_token}})

**Sample Data:**
```json
{
    "orderId": "{{order_id}}",
    "amount": 1199.00,
    "currency": "USD",
    "paymentMethod": "stripe"
}
```

**Expected Result:**
- Status: 201 Created
- Environment variable `payment_id` is set
- Returns payment intent details

---

### Step 84: Get Payment Status
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/payments/{{payment_id}}`
**Auth:** Bearer Token ({{user_token}})

**Expected Result:**
- Status: 200 OK
- Returns payment details

---

### Step 85: Confirm Payment (Admin Required)
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/payments/{{payment_id}}/confirm`
**Auth:** Bearer Token ({{auth_token}})
**Note:** Switch back to admin token for this

**Expected Result:**
- Status: 200 OK
- Payment status updated to SUCCESSFUL
- Order status updated to PROCESSING

---

### Step 86: Create Review for Product
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/reviews`
**Auth:** Bearer Token ({{user_token}})

**Sample Data:**
```json
{
    "productId": "{{product_id}}",
    "rating": 5,
    "comment": "Excellent product! The iPhone 15 Pro Max exceeded my expectations. Amazing camera quality and performance."
}
```

**Expected Result:**
- Status: 201 Created
- Environment variable `review_id` is set

---

### Step 87: Get Reviews for Product
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/reviews/product/{{product_id}}`
**Auth:** Bearer Token ({{user_token}})

**Expected Result:**
- Status: 200 OK

---

### Step 88: Get My Reviews
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/reviews/user/{{user_id}}`
**Auth:** Bearer Token ({{user_token}})

**Expected Result:**
- Status: 200 OK

---

### Step 89: Update My Review
**Endpoint:** `PUT /api/v1/tenants/{{tenant_id}}/reviews/{{review_id}}`
**Auth:** Bearer Token ({{user_token}})

**Sample Data:**
```json
{
    "productId": "{{product_id}}",
    "rating": 4,
    "comment": "Great product overall! Updated my review after using it for a month. Battery life is excellent."
}
```

**Expected Result:**
- Status: 200 OK

---

### Step 90: Get Product Rating Statistics
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/reviews/product/{{product_id}}/rating`
**Auth:** Bearer Token ({{user_token}})

**Expected Result:**
- Status: 200 OK
- Returns average rating and count

---

### Step 91: Get Product Reviews (Public)
**Endpoint:** `GET /api/v1/public/tenants/{{tenant_id}}/reviews/product/{{product_id}}`
**Auth:** None

**Expected Result:**
- Status: 200 OK

---

### Step 92: Get Product Rating (Public)
**Endpoint:** `GET /api/v1/public/tenants/{{tenant_id}}/reviews/product/{{product_id}}/rating`
**Auth:** None

**Expected Result:**
- Status: 200 OK

---

### Step 93: Get Published Pages (Public)
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/pages/published`
**Auth:** None

**Expected Result:**
- Status: 200 OK

---

### Step 94: Get Page by Slug - About Us (Public)
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/pages/slug/about-us`
**Auth:** None

**Expected Result:**
- Status: 200 OK

---

### Step 95: Get Page by Slug - Privacy Policy (Public)
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/pages/slug/privacy-policy`
**Auth:** None

**Expected Result:**
- Status: 200 OK

---

### Step 96: Get Current User (via /users/me)
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/users/me`
**Auth:** Bearer Token ({{user_token}})

**Expected Result:**
- Status: 200 OK

---

### Step 97: Get My Profile by ID
**Endpoint:** `GET /api/v1/tenants/{{tenant_id}}/users/{{user_id}}`
**Auth:** Bearer Token ({{user_token}})

**Expected Result:**
- Status: 200 OK

---

### Step 98: Update My Profile
**Endpoint:** `PUT /api/v1/tenants/{{tenant_id}}/users/{{user_id}}`
**Auth:** Bearer Token ({{user_token}})

**Sample Data:**
```json
{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe.updated@example.com",
    "phone": "+1-555-5678"
}
```

**Expected Result:**
- Status: 200 OK

---

### Step 99: Verify Review (Admin Required)
**Endpoint:** `PUT /api/v1/tenants/{{tenant_id}}/reviews/{{review_id}}/verify`
**Auth:** Bearer Token ({{auth_token}})
**Note:** Switch to admin token

**Expected Result:**
- Status: 200 OK

---

### Step 100: Forgot Password
**Endpoint:** `POST /api/v1/auth/forgot-password`
**Auth:** None

**Sample Data:**
```json
{
    "email": "john.doe@example.com"
}
```

**Expected Result:**
- Status: 200 OK
- Password reset email sent

---

## Additional Admin Operations (Collection 2)

### Step 101: Complete Order (Admin)
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/orders/{{order_id}}/complete`
**Auth:** Bearer Token ({{auth_token}})

**Expected Result:**
- Status: 200 OK
- Order status updated to COMPLETED

---

### Step 102: Cancel Order (Admin)
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/orders/{{order_id}}/cancel`
**Auth:** Bearer Token ({{auth_token}})

**Expected Result:**
- Status: 200 OK
- Order status updated to CANCELLED

---

### Step 103: Refund Order (Admin)
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/orders/{{order_id}}/refund`
**Auth:** Bearer Token ({{auth_token}})

**Expected Result:**
- Status: 200 OK
- Order refunded

---

### Step 104: Partial Refund Payment (Admin)
**Endpoint:** `POST /api/v1/tenants/{{tenant_id}}/payments/{{payment_id}}/partial-refund`
**Auth:** Bearer Token ({{auth_token}})

**Sample Data:**
```json
{
    "refundAmount": 100.00,
    "reason": "Partial refund requested by customer"
}
```

**Expected Result:**
- Status: 200 OK
- Partial refund processed

---

### Step 105: Delete Bundle (Admin)
**Endpoint:** `DELETE /api/v1/tenants/{{tenant_id}}/bundles/{{bundle_id}}`
**Auth:** Bearer Token ({{auth_token}})

**Expected Result:**
- Status: 204 No Content

---

### Step 106: Delete Discount (Admin)
**Endpoint:** `DELETE /api/v1/tenants/{{tenant_id}}/discounts/{{discount_id}}`
**Auth:** Bearer Token ({{auth_token}})

**Expected Result:**
- Status: 204 No Content

---

### Step 107: Delete Currency (Admin)
**Endpoint:** `DELETE /api/v1/tenants/{{tenant_id}}/currencies/EUR`
**Auth:** Bearer Token ({{auth_token}})

**Expected Result:**
- Status: 204 No Content

---

### Step 108: Delete Digital Product Details (Admin)
**Endpoint:** `DELETE /api/v1/tenants/{{tenant_id}}/digital-product-details/{{product_id}}`
**Auth:** Bearer Token ({{auth_token}})

**Expected Result:**
- Status: 204 No Content

---

### Step 109: Delete Product (Admin)
**Endpoint:** `DELETE /api/v1/tenants/{{tenant_id}}/products/{{product_id}}`
**Auth:** Bearer Token ({{auth_token}})

**Expected Result:**
- Status: 204 No Content

---

### Step 110: Delete Category (Admin)
**Endpoint:** `DELETE /api/v1/tenants/{{tenant_id}}/categories/{{category_id}}`
**Auth:** Bearer Token ({{auth_token}})

**Expected Result:**
- Status: 204 No Content

---

### Step 111: Delete User (Admin)
**Endpoint:** `DELETE /api/v1/tenants/{{tenant_id}}/users/{{user_id}}`
**Auth:** Bearer Token ({{auth_token}})

**Expected Result:**
- Status: 204 No Content

---

### Step 112: Delete Tenant (Platform Admin)
**Endpoint:** `DELETE /api/v1/tenants/{{tenant_id}}`
**Auth:** Bearer Token ({{auth_token}})
**Collection:** 1

**Expected Result:**
- Status: 204 No Content

---

## Testing Checklist

### Before Testing:
- [ ] MySQL database is running
- [ ] Redis is running
- [ ] Spring Boot application is running
- [ ] Postman environment is configured
- [ ] All three collections are imported

### During Testing:
- [ ] Test Collection 1 first (creates tenant)
- [ ] Test Collection 2 second (admin operations)
- [ ] Test Collection 3 last (user operations)
- [ ] Verify environment variables are auto-populated
- [ ] Check response status codes match expected
- [ ] Verify data is persisted in database

### After Testing:
- [ ] Check logs for any errors
- [ ] Verify emails were sent (check console logs)
- [ ] Confirm all CRUD operations worked
- [ ] Test cleanup (delete operations) if needed

---

## Common Issues & Solutions

### Issue 1: 401 Unauthorized
**Solution:** Ensure you're using the correct token (`{{auth_token}}` for admin, `{{user_token}}` for users)

### Issue 2: 404 Not Found
**Solution:** Verify the resource ID exists and environment variable is set correctly

### Issue 3: 400 Bad Request
**Solution:** Check request body matches sample data format exactly

### Issue 4: 500 Internal Server Error
**Solution:** Check application logs for stack trace

### Issue 5: Token Expired
**Solution:** Re-run the login endpoint to get a fresh token

---

## Notes

- **Order of Execution:** Follow the step numbers sequentially for best results
- **Dependencies:** Some endpoints depend on previous steps (e.g., create before update/delete)
- **Environment Variables:** Most IDs are auto-populated by test scripts
- **Tokens:** Switch between `{{auth_token}}` (admin) and `{{user_token}}` (user) as needed
- **Sample Data:** Modify sample data as needed for your testing scenarios
- **Stripe Testing:** Use test payment method `pm_card_visa` for Stripe payments
- **Timestamps:** Use ISO 8601 format for dates (e.g., `2024-01-01T00:00:00`)

---

**Happy Testing! 🚀**
