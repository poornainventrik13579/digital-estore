# Tenant API Reference

Base URL: `http://localhost:8080`

---

## Authentication

### 1. Tenant Signup
```
POST /api/v1/tenants/auth/signup
```
```json
{
  "shopName": "TechHub Digital Store",
  "shopEmail": "admin@techhub.com",
  "password": "Admin@12345",
  "shopPhone": "+1-555-0100",
  "shopLogo": "https://techhub.com/assets/logo.png",
  "domainName": "techhub.example.com",
  "subdomain": "techhub",
  "countryRegion": "United States",
  "baseCurrency": "USD",
  "multiCurrency": false,
  "taxId": "US-987654321",
  "timezone": "America/New_York"
}
```

### 2. Tenant Login
```
POST /api/v1/tenants/auth/login
```
```json
{
  "email": "admin@techhub.com",
  "password": "Admin@12345"
}
```

### 3. Get Current Tenant Profile
```
GET /api/v1/tenants/auth/me
Authorization: Bearer {token}
```

### 4. Check Email Exists
```
GET /api/v1/tenants/auth/check/email/{email}
```

### 5. Check Subdomain Exists
```
GET /api/v1/tenants/auth/check/subdomain/{subdomain}
```

### 6. Check Domain Exists
```
GET /api/v1/tenants/auth/check/domain/{domainName}
```

---

## Store Management

### 5. Check Store Status
```
GET /api/v1/public/store/status
```

### 6. Check Subdomain Availability
```
GET /api/v1/public/store/subdomain/{subdomain}/availability
```

### 7. Get Public Store Info
```
GET /api/v1/public/store/{subdomain}
```

---

## Products

### 8. Create Product
```
POST /api/v1/tenants/{tenantId}/products
Authorization: Bearer {token}
```
```json
{
  "name": "ProCRM Enterprise",
  "slug": "procrm-enterprise",
  "description": "Complete CRM Solution",
  "shortDescription": "Professional CRM software",
  "sku": "SOFT-CRM-ENT-001",
  "categoryId": 2,
  "basePrice": 499.99,
  "salePrice": 399.99,
  "currency": "USD",
  "stockQuantity": 9999,
  "isActive": true,
  "isFeatured": true,
  "imageUrl": "https://techhub.com/products/procrm-main.jpg",
  "tags": ["CRM", "Business", "Enterprise"]
}
```

### 9. Get All Products
```
GET /api/v1/tenants/{tenantId}/products?page=0&size=20
Authorization: Bearer {token}
```

### 10. Get Product by ID
```
GET /api/v1/tenants/{tenantId}/products/{productId}
Authorization: Bearer {token}
```

### 11. Get Product by Slug
```
GET /api/v1/tenants/{tenantId}/products/slug/{slug}
Authorization: Bearer {token}
```

### 12. Get Product by SKU
```
GET /api/v1/tenants/{tenantId}/products/sku/{sku}
Authorization: Bearer {token}
```

### 13. Update Product
```
PUT /api/v1/tenants/{tenantId}/products/{productId}
Authorization: Bearer {token}
```
```json
{
  "salePrice": 349.99,
  "description": "Updated description",
  "isFeatured": true
}
```

### 14. Update Product Stock
```
PUT /api/v1/tenants/{tenantId}/products/{productId}/stock
Authorization: Bearer {token}
```
```json
{
  "stockQuantity": 10000
}
```

### 15. Get Featured Products
```
GET /api/v1/tenants/{tenantId}/products/featured
Authorization: Bearer {token}
```

### 16. Search Products
```
GET /api/v1/tenants/{tenantId}/products/search?keyword=CRM&page=0&size=10
Authorization: Bearer {token}
```

### 17. Get Products by Category
```
GET /api/v1/tenants/{tenantId}/products/category/{categoryId}?page=0&size=10
Authorization: Bearer {token}
```

### 18. Get Low Stock Products
```
GET /api/v1/tenants/{tenantId}/products/low-stock?threshold=100
Authorization: Bearer {token}
```

### 19. Get Out of Stock Products
```
GET /api/v1/tenants/{tenantId}/products/out-of-stock
Authorization: Bearer {token}
```

### 20. Delete Product
```
DELETE /api/v1/tenants/{tenantId}/products/{productId}
Authorization: Bearer {token}
```

---

## Digital Products

### 21. Add Digital Details
```
POST /api/v1/tenants/{tenantId}/products/{productId}/digital
Authorization: Bearer {token}
```
```json
{
  "fileUrl": "https://downloads.techhub.com/procrm-v3.5.0.zip",
  "fileName": "ProCRM-Enterprise-v3.5.0.zip",
  "fileSize": 157286400,
  "fileType": "application/zip",
  "version": "3.5.0",
  "releaseNotes": "New features and bug fixes",
  "downloadLimit": 10,
  "expirationDays": 365,
  "requiresLicenseKey": true
}
```

### 22. Get Digital Product Details
```
GET /api/v1/tenants/{tenantId}/products/{productId}/digital
Authorization: Bearer {token}
```

### 23. Update Digital Product
```
PUT /api/v1/tenants/{tenantId}/products/{productId}/digital
Authorization: Bearer {token}
```
```json
{
  "version": "3.6.0",
  "releaseNotes": "Major update with new features",
  "fileUrl": "https://downloads.techhub.com/procrm-v3.6.0.zip",
  "downloadLimit": 15
}
```

### 24. Delete Digital Product
```
DELETE /api/v1/tenants/{tenantId}/products/{productId}/digital
Authorization: Bearer {token}
```

---

## Categories

### 25. Create Category
```
POST /api/v1/tenants/{tenantId}/categories
Authorization: Bearer {token}
```
```json
{
  "name": "Software",
  "slug": "software",
  "description": "Professional software applications",
  "displayOrder": 1,
  "isActive": true
}
```

### 26. Get All Categories
```
GET /api/v1/tenants/{tenantId}/categories
Authorization: Bearer {token}
```

### 27. Get Category by ID
```
GET /api/v1/tenants/{tenantId}/categories/{categoryId}
Authorization: Bearer {token}
```

### 28. Get Category by Slug
```
GET /api/v1/tenants/{tenantId}/categories/slug/{slug}
Authorization: Bearer {token}
```

### 29. Update Category
```
PUT /api/v1/tenants/{tenantId}/categories/{categoryId}
Authorization: Bearer {token}
```
```json
{
  "description": "Updated description",
  "displayOrder": 1,
  "isActive": true
}
```

### 30. Get Subcategories
```
GET /api/v1/tenants/{tenantId}/categories/{categoryId}/subcategories
Authorization: Bearer {token}
```

### 31. Get Root Categories
```
GET /api/v1/tenants/{tenantId}/categories/root
Authorization: Bearer {token}
```

### 32. Delete Category
```
DELETE /api/v1/tenants/{tenantId}/categories/{categoryId}
Authorization: Bearer {token}
```

---

## Orders

### 33. Get All Orders
```
GET /api/v1/tenants/{tenantId}/orders?page=0&size=20
Authorization: Bearer {token}
```

### 34. Get Order by ID
```
GET /api/v1/tenants/{tenantId}/orders/{orderId}
Authorization: Bearer {token}
```

### 35. Get Order Summary
```
GET /api/v1/tenants/{tenantId}/orders/{orderId}/summary
Authorization: Bearer {token}
```

### 36. Update Order Status
```
PUT /api/v1/tenants/{tenantId}/orders/{orderId}
Authorization: Bearer {token}
```
```json
{
  "status": "PROCESSING"
}
```

### 37. Cancel Order
```
PUT /api/v1/tenants/{tenantId}/orders/{orderId}/cancel
Authorization: Bearer {token}
```
```json
{
  "cancellationReason": "Customer requested cancellation"
}
```

### 38. Get Orders by Status
```
GET /api/v1/tenants/{tenantId}/orders/status/{status}?page=0&size=10
Authorization: Bearer {token}
```

### 39. Get Orders by Date Range
```
GET /api/v1/tenants/{tenantId}/orders/date-range?startDate=2025-01-01&endDate=2025-01-31
Authorization: Bearer {token}
```

### 40. Get Order Statistics
```
GET /api/v1/tenants/{tenantId}/orders/stats
Authorization: Bearer {token}
```

### 41. Get Revenue Report
```
GET /api/v1/tenants/{tenantId}/orders/revenue?startDate=2025-01-01&endDate=2025-01-31
Authorization: Bearer {token}
```

### 42. Search Orders
```
GET /api/v1/tenants/{tenantId}/orders/search?keyword=ORD-2025-0001
Authorization: Bearer {token}
```

---

## Payments

### 43. Get All Payments
```
GET /api/v1/tenants/{tenantId}/payments
Authorization: Bearer {token}
```

### 44. Get Payment by ID
```
GET /api/v1/tenants/{tenantId}/payments/{paymentId}
Authorization: Bearer {token}
```

### 45. Get Payments for Order
```
GET /api/v1/tenants/{tenantId}/payments/order/{orderId}
Authorization: Bearer {token}
```

### 46. Get Payment Audit Trail
```
GET /api/v1/tenants/{tenantId}/payments/{paymentId}/audit
Authorization: Bearer {token}
```

---

## Refunds

### 47. Create Refund
```
POST /api/v1/tenants/{tenantId}/refunds
Authorization: Bearer {token}
```
```json
{
  "paymentId": 1,
  "orderId": 1,
  "amount": 530.32,
  "reason": "CUSTOMER_REQUEST",
  "reasonDescription": "Customer requested refund",
  "refundMethod": "STRIPE"
}
```

### 48. Get All Refunds
```
GET /api/v1/tenants/{tenantId}/refunds
Authorization: Bearer {token}
```

### 49. Get Refund by ID
```
GET /api/v1/tenants/{tenantId}/refunds/{refundId}
Authorization: Bearer {token}
```

### 50. Get Refunds for Order
```
GET /api/v1/tenants/{tenantId}/refunds/order/{orderId}
Authorization: Bearer {token}
```

### 51. Get Refunds by Status
```
GET /api/v1/tenants/{tenantId}/refunds/status/{status}
Authorization: Bearer {token}
```

---

## Bundles

### 52. Create Bundle
```
POST /api/v1/tenants/{tenantId}/bundles
Authorization: Bearer {token}
```
```json
{
  "name": "Business Essentials Bundle",
  "slug": "business-essentials-bundle",
  "description": "Complete business software package",
  "bundlePrice": 599.99,
  "discountPercentage": 20.0,
  "isActive": true,
  "products": [
    {"productId": 1, "quantity": 1},
    {"productId": 2, "quantity": 1}
  ]
}
```

### 53. Get All Bundles
```
GET /api/v1/tenants/{tenantId}/bundles
Authorization: Bearer {token}
```

### 54. Get Bundle by ID
```
GET /api/v1/tenants/{tenantId}/bundles/{bundleId}
Authorization: Bearer {token}
```

### 55. Update Bundle
```
PUT /api/v1/tenants/{tenantId}/bundles/{bundleId}
Authorization: Bearer {token}
```
```json
{
  "name": "Business Essentials Bundle - Premium",
  "bundlePrice": 549.99,
  "discountPercentage": 25.0
}
```

### 56. Calculate Bundle Price
```
GET /api/v1/tenants/{tenantId}/bundles/{bundleId}/calculate-price
Authorization: Bearer {token}
```

### 57. Update Product Quantity in Bundle
```
PUT /api/v1/tenants/{tenantId}/bundles/{bundleId}/products/{productId}/quantity?quantity=2
Authorization: Bearer {token}
```

### 58. Delete Bundle
```
DELETE /api/v1/tenants/{tenantId}/bundles/{bundleId}
Authorization: Bearer {token}
```

---

## Discounts

### 59. Create Discount
```
POST /api/v1/tenants/{tenantId}/discounts
Authorization: Bearer {token}
```
```json
{
  "code": "LAUNCH2025",
  "description": "Grand Launch 30% Discount",
  "discountType": "PERCENTAGE",
  "discountValue": 30.0,
  "startDate": "2025-01-01T00:00:00",
  "endDate": "2025-03-31T23:59:59",
  "maxUsageCount": 500,
  "minPurchaseAmount": 50.0,
  "maxDiscountAmount": 200.0,
  "isActive": true
}
```

### 60. Get All Discounts
```
GET /api/v1/tenants/{tenantId}/discounts
Authorization: Bearer {token}
```

### 61. Get Discount by Code
```
GET /api/v1/tenants/{tenantId}/discounts/code/{code}
Authorization: Bearer {token}
```

### 62. Get Active Discounts
```
GET /api/v1/tenants/{tenantId}/discounts/active
Authorization: Bearer {token}
```

### 63. Update Discount
```
PUT /api/v1/tenants/{tenantId}/discounts/{discountId}
Authorization: Bearer {token}
```
```json
{
  "discountValue": 35.0,
  "maxUsageCount": 1000
}
```

### 64. Validate Discount
```
POST /api/v1/tenants/{tenantId}/discounts/validate
Authorization: Bearer {token}
```
```json
{
  "code": "LAUNCH2025",
  "purchaseAmount": 499.99
}
```

### 65. Delete Discount
```
DELETE /api/v1/tenants/{tenantId}/discounts/{discountId}
Authorization: Bearer {token}
```

---

## Taxes

### 66. Create Tax
```
POST /api/v1/tenants/{tenantId}/taxes
Authorization: Bearer {token}
```
```json
{
  "code": "US-CA-SALES-TAX",
  "description": "California State Sales Tax",
  "value": 8.25,
  "type": "PERCENTAGE",
  "region": "California",
  "country": "United States",
  "state": "CA",
  "status": "A",
  "defaultFlag": "Y",
  "startDate": "2025-01-01",
  "isCompound": false,
  "taxOrder": 1
}
```

### 67. Get All Taxes
```
GET /api/v1/tenants/{tenantId}/taxes
Authorization: Bearer {token}
```

### 68. Get Tax by ID
```
GET /api/v1/tenants/{tenantId}/taxes/{taxId}
Authorization: Bearer {token}
```

### 69. Get Tax by Code
```
GET /api/v1/tenants/{tenantId}/taxes/code/{code}
Authorization: Bearer {token}
```

### 70. Get Active Taxes
```
GET /api/v1/tenants/{tenantId}/taxes/active
Authorization: Bearer {token}
```

### 71. Get Default Tax
```
GET /api/v1/tenants/{tenantId}/taxes/default
Authorization: Bearer {token}
```

### 72. Update Tax
```
PUT /api/v1/tenants/{tenantId}/taxes/{taxId}
Authorization: Bearer {token}
```
```json
{
  "value": 9.0,
  "description": "Updated Sales Tax"
}
```

### 73. Set Tax as Default
```
PUT /api/v1/tenants/{tenantId}/taxes/{taxId}/default
Authorization: Bearer {token}
```

### 74. Calculate Tax
```
POST /api/v1/tenants/{tenantId}/taxes/calculate
Authorization: Bearer {token}
```
```json
{
  "baseAmount": 100.00
}
```

### 75. Calculate Tax for Date
```
POST /api/v1/tenants/{tenantId}/taxes/calculate/date
Authorization: Bearer {token}
```
```json
{
  "baseAmount": 250.00,
  "date": "2025-02-15"
}
```

### 76. Get Valid Taxes for Date
```
GET /api/v1/tenants/{tenantId}/taxes/valid?date=2025-06-01
Authorization: Bearer {token}
```

### 77. Search Taxes
```
GET /api/v1/tenants/{tenantId}/taxes/search?keyword=California
Authorization: Bearer {token}
```

### 78. Count Active Taxes
```
GET /api/v1/tenants/{tenantId}/taxes/count
Authorization: Bearer {token}
```

### 79. Delete Tax
```
DELETE /api/v1/tenants/{tenantId}/taxes/{taxId}
Authorization: Bearer {token}
```

---

## Currencies

### 80. Create Currency
```
POST /api/v1/tenants/{tenantId}/currencies
Authorization: Bearer {token}
```
```json
{
  "code": "USD",
  "name": "US Dollar",
  "symbol": "$",
  "exchangeRate": 1.0,
  "decimalPlaces": 2,
  "isDefault": true,
  "isActive": true
}
```

### 81. Get All Currencies
```
GET /api/v1/tenants/{tenantId}/currencies
Authorization: Bearer {token}
```

### 82. Get Currency by Code
```
GET /api/v1/tenants/{tenantId}/currencies/{code}
Authorization: Bearer {token}
```

### 83. Get Active Currencies
```
GET /api/v1/tenants/{tenantId}/currencies/active
Authorization: Bearer {token}
```

### 84. Get Default Currency
```
GET /api/v1/tenants/{tenantId}/currencies/default
Authorization: Bearer {token}
```

### 85. Update Currency
```
PUT /api/v1/tenants/{tenantId}/currencies/{code}
Authorization: Bearer {token}
```
```json
{
  "exchangeRate": 0.93,
  "isActive": true
}
```

### 86. Set Default Currency
```
PUT /api/v1/tenants/{tenantId}/currencies/{code}/default
Authorization: Bearer {token}
```

### 87. Delete Currency
```
DELETE /api/v1/tenants/{tenantId}/currencies/{code}
Authorization: Bearer {token}
```

---

## Themes

### 88. Create Theme
```
POST /api/v1/tenants/{tenantId}/themes
Authorization: Bearer {token}
```
```json
{
  "name": "Modern Blue Theme",
  "description": "Clean and professional blue color scheme",
  "primaryColor": "#2563eb",
  "secondaryColor": "#64748b",
  "accentColor": "#f59e0b",
  "backgroundColor": "#ffffff",
  "textColor": "#1e293b",
  "fontFamily": "Inter, system-ui, sans-serif",
  "logoUrl": "https://techhub.com/assets/logo.png",
  "status": "ACTIVE"
}
```

### 89. Get All Themes
```
GET /api/v1/tenants/{tenantId}/themes
Authorization: Bearer {token}
```

### 90. Get Theme by ID
```
GET /api/v1/tenants/{tenantId}/themes/{themeId}
Authorization: Bearer {token}
```

### 91. Get Active Theme
```
GET /api/v1/tenants/{tenantId}/themes/active
Authorization: Bearer {token}
```

### 92. Update Theme
```
PUT /api/v1/tenants/{tenantId}/themes/{themeId}
Authorization: Bearer {token}
```
```json
{
  "primaryColor": "#1d4ed8",
  "accentColor": "#ef4444"
}
```

### 93. Activate Theme
```
PUT /api/v1/tenants/{tenantId}/themes/{themeId}/activate
Authorization: Bearer {token}
```

### 94. Deactivate Theme
```
PUT /api/v1/tenants/{tenantId}/themes/{themeId}/deactivate
Authorization: Bearer {token}
```

### 95. Delete Theme
```
DELETE /api/v1/tenants/{tenantId}/themes/{themeId}
Authorization: Bearer {token}
```

---

## Tenant Users

### 96. Create Tenant User
```
POST /api/v1/tenants/{tenantId}/users
Authorization: Bearer {token}
```
```json
{
  "email": "staff@techhub.com",
  "password": "Staff@12345",
  "firstName": "Mike",
  "lastName": "Wilson",
  "role": "USER",
  "phone": "+1-555-0301",
  "department": "Sales",
  "isActive": true
}
```

### 97. Get All Tenant Users
```
GET /api/v1/tenants/{tenantId}/users
Authorization: Bearer {token}
```

### 98. Get User by ID
```
GET /api/v1/tenants/{tenantId}/users/{userId}
Authorization: Bearer {token}
```

### 99. Update User
```
PUT /api/v1/tenants/{tenantId}/users/{userId}
Authorization: Bearer {token}
```
```json
{
  "firstName": "Michael",
  "lastName": "Wilson",
  "department": "Sales & Marketing"
}
```

### 100. Update User Role
```
PUT /api/v1/tenants/{tenantId}/users/{userId}/role
Authorization: Bearer {token}
```
```json
{
  "role": "TENANT_ADMIN"
}
```

### 101. Get Users by Role
```
GET /api/v1/tenants/{tenantId}/users/role/{role}
Authorization: Bearer {token}
```

### 102. Search Users
```
GET /api/v1/tenants/{tenantId}/users/search?keyword=Mike
Authorization: Bearer {token}
```

### 103. Activate User
```
PUT /api/v1/tenants/{tenantId}/users/{userId}/activate
Authorization: Bearer {token}
```

### 104. Deactivate User
```
PUT /api/v1/tenants/{tenantId}/users/{userId}/deactivate
Authorization: Bearer {token}
```

### 105. Delete User
```
DELETE /api/v1/tenants/{tenantId}/users/{userId}
Authorization: Bearer {token}
```

---

## Reviews

### 106. Get Product Reviews
```
GET /api/v1/tenants/{tenantId}/reviews/product/{productId}
Authorization: Bearer {token}
```

### 107. Get Review by ID
```
GET /api/v1/tenants/{tenantId}/reviews/{reviewId}
Authorization: Bearer {token}
```

### 108. Get Reviews by Rating
```
GET /api/v1/tenants/{tenantId}/reviews/rating/{rating}
Authorization: Bearer {token}
```

### 109. Verify Review
```
PUT /api/v1/tenants/{tenantId}/reviews/{reviewId}/verify
Authorization: Bearer {token}
```

### 110. Flag Review
```
PUT /api/v1/tenants/{tenantId}/reviews/{reviewId}/flag
Authorization: Bearer {token}
```
```json
{
  "flagReason": "INAPPROPRIATE_CONTENT",
  "flagDescription": "Contains promotional links"
}
```

### 111. Approve Review
```
PUT /api/v1/tenants/{tenantId}/reviews/{reviewId}/approve
Authorization: Bearer {token}
```

### 112. Delete Review
```
DELETE /api/v1/tenants/{tenantId}/reviews/{reviewId}
Authorization: Bearer {token}
```

---

## Downloads

### 113. Get All Downloads
```
GET /api/v1/tenants/{tenantId}/downloads
Authorization: Bearer {token}
```

### 114. Get Product Downloads
```
GET /api/v1/tenants/{tenantId}/downloads/product/{productId}
Authorization: Bearer {token}
```

---

## CMS Pages

### 115. Create Page
```
POST /api/v1/tenants/{tenantId}/pages
Authorization: Bearer {token}
```
```json
{
  "title": "About Us",
  "slug": "about-us",
  "content": "<h1>About TechHub</h1><p>Content here</p>",
  "metaTitle": "About Us - TechHub",
  "status": "PUBLISHED",
  "visibility": "PUBLIC",
  "language": "en"
}
```

### 116. Get All Pages
```
GET /api/v1/tenants/{tenantId}/pages
Authorization: Bearer {token}
```

### 117. Get Page by ID
```
GET /api/v1/tenants/{tenantId}/pages/{pageId}
Authorization: Bearer {token}
```

### 118. Get Page by Slug
```
GET /api/v1/tenants/{tenantId}/pages/slug/{slug}
Authorization: Bearer {token}
```

### 119. Get Published Pages
```
GET /api/v1/tenants/{tenantId}/pages/published
Authorization: Bearer {token}
```

### 120. Get Pages by Status
```
GET /api/v1/tenants/{tenantId}/pages/status/{status}
Authorization: Bearer {token}
```

### 121. Get Pages by Visibility
```
GET /api/v1/tenants/{tenantId}/pages/visibility/{visibility}
Authorization: Bearer {token}
```

### 122. Update Page
```
PUT /api/v1/tenants/{tenantId}/pages/{pageId}
Authorization: Bearer {token}
```
```json
{
  "content": "<h1>Updated Content</h1>"
}
```

### 123. Publish Page
```
PUT /api/v1/tenants/{tenantId}/pages/{pageId}/publish
Authorization: Bearer {token}
```

### 124. Unpublish Page
```
PUT /api/v1/tenants/{tenantId}/pages/{pageId}/unpublish
Authorization: Bearer {token}
```

### 125. Search Pages
```
GET /api/v1/tenants/{tenantId}/pages/search?keyword=privacy
Authorization: Bearer {token}
```

### 126. Delete Page
```
DELETE /api/v1/tenants/{tenantId}/pages/{pageId}
Authorization: Bearer {token}
```

---

## Admin Operations

### 127. Get Tenant Details
```
GET /api/admin/tenants/{tenantId}
Authorization: Bearer {admin_token}
```

### 128. Get Tenant by Subdomain
```
GET /api/admin/tenants/subdomain/{subdomain}
Authorization: Bearer {admin_token}
```

### 129. Get Tenant by Domain
```
GET /api/admin/tenants/domain/{domain}
Authorization: Bearer {admin_token}
```

### 130. Update Tenant
```
PUT /api/admin/tenants/{tenantId}
Authorization: Bearer {admin_token}
```
```json
{
  "status": "ACTIVE",
  "subscriptionTier": "PREMIUM"
}
```

### 131. Suspend Tenant
```
PUT /api/admin/tenants/{tenantId}/suspend
Authorization: Bearer {admin_token}
```

### 132. Reactivate Tenant
```
PUT /api/admin/tenants/{tenantId}/reactivate
Authorization: Bearer {admin_token}
```

### 133. Delete Tenant
```
DELETE /api/admin/tenants/{tenantId}
Authorization: Bearer {admin_token}
```

---

**Total Tenant Endpoints: 133**
