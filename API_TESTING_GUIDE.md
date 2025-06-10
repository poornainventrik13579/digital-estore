# Digital E-Store API Testing Guide

This document provides comprehensive testing flows for all Digital E-Store API endpoints including proper sequences, edge cases, and validation scenarios.

## Prerequisites
- Postman or similar API testing tool
- Valid OAuth2 credentials (client_id, client_secret)
- Base URL configured (default: http://localhost:8080)
- Valid tenant_id (default: 1)

## Collection Variables Required
```
base_url = http://localhost:8080
tenant_id = 1
client_id = web-client
client_secret = web-secret
access_token = (auto-populated)
user_id = (auto-populated)
product_id = (auto-populated)
category_id = (auto-populated)
order_id = (auto-populated)
payment_id = (auto-populated)
bundle_id = (auto-populated)
discount_id = (auto-populated)
review_id = (auto-populated)
```

## 1. Authentication Flow

### Basic Authentication Test Sequence
1. Get OAuth2 Token
   - POST /oauth2/token
   - Basic Auth: client_id:client_secret
   - Body: grant_type=client_credentials&scope=read write
   - Expected: 200 OK with access_token

2. User Signup
   - POST /api/v1/auth/signup
   - Body: Form data with user details
   - Expected: 200 OK with success message

### Authentication Edge Cases
- Invalid client credentials
- Expired tokens
- Missing authorization headers
- Invalid grant types

## 2. Category Management Flow

### Basic Operations
1. Create Category
   - POST /api/v1/tenants/1/categories
   - Body: JSON with categoryName and description
   - Expected: 201 Created

2. Get All Categories
   - GET /api/v1/tenants/1/categories
   - Expected: 200 OK with categories array

3. Get Category by ID
   - GET /api/v1/tenants/1/categories/{id}
   - Expected: 200 OK with category details

4. Get Active Categories
   - GET /api/v1/tenants/1/categories/active
   - Expected: 200 OK with active categories

5. Update Category
   - PUT /api/v1/tenants/1/categories/{id}
   - Body: JSON with updated fields
   - Expected: 200 OK

6. Delete Category
   - DELETE /api/v1/tenants/1/categories/{id}
   - Expected: 204 No Content

### Edge Cases
- Duplicate category names
- Non-existent category (404)
- Invalid data updates
- Delete category with products
- Invalid tenant_id
- Missing required fields

## 3. Product Management Flow

### Basic Operations
1. Create Product
   - POST /api/v1/tenants/1/products
   - Body: JSON with product details
   - Expected: 201 Created

2. Get All Products
   - GET /api/v1/tenants/1/products
   - Expected: 200 OK

3. Get Product by ID
   - GET /api/v1/tenants/1/products/{id}
   - Expected: 200 OK

4. Get Active Products
   - GET /api/v1/tenants/1/products/active
   - Expected: 200 OK

5. Get Products by Category
   - GET /api/v1/tenants/1/products/category/{categoryId}
   - Expected: 200 OK

6. Update Product
   - PUT /api/v1/tenants/1/products/{id}
   - Expected: 200 OK

7. Delete Product
   - DELETE /api/v1/tenants/1/products/{id}
   - Expected: 204 No Content

### Edge Cases
- Invalid category_id
- Negative prices
- Invalid currency codes
- Missing required fields
- Large descriptions

## 4. User Management Flow

### Basic Operations
1. Create User
   - POST /api/v1/tenants/1/users
   - Body: JSON with user details
   - Expected: 201 Created

2. Get All Users (Admin only)
   - GET /api/v1/tenants/1/users
   - Expected: 200 OK

3. Get User by ID
   - GET /api/v1/tenants/1/users/{id}
   - Expected: 200 OK

4. Get Current User
   - GET /api/v1/users/me
   - Expected: 200 OK

5. Find User by Username
   - GET /api/v1/tenants/1/users/username/{username}
   - Expected: 200 OK

6. Find User by Email
   - GET /api/v1/tenants/1/users/email/{email}
   - Expected: 200 OK

7. Get Active Users
   - GET /api/v1/tenants/1/users/active
   - Expected: 200 OK

8. Update User
   - PUT /api/v1/tenants/1/users/{id}
   - Expected: 200 OK

9. Delete User
   - DELETE /api/v1/tenants/1/users/{id}
   - Expected: 204 No Content

### Edge Cases
- Duplicate username/email
- Invalid email format
- Weak passwords
- Unauthorized access
- Invalid phone formats

## 5. Order and Payment Flow

### Complete Purchase Flow
1. Create Order
   - POST /api/v1/tenants/1/orders
   - Body: userId, currency, totalAmount, orderItems
   - Expected: 201 Created (Status: Pending)

2. Verify Order
   - GET /api/v1/tenants/1/orders/{orderId}
   - Expected: 200 OK

3. Create Payment
   - POST /api/v1/tenants/1/payments
   - Body: orderId, amount, currency, paymentMethod
   - Expected: 201 Created (Status: Pending)

4. Confirm Payment
   - POST /api/v1/tenants/1/payments/{paymentId}/confirm
   - Query: transactionId
   - Expected: 200 OK (Status: Completed)

5. Complete Order
   - POST /api/v1/tenants/1/orders/{orderId}/complete
   - Expected: 200 OK (Status: Completed)

### Query Operations
6. Get Orders by User
   - GET /api/v1/tenants/1/orders/user/{userId}
   - Expected: 200 OK

7. Get Orders by Status
   - GET /api/v1/tenants/1/orders/status/{status}
   - Expected: 200 OK

8. Get Payments by Order
   - GET /api/v1/tenants/1/payments/order/{orderId}
   - Expected: 200 OK

9. Get Payments by Status
   - GET /api/v1/tenants/1/payments/status/{status}
   - Expected: 200 OK

### Refund Flow
10. Partial Refund
    - POST /api/v1/tenants/1/payments/{paymentId}/partial-refund
    - Body: amount, reason
    - Expected: 200 OK

11. Full Refund Payment
    - POST /api/v1/tenants/1/payments/{paymentId}/refund
    - Expected: 200 OK

12. Refund Order
    - POST /api/v1/tenants/1/orders/{orderId}/refund
    - Expected: 200 OK

### Alternative Flows
13. Cancel Payment
    - POST /api/v1/tenants/1/payments/{paymentId}/cancel
    - Expected: 200 OK

14. Cancel Order
    - POST /api/v1/tenants/1/orders/{orderId}/cancel
    - Expected: 200 OK

### Edge Cases
- Invalid user_id in order
- Payment amount mismatch
- Confirm confirmed payment
- Complete without payment
- Refund unpaid order
- Excess partial refund
- Cancel completed order
- Multiple payments per order
- Invalid currencies
- Negative amounts

## 6. Bundle Management Flow

### Basic Operations
1. Create Bundle
   - POST /api/v1/tenants/1/bundles
   - Body: bundle details and items
   - Expected: 201 Created

2. Get All Bundles
   - GET /api/v1/tenants/1/bundles
   - Expected: 200 OK

3. Get Active Bundles
   - GET /api/v1/tenants/1/bundles/active
   - Expected: 200 OK

4. Get Bundle by ID
   - GET /api/v1/tenants/1/bundles/{id}
   - Expected: 200 OK

5. Search Bundles
   - GET /api/v1/tenants/1/bundles/search?name=Adobe
   - Expected: 200 OK

6. Calculate Bundle Price
   - POST /api/v1/tenants/1/bundles/calculate-price
   - Body: array of bundle items
   - Expected: 200 OK

7. Get Bundles with Product
   - GET /api/v1/tenants/1/bundles/product/{productId}
   - Expected: 200 OK

8. Update Bundle
   - PUT /api/v1/tenants/1/bundles/{id}
   - Expected: 200 OK

### Bundle Item Management
9. Add Product to Bundle
   - POST /api/v1/tenants/1/bundles/{bundleId}/products/{productId}
   - Query: quantity
   - Expected: 200 OK

10. Update Product Quantity
    - PUT /api/v1/tenants/1/bundles/{bundleId}/products/{productId}/quantity
    - Query: quantity
    - Expected: 200 OK

11. Remove Product from Bundle
    - DELETE /api/v1/tenants/1/bundles/{bundleId}/products/{productId}
    - Expected: 200 OK

12. Get Bundle Count
    - GET /api/v1/tenants/1/bundles/count
    - Expected: 200 OK

13. Delete Bundle
    - DELETE /api/v1/tenants/1/bundles/{id}
    - Expected: 204 No Content

### Edge Cases
- Non-existent products in bundle
- Duplicate products
- Zero/negative quantities
- Remove non-existent products
- Currency conflicts
- Invalid discount percentages
- Empty bundles

## 7. Discount Code Management Flow

### Basic Operations
1. Create Discount Code
   - POST /api/v1/tenants/1/discounts
   - Body: code, type, value, dates
   - Expected: 201 Created

2. Get All Discounts
   - GET /api/v1/tenants/1/discounts
   - Expected: 200 OK

3. Get Active Discounts
   - GET /api/v1/tenants/1/discounts/active
   - Expected: 200 OK

4. Get Discount by ID
   - GET /api/v1/tenants/1/discounts/{id}
   - Expected: 200 OK

5. Get Discount by Code
   - GET /api/v1/tenants/1/discounts/code/{code}
   - Expected: 200 OK

6. Update Discount
   - PUT /api/v1/tenants/1/discounts/{id}
   - Expected: 200 OK

7. Validate Discount
   - POST /api/v1/tenants/1/discounts/validate
   - Body: code, orderAmount
   - Expected: 200 OK

8. Get Usage Statistics
   - POST /api/v1/tenants/1/discounts/{id}/usage
   - Expected: 200 OK

9. Cleanup Expired
   - POST /api/v1/tenants/1/discounts/cleanup-expired
   - Expected: 200 OK

10. Delete Discount
    - DELETE /api/v1/tenants/1/discounts/{id}
    - Expected: 204 No Content

### Edge Cases
- Duplicate codes
- Invalid date ranges
- Negative values
- Over 100% discounts
- Expired code validation
- Insufficient order amount
- Exceed usage limits
- Invalid types

## 8. Review Management Flow

### Basic Operations
1. Create Review
   - POST /api/v1/reviews
   - Headers: X-Tenant-ID, Authorization
   - Body: productId, rating, comment
   - Expected: 201 Created

2. Get Product Reviews
   - GET /api/v1/reviews/product/{productId}
   - Headers: X-Tenant-ID
   - Expected: 200 OK

3. Get User Reviews
   - GET /api/v1/reviews/user/{userId}
   - Headers: X-Tenant-ID, Authorization
   - Expected: 200 OK

4. Get Review by ID
   - GET /api/v1/reviews/{id}
   - Headers: X-Tenant-ID
   - Expected: 200 OK

5. Get Product Rating
   - GET /api/v1/reviews/product/{productId}/rating
   - Headers: X-Tenant-ID
   - Expected: 200 OK

6. Get Verified Reviews
   - GET /api/v1/reviews/verified
   - Headers: X-Tenant-ID
   - Expected: 200 OK

7. Update Review
   - PUT /api/v1/reviews/{id}
   - Headers: X-Tenant-ID, Authorization
   - Expected: 200 OK

8. Verify Review (Admin)
   - PUT /api/v1/reviews/{id}/verify
   - Headers: X-Tenant-ID, Authorization
   - Expected: 200 OK

9. Delete Review
   - DELETE /api/v1/reviews/{id}
   - Headers: X-Tenant-ID, Authorization
   - Expected: 204 No Content

### Edge Cases
- Non-existent product reviews
- Duplicate user reviews
- Invalid ratings (outside 1-5)
- Long comments
- Verify verified reviews
- Delete non-existent reviews
- Unauthorized access

## 9. Digital Downloads Flow

### Download Tracking
1. Record Download
   - POST /api/v1/tenants/1/order-items/{id}/record-download
   - Expected: 200 OK

2. Get Order Item Downloads
   - GET /api/v1/tenants/1/order-items/{id}/download-history
   - Expected: 200 OK

3. Get User Downloads
   - GET /api/v1/tenants/1/users/{userId}/download-history
   - Expected: 200 OK

### Digital Product Management
4. Create Digital Details
   - POST /api/v1/tenants/1/digital-product-details
   - Body: productId, fileUrl, fileSize, etc.
   - Expected: 201 Created

5. Get Digital Details
   - GET /api/v1/tenants/1/digital-product-details/{productId}
   - Expected: 200 OK

6. Get All Digital Details
   - GET /api/v1/tenants/1/digital-product-details
   - Expected: 200 OK

7. Update Digital Details
   - PUT /api/v1/tenants/1/digital-product-details/{productId}
   - Expected: 200 OK

8. Delete Digital Details
   - DELETE /api/v1/tenants/1/digital-product-details/{productId}
   - Expected: 204 No Content

### Edge Cases
- Non-existent order items
- Invalid file URLs
- Negative file sizes
- Non-existent products

## 10. Currency Management Flow

### Basic Operations
1. Create Currency
   - POST /api/currencies
   - Headers: X-Tenant-ID
   - Body: code, name, rate, symbol
   - Expected: 201 Created

2. Get All Currencies
   - GET /api/currencies
   - Headers: X-Tenant-ID
   - Expected: 200 OK

3. Get Currency by Code
   - GET /api/currencies/{code}
   - Headers: X-Tenant-ID
   - Expected: 200 OK

4. Get Default Currency
   - GET /api/currencies/default
   - Headers: X-Tenant-ID
   - Expected: 200 OK

5. Update Currency
   - PUT /api/currencies/{code}
   - Headers: X-Tenant-ID
   - Expected: 200 OK

6. Convert Amount
   - GET /api/currencies/convert?amount=100&fromCurrency=USD&toCurrency=EUR
   - Headers: X-Tenant-ID
   - Expected: 200 OK

7. Get Exchange Rate
   - GET /api/currencies/exchange-rate?fromCurrency=USD&toCurrency=EUR
   - Headers: X-Tenant-ID
   - Expected: 200 OK

8. Delete Currency
   - DELETE /api/currencies/{code}
   - Headers: X-Tenant-ID
   - Expected: 204 No Content

### Edge Cases
- Duplicate codes
- Invalid codes (not 3 chars)
- Negative/zero rates
- Non-existent conversions
- Multiple defaults

## 11. Webhook Testing

### Stripe Webhook
1. Process Stripe Webhook
   - POST /api/webhooks/stripe
   - Headers: Stripe-Signature, Content-Type
   - Body: Stripe event JSON
   - Expected: 200 OK

### Edge Cases
- Invalid signatures
- Malformed JSON
- Unknown event types
- Missing headers

## 12. Integration Test Scenarios

### Full Purchase Cycle
1. Create Category
2. Create Product with Digital Details
3. Create User
4. Create Order
5. Create Payment
6. Confirm Payment
7. Complete Order
8. Record Download
9. Create Review

### Bundle Purchase
1. Create Products
2. Create Bundle
3. Calculate Price
4. Create Order
5. Process Payment

### Refund Processing
1. Complete Purchase
2. Partial Refund
3. Full Refund
4. Refund Order

### Multi-Currency
1. Create Currencies
2. Create Products
3. Convert Amounts
4. Process Orders

## 13. Error Handling Tests

### Authentication Errors
- 401 Unauthorized
- 403 Forbidden

### Validation Errors
- 400 Bad Request
- 422 Unprocessable Entity

### Resource Errors
- 404 Not Found
- 409 Conflict

### Server Errors
- 500 Internal Server Error

## 14. Security Test Cases

### Authorization Testing
- Cross-tenant access
- User data modification
- Admin endpoint access

### Input Validation
- SQL injection
- XSS attempts
- Oversized payloads

### Data Privacy
- PII exposure
- Sensitive data logs
- Encryption validation

## Testing Tips

1. Always start with authentication
2. Follow logical business flows
3. Test edge cases thoroughly
4. Document unexpected behaviors
5. Use collection variables for IDs
6. Implement proper cleanup
7. Test both success and failure paths
8. Validate response structures
9. Check HTTP status codes
10. Monitor performance during tests

This guide provides comprehensive coverage for testing all Digital E-Store API endpoints in realistic scenarios. 