# Digital E-Store Postman Collections

## Overview

This folder contains three Postman collections for testing the Digital E-Store API:

1. **1-Platform-Admin.postman_collection.json** - Platform-level admin operations
2. **2-Tenant-Admin.postman_collection.json** - Tenant/Store admin operations
3. **3-User-Customer.postman_collection.json** - Customer operations

## Quick Start

### 1. Import Collections
1. Open Postman
2. Click "Import"
3. Select all three `.json` files and import them

### 2. Set Up Environment
1. Click on the environment dropdown (top right corner)
2. Select "Manage Environments"
3. Create a new environment named `Digital E-Store Dev`
4. Add the variable:
   ```
   base_url: http://localhost:8080
   ```

### 3. Select the Environment
Make sure `Digital E-Store Dev` is selected (top right dropdown)

## Collection Details

### 1-Platform-Admin Collection

For platform administrators to manage tenants across the system.

**Key Endpoints:**
- Platform Admin Login
- Forgot Password
- Get Me (current user info)
- Tenant Management (CRUD operations)

**Note:** All auth endpoints use `application/x-www-form-urlencoded` (form data), NOT JSON.

### 2-Tenant-Admin Collection

For tenant administrators to manage their store.

**Key Endpoints:**
- Tenant Auth (signup, login, forgot password)
- User Management (CRUD users)
- Category Management (CRUD categories)
- Product Management (CRUD products)
- Order Management (CRUD orders)
- Discount Management (CRUD discount codes)
- Tax Management (CRUD taxes)
- Review Management (CRUD reviews)
- Bundle Management (CRUD product bundles)
- Currency Management (CRUD currencies, convert, exchange rates)
- Page Management (CRUD CMS pages)
- Theme Management (CRUD store themes)
- Payment Management (create, confirm, cancel, refund)
- Download Management (digital product details, download tracking)

### 3-User-Customer Collection

For customers to browse products, place orders, and manage their account.

**Key Endpoints:**
- User Auth (signup, login, forgot password, logout)
- Public Products (browse, search, filter by category)
- Public Categories (browse categories)
- Public Pages (browse CMS pages)
- Public Reviews (view reviews, ratings)
- My Orders (view order history)
- My Reviews (create, update, delete reviews)
- My Downloads (view download history)

**Note:** User auth endpoints use `application/x-www-form-urlencoded` (form data).

## Testing Flow

### Recommended Testing Order

#### Step 1: Platform Setup
1. **Login as Platform Admin**
   - Collection: `1-Platform-Admin`
   - Request: `Login`
   - Expected: Sets `access_token` environment variable

2. **Create a New Tenant**
   - Collection: `1-Platform-Admin`
   - Request: `Create Tenant`
   - Expected: Sets `tenant_id` environment variable
   - Sample: Full tenant setup with shop details, admin credentials

3. **Switch to Tenant Admin Collection**
   - Now switch to `2-Tenant-Admin` collection

4. **Login as Tenant Admin**
   - Collection: `2-Tenant-Admin`
   - Request: `Login`
   - Expected: Sets `access_token` environment variable
   - Sample: Uses tenant_id from environment

#### Step 2: Store Setup

5. **Create Categories**
   - Request: `Create Category`
   - Sample: "Electronics" with description
   - Expected: Returns category ID, sets `category_id`

6. **Create Products**
   - Request: `Create Product`
   - Sample: iPhone with full details, images, pricing
   - Expected: Returns product ID, sets `product_id`

7. **Create Bundles**
   - Request: `Create Bundle`
   - Sample: "Photographer Starter Kit" with multiple products
   - Expected: Returns bundle ID, sets `bundle_id`

8. **Create Discounts**
   - Request: `Create Discount`
   - Sample: "SUMMER2024" - 20% off
   - Expected: Returns discount ID, sets `discount_id`

9. **Create Currencies**
   - Request: `Create Currency`
   - Sample: "EUR" with exchange rate
   - Expected: Returns currency info

10. **Create Pages**
   - Request: `Create Page`
   - Sample: About Us page
   - Expected: Returns page ID

#### Step 3: Customer Testing

11. **Switch to User/Customer Collection**
   - Now switch to `3-User-Customer` collection

12. **User Signup**
   - Request: `Signup`
   - Sample: Full user details
   - Expected: Returns access_token and sets `user_id`

13. **Browse Products (Public)**
   - Request: `Browse All Products`
   - Query params: `?page=0&size=20`
   - Expected: Returns paginated product list

14. **Get Product Details**
   - Use `product_id` from previous response
   - Expected: Returns full product details

15. **Create Order**
   - Request: `Create Order`
   - Sample: Order with user, currency, totalAmount, orderItems, discountCode
   - Expected: Returns order ID, sets `order_id`

16. **Create Payment**
   - Request: `Create Payment`
   - Sample: Payment with order, amount, paymentMethod, currency
   - Expected: Returns payment ID, sets `payment_id`

17. **Confirm Payment**
   - Request: `Confirm Payment`
   - Query param: `?transactionId=txn_123456789`
   - Important: NO request body, transaction ID in URL

18. **View Order Details**
   - Request: `Get Order by ID`
   - Expected: Returns order with status

19. **Record Download**
   - Request: `Record Download`
   - Expected: Records the download event

20. **Create Review**
   - Request: `Create Review`
   - Sample: Product rating and comment
   - Expected: Returns review ID

## Important Notes

### Content Types

| Collection | Content Type |
|-------------|---------------|
| Platform Admin Auth | `application/x-www-form-urlencoded` |
| Tenant Admin Auth | `application/x-www-form-urlencoded` |
| User/Customer Auth | `application/x-www-form-urlencoded` |
| All other endpoints | `application/json` |

### Field Name Corrections

The following field names were corrected to match API DTOs:

| Endpoint | Old (Incorrect) | New (Correct) |
|-----------|------------------|----------------|
| Product | `price` | `defaultPrice` |
| Product | `imageUrl` | `image1Url` |
| Order | `items` | `orderItems` |
| Order | `shippingAddress` | (removed - not in DTO) |
| Order | `paymentMethod` | (removed - not in DTO) |
| Discount | `status` | (removed - not in DTO) |
| Tax | `taxName` | `code` |
| Tax | `taxRate` | `value` |
| Tax | `isDefault` | `defaultFlag` |
| Currency | `code` | `currencyCode` |
| Currency | `name` | `currencyName` |
| Currency Convert | `from` | `fromCurrency` |
| Currency Convert | `to` | `toCurrency` |
| Review | `userId` | (removed - not in DTO) |
| Review | `title` | (removed - not in DTO) |
| Bundle | `name` | `bundleName` |
| Bundle | `price` | `bundlePrice` |
| Bundle Calculate | `{ productIds, discountPercent }` | Array of `{ productId, quantity }` |
| Digital Product | `downloadUrl` | `fileUrl` |
| Payment Confirm | Body `{ transactionId }` | Query param `?transactionId=` |

### Payment Confirm

**IMPORTANT:** The Payment Confirm endpoint expects `transactionId` as a QUERY PARAMETER, not in the request body.

```
❌ WRONG:
POST /api/v1/tenants/1/payments/PAY-001/confirm
Body: { "transactionId": "txn_123456" }

✅ CORRECT:
POST /api/v1/tenants/1/payments/PAY-001/confirm?transactionId=txn_123456
No body needed
```

### Environment Variables

All collections use these environment variables:

| Variable | Description |
|----------|-------------|
| `base_url` | API base URL (http://localhost:8080) |
| `access_token` | JWT token (auto-set after login) |
| `tenant_id` | Tenant ID (auto-set after tenant creation) |
| `user_id` | User ID (auto-set after user creation) |
| `category_id` | Category ID (copy from create response) |
| `product_id` | Product ID (copy from create response) |
| `order_id` | Order ID (copy from create response) |
| `payment_id` | Payment ID (copy from create response) |
| `discount_id` | Discount ID (copy from create response) |
| `tax_id` | Tax ID (copy from create response) |
| `review_id` | Review ID (copy from create response) |
| `bundle_id` | Bundle ID (copy from create response) |

**Tip:** After each successful "Create" or "Get" request, copy the returned ID from the response and use it in subsequent requests.

## Troubleshooting

### 401 Unauthorized
- Check that `access_token` environment variable is set
- Verify the token is valid (not expired)

### 400 Bad Request
- Check Content-Type matches expected format
- Check field names exactly match DTO
- Check all required fields are present

### 404 Not Found
- Verify the resource ID exists
- Verify `tenant_id` is correct

### 422 Unprocessable Entity
- Check validation rules (email format, password length, etc.)
- Review error message in response

### Payment Confirm Issues
- Ensure `transactionId` is in URL query params, NOT in request body
- Verify payment exists before confirming

## Getting Help

For detailed API documentation, see:
- **API-Testing-Guide.md** - Complete endpoint reference and testing checklist
- Swagger/OpenAPI documentation (when backend is running): `http://localhost:8080/swagger-ui.html`

## Sample Data Summary

All requests in the collections now include comprehensive sample data:
- **Platform Admin:** Complete tenant creation with shop details, admin credentials
- **Tenant Admin:** Full product with images, pricing, metadata; Category with description; Discount with dates and limits; Tax with rates; Bundle with multiple products
- **User/Customer:** Full user signup; Order with items and pricing; Review with detailed comment

Simply import the collections, set the environment, and start testing!
