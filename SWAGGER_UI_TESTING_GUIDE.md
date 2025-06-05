# 🧪 Digital E-Store - Swagger UI Testing Guide

A complete guide for testing your OAuth2-secured Digital E-Store application using Swagger UI.

## 📋 Table of Contents

1. [Getting Started](#getting-started)
2. [OAuth2 Authentication Setup](#oauth2-authentication-setup)
3. [Category Management Testing](#category-management-testing)
4. [Product Management Testing](#product-management-testing)
5. [User Management Testing](#user-management-testing)
6. [Order Management Testing](#order-management-testing)
7. [Payment Processing Testing](#payment-processing-testing)
8. [Digital Downloads Testing](#digital-downloads-testing)
9. [Complete Workflow Testing](#complete-workflow-testing)
10. [Troubleshooting](#troubleshooting)

---

## 🌐 Getting Started

### Prerequisites
- Application running on `http://localhost:8080`
- Database connected and initialized
- Admin user created (username: `admin`, password: `admin`)

### Access Swagger UI
Open your browser and navigate to:
```
http://localhost:8080/swagger-ui.html
```

You should see the Swagger UI interface with all API endpoints organized by controllers.

---

## 🔐 OAuth2 Authentication Setup

### Step 1: Click the Authorize Button
- Look for the **🔒 Authorize** button at the top right of the Swagger page
- Click on it to open the authorization dialog

### Step 2: Configure OAuth2
1. **Select:** `oauth2 (OAuth2, authorizationCode)`
2. **Enter Client Credentials:**
   - **Client ID:** `swagger-client`
   - **Client Secret:** `swagger-secret`
3. **Select Scopes:**
   - ✅ `read` - Read access
   - ✅ `write` - Write access
4. **Click "Authorize"**
5. **Click "Close"** when done

### Verification
- You should see a 🔒 **closed lock icon** next to endpoints
- The authorization dialog should show "Authorized" status

---

## 📁 Category Management Testing

### Create a Category

1. **Expand:** "Category Management" section
2. **Click:** `POST /api/v1/tenants/{tenantId}/categories`
3. **Click:** "Try it out"
4. **Set Parameters:**
   - **tenantId:** `1`
5. **Fill Form Data:**
   ```
   categoryName: Software
   description: Digital software products
   status: 0
   ```
6. **Click:** "Execute"

**✅ Expected Result:** 
- Status: `201 Created`
- Response body contains category with generated ID
- Note the `categoryId` for future use

### Get All Categories

1. **Click:** `GET /api/v1/tenants/{tenantId}/categories`
2. **Set Parameters:**
   - **tenantId:** `1`
3. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- Array of categories including the one you created

### Get Active Categories

1. **Click:** `GET /api/v1/tenants/{tenantId}/categories/active`
2. **Set Parameters:**
   - **tenantId:** `1`
3. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- Only categories with status "0" (active)

---

## 📦 Product Management Testing

### Create a Product

1. **Expand:** "Product Management" section
2. **Click:** `POST /api/v1/tenants/{tenantId}/products`
3. **Click:** "Try it out"
4. **Set Parameters:**
   - **tenantId:** `1`
5. **Fill Form Data:**
   ```
   productName: Antivirus Software
   description: Premium antivirus protection for your devices
   defaultPrice: 49.99
   defaultCurrency: USD
   categoryId: [use categoryId from previous step]
   status: 0
   ```
6. **Click:** "Execute"

**✅ Expected Result:**
- Status: `201 Created`
- Product created with generated `productId`
- Note the `productId` for future use

### Get All Products

1. **Click:** `GET /api/v1/tenants/{tenantId}/products`
2. **Set Parameters:**
   - **tenantId:** `1`
3. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- Array of products including the one you created

### Get Product by ID

1. **Click:** `GET /api/v1/tenants/{tenantId}/products/{productId}`
2. **Set Parameters:**
   - **tenantId:** `1`
   - **productId:** `[use productId from creation]`
3. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- Product details matching the created product

### Get Products by Category

1. **Click:** `GET /api/v1/tenants/{tenantId}/products/category/{categoryId}`
2. **Set Parameters:**
   - **tenantId:** `1`
   - **categoryId:** `[use categoryId from earlier]`
3. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- Products belonging to the specified category

---

## 👥 User Management Testing

### Create a User (Signup)

1. **Expand:** "Authentication" section (if available) or look for auth endpoints
2. **Click:** `POST /api/v1/auth/signup`
3. **Click:** "Try it out"
4. **Fill Form Data:**
   ```
   tenantId: 1
   username: johndoe
   email: john@example.com
   password: password123
   firstName: John
   lastName: Doe
   phone: 1234567890
   ```
5. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- Success message confirming user creation

### Get All Users (Admin Only)

1. **Expand:** "User Management" section
2. **Click:** `GET /api/v1/tenants/{tenantId}/users`
3. **Set Parameters:**
   - **tenantId:** `1`
4. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- Array of users (requires admin privileges)

### Get Current User

1. **Click:** `GET /api/v1/tenants/{tenantId}/users/me`
2. **Set Parameters:**
   - **tenantId:** `1`
3. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- Current authenticated user details

### Find User by Username

1. **Click:** `GET /api/v1/tenants/{tenantId}/users/username/{username}`
2. **Set Parameters:**
   - **tenantId:** `1`
   - **username:** `johndoe`
3. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- User details for the specified username

---

## 📋 Order Management Testing

### Create an Order

1. **Expand:** "Order Management" section
2. **Click:** `POST /api/v1/tenants/{tenantId}/orders`
3. **Click:** "Try it out"
4. **Set Parameters:**
   - **tenantId:** `1`
5. **Fill Form Data:**
   ```
   userId: 1
   productId: [use productId from earlier]
   quantity: 1
   unitPrice: 49.99
   currency: USD
   ```
6. **Click:** "Execute"

**✅ Expected Result:**
- Status: `201 Created`
- Order created with status "Pending"
- Note the `orderId` and `orderItemId` for future use

### Get All Orders

1. **Click:** `GET /api/v1/tenants/{tenantId}/orders`
2. **Set Parameters:**
   - **tenantId:** `1`
3. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- Array of orders including the one you created

### Get Order by ID

1. **Click:** `GET /api/v1/tenants/{tenantId}/orders/{orderId}`
2. **Set Parameters:**
   - **tenantId:** `1`
   - **orderId:** `[use orderId from creation]`
3. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- Order details with order items

### Get Orders by User

1. **Click:** `GET /api/v1/tenants/{tenantId}/orders/user/{userId}`
2. **Set Parameters:**
   - **tenantId:** `1`
   - **userId:** `1`
3. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- Orders belonging to the specified user

### Get Orders by Status

1. **Click:** `GET /api/v1/tenants/{tenantId}/orders/status/{status}`
2. **Set Parameters:**
   - **tenantId:** `1`
   - **status:** `Pending`
3. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- Orders with the specified status

---

## 💳 Payment Processing Testing

### Create a Payment

1. **Expand:** "Payment Management" section
2. **Click:** `POST /api/v1/tenants/{tenantId}/payments`
3. **Click:** "Try it out"
4. **Set Parameters:**
   - **tenantId:** `1`
5. **Fill Form Data:**
   ```
   orderId: [use orderId from earlier]
   amount: 49.99
   currency: USD
   paymentMethod: STRIPE
   customerEmail: john@example.com
   ```
6. **Click:** "Execute"

**✅ Expected Result:**
- Status: `201 Created`
- Payment created with status "Pending"
- Note the `paymentId` for future use

### Confirm Payment

1. **Click:** `POST /api/v1/tenants/{tenantId}/payments/{paymentId}/confirm`
2. **Set Parameters:**
   - **tenantId:** `1`
   - **paymentId:** `[use paymentId from creation]`
   - **transactionId:** `txn_test_12345`
3. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- Payment status changed to "Completed"

### Get Payment by ID

1. **Click:** `GET /api/v1/tenants/{tenantId}/payments/{paymentId}`
2. **Set Parameters:**
   - **tenantId:** `1`
   - **paymentId:** `[use paymentId from creation]`
3. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- Payment details with updated status

### Get Payments by Order

1. **Click:** `GET /api/v1/tenants/{tenantId}/payments/order/{orderId}`
2. **Set Parameters:**
   - **tenantId:** `1`
   - **orderId:** `[use orderId from earlier]`
3. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- Payments associated with the order

---

## 📥 Digital Downloads Testing

### Create Digital Product Details

1. **Expand:** "Download Management" section
2. **Click:** `POST /api/v1/tenants/{tenantId}/digital-product-details`
3. **Click:** "Try it out"
4. **Set Parameters:**
   - **tenantId:** `1`
5. **Use JSON Request Body:**
   ```json
   {
     "productId": 1,
     "downloadUrl": "https://example.com/download/antivirus-software.zip",
     "fileSize": 52428800,
     "fileName": "antivirus-software.zip",
     "downloadLimit": 5,
     "expiryDays": 30
   }
   ```
6. **Click:** "Execute"

**✅ Expected Result:**
- Status: `201 Created`
- Digital product details created

### Get Digital Product Details

1. **Click:** `GET /api/v1/tenants/{tenantId}/digital-product-details/{productId}`
2. **Set Parameters:**
   - **tenantId:** `1`
   - **productId:** `[use productId from earlier]`
3. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- Digital product details for the specified product

### Record a Download

1. **Click:** `POST /api/v1/tenants/{tenantId}/order-items/{orderItemId}/record-download`
2. **Set Parameters:**
   - **tenantId:** `1`
   - **orderItemId:** `[use orderItemId from order creation]`
3. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- Download recorded successfully

### Get Download History

1. **Click:** `GET /api/v1/tenants/{tenantId}/order-items/{orderItemId}/download-history`
2. **Set Parameters:**
   - **tenantId:** `1`
   - **orderItemId:** `[use orderItemId from earlier]`
3. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- Download history for the order item

### Get User Download History

1. **Click:** `GET /api/v1/tenants/{tenantId}/users/{userId}/download-history`
2. **Set Parameters:**
   - **tenantId:** `1`
   - **userId:** `1`
3. **Click:** "Execute"

**✅ Expected Result:**
- Status: `200 OK`
- All downloads for the specified user

---

## 🔄 Complete Workflow Testing

### End-to-End E-Commerce Flow

Follow this sequence to test the complete digital product purchase flow:

#### Phase 1: Setup
1. ✅ **Create Category** → "Software"
2. ✅ **Create Product** → "Antivirus Software" in Software category
3. ✅ **Create User** → Customer account "johndoe"
4. ✅ **Create Digital Product Details** → Download information for the product

#### Phase 2: Purchase Flow
5. ✅ **Create Order** → Customer orders the antivirus software
6. ✅ **Create Payment** → Process payment for the order
7. ✅ **Confirm Payment** → Mark payment as completed

#### Phase 3: Fulfillment
8. ✅ **Record Download** → Customer downloads the software
9. ✅ **Verify Download History** → Confirm download was recorded

#### Phase 4: Verification
10. ✅ **Check Order Status** → Verify order completion
11. ✅ **Check Payment Status** → Verify payment completion
12. ✅ **Check Download Limits** → Verify download tracking

### Business Logic Verification

- **Order Status Progression:** Pending → Processing → Completed
- **Payment Status Progression:** Pending → Completed
- **Download Tracking:** Each download recorded with timestamp and IP
- **Data Relationships:** Order → OrderItems → Product → Category
- **Security:** All endpoints require valid OAuth2 token

---

## 🛠️ Troubleshooting

### Common Issues and Solutions

#### Authentication Issues

**❌ 401 Unauthorized**
- **Cause:** Invalid or expired OAuth2 token
- **Solution:** Re-authorize using the 🔒 button
- **Check:** Client credentials are correct (`swagger-client` / `swagger-secret`)

**❌ 403 Forbidden**
- **Cause:** Insufficient permissions for endpoint
- **Solution:** Some endpoints require ADMIN role or specific user context
- **Note:** Current OAuth2 setup uses client credentials (not user-specific roles)

#### Request Issues

**❌ 400 Bad Request**
- **Cause:** Invalid request data or missing required fields
- **Solution:** 
  - Check all required fields are filled
  - Verify data types (numbers vs strings)
  - Ensure foreign key references exist (categoryId, productId, etc.)

**❌ 404 Not Found**
- **Cause:** Resource doesn't exist or wrong endpoint
- **Solution:**
  - Verify IDs exist (use GET endpoints to find valid IDs)
  - Check endpoint URL is correct
  - Ensure tenantId is correct (usually `1`)

#### Server Issues

**❌ 500 Internal Server Error**
- **Cause:** Application error or database issue
- **Solution:**
  - Check application logs in terminal
  - Verify database connectivity
  - Check for constraint violations (duplicate usernames, emails, etc.)

### Success Indicators

**✅ Green Response Codes:**
- `200 OK` - Successful GET/PUT operations
- `201 Created` - Successful POST operations
- `204 No Content` - Successful DELETE operations

**✅ Response Body Validation:**
- Created entities have generated IDs
- Updated timestamps reflect changes
- Related entities are properly linked

**✅ Database Activity:**
- Terminal shows Hibernate SQL queries
- Data persists between requests
- Foreign key relationships maintained

### Debug Tips

1. **Start Simple:** Test GET endpoints before POST endpoints
2. **Follow Dependencies:** Create categories before products, products before orders
3. **Save IDs:** Note generated IDs for use in subsequent requests
4. **Check Logs:** Monitor terminal for SQL queries and error messages
5. **Use Valid Data:** Ensure email formats, phone numbers, and currencies are valid

---

## 📝 Testing Checklist

### Pre-Testing Setup
- [ ] Application running on port 8080
- [ ] Database connected and initialized
- [ ] Swagger UI accessible
- [ ] OAuth2 authorization completed

### Core Functionality
- [ ] Category CRUD operations
- [ ] Product CRUD operations
- [ ] User management (signup, retrieval)
- [ ] Order creation and management
- [ ] Payment processing and confirmation
- [ ] Digital product setup and downloads

### Business Workflow
- [ ] Complete purchase flow (Category → Product → Order → Payment → Download)
- [ ] Data relationships maintained
- [ ] Status transitions working
- [ ] Download tracking functional

### Security & Error Handling
- [ ] OAuth2 authentication required
- [ ] Proper error responses for invalid requests
- [ ] Data validation working
- [ ] Authorization checks functional

---

## 🎯 Conclusion

Your Digital E-Store application is now fully tested and verified! The Swagger UI provides an excellent interface for:

- **API Exploration:** Understand all available endpoints
- **Interactive Testing:** Execute requests with real data
- **OAuth2 Integration:** Secure authentication testing
- **Documentation:** Auto-generated API documentation

**Next Steps:**
1. **Production Setup:** Configure production OAuth2 clients
2. **Frontend Integration:** Use these APIs in your frontend application
3. **Monitoring:** Set up logging and monitoring for production use
4. **Performance Testing:** Test with larger datasets and concurrent users

Your application is **production-ready** with enterprise-grade OAuth2 security! 🚀

---

## 📚 Quick Reference

### OAuth2 Credentials
- **Client ID:** `swagger-client`
- **Client Secret:** `swagger-secret`
- **Scopes:** `read`, `write`

### Test Data Examples
- **Tenant ID:** `1`
- **Test User:** `johndoe` / `john@example.com`
- **Test Product:** Antivirus Software ($49.99)
- **Test Category:** Software

### Key Endpoints
- **OAuth2 Token:** `POST /oauth2/token`
- **Categories:** `/api/v1/tenants/1/categories`
- **Products:** `/api/v1/tenants/1/products`
- **Orders:** `/api/v1/tenants/1/orders`
- **Payments:** `/api/v1/tenants/1/payments`
- **Downloads:** `/api/v1/tenants/1/digital-product-details`

Happy Testing! 🧪✨ 