# User API Reference

Base URL: `http://localhost:8080`

---

## Authentication

### 1. Customer Signup
```
POST /api/v1/tenants/{tenantId}/auth/signup
```
```json
{
  "email": "sarah.jones@email.com",
  "password": "Customer@123",
  "firstName": "Sarah",
  "lastName": "Jones"
}
```

### 2. Customer Login
```
POST /api/v1/tenants/{tenantId}/auth/login
```
```json
{
  "email": "sarah.jones@email.com",
  "password": "Customer@123"
}
```

### 3. Get My Profile
```
GET /api/v1/tenants/{tenantId}/auth/me
Authorization: Bearer {token}
```

---

## Products (Public)

### 5. Get All Products
```
GET /api/v1/public/tenants/{tenantId}/products?page=0&size=10
```

### 6. Get Product Details
```
GET /api/v1/public/tenants/{tenantId}/products/{productId}
```

### 7. Get Categories
```
GET /api/v1/public/tenants/{tenantId}/categories
```

---

## Orders

### 8. Create Order
```
POST /api/v1/tenants/{tenantId}/orders
Authorization: Bearer {token}
```
```json
{
  "userId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 1,
      "price": 399.99,
      "productName": "ProCRM Enterprise"
    }
  ],
  "discountCode": "LAUNCH2025",
  "notes": "Please include installation guide",
  "billingAddress": {
    "firstName": "Sarah",
    "lastName": "Jones",
    "email": "customer@email.com",
    "phone": "+1-555-0201",
    "street": "123 Market Street",
    "city": "San Francisco",
    "state": "CA",
    "country": "United States",
    "zipCode": "94105"
  },
  "shippingAddress": {
    "firstName": "Sarah",
    "lastName": "Jones",
    "street": "123 Market Street",
    "city": "San Francisco",
    "state": "CA",
    "country": "United States",
    "zipCode": "94105"
  }
}
```

### 9. Get Order Details
```
GET /api/v1/tenants/{tenantId}/orders/{orderId}
Authorization: Bearer {token}
```

### 10. Get User Orders
```
GET /api/v1/tenants/{tenantId}/orders/user/{userId}?page=0&size=10
Authorization: Bearer {token}
```

---

## Payments

### 11. Create Payment
```
POST /api/v1/tenants/{tenantId}/payments
Authorization: Bearer {token}
```
```json
{
  "orderId": 1,
  "amount": 530.32,
  "currency": "USD",
  "paymentMethod": "STRIPE",
  "paymentMethodDetails": {
    "cardLast4": "4242",
    "cardBrand": "Visa",
    "cardExpMonth": "12",
    "cardExpYear": "2027"
  },
  "stripeToken": "tok_visa",
  "description": "Payment for order ORD-2025-0001"
}
```

---

## Downloads

### 12. Record Download
```
POST /api/v1/tenants/{tenantId}/downloads
Authorization: Bearer {token}
```
```json
{
  "userId": 1,
  "productId": 1,
  "orderId": 1,
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0"
}
```

### 13. Get Download Link
```
GET /api/v1/tenants/{tenantId}/downloads/product/{productId}/user/{userId}
Authorization: Bearer {token}
```

### 14. Get User Download History
```
GET /api/v1/tenants/{tenantId}/downloads/user/{userId}
Authorization: Bearer {token}
```

---

## Reviews

### 15. Create Review
```
POST /api/v1/tenants/{tenantId}/reviews
Authorization: Bearer {token}
```
```json
{
  "productId": 1,
  "userId": 1,
  "rating": 5,
  "title": "Best CRM Software!",
  "comment": "ProCRM has transformed our business operations.",
  "isVerifiedPurchase": true,
  "pros": ["Easy to use", "Great features"],
  "cons": ["Learning curve"]
}
```

### 16. Get User Reviews
```
GET /api/v1/tenants/{tenantId}/reviews/user/{userId}
Authorization: Bearer {token}
```

### 17. Update Review
```
PUT /api/v1/tenants/{tenantId}/reviews/{reviewId}
Authorization: Bearer {token}
```
```json
{
  "rating": 5,
  "comment": "Updated review after 3 months of use"
}
```

---

## Reviews (Public)

### 18. Get Product Reviews
```
GET /api/v1/public/tenants/{tenantId}/products/{productId}/reviews
```

### 19. Get Average Rating
```
GET /api/v1/public/tenants/{tenantId}/products/{productId}/reviews/average
```

### 20. Get Verified Reviews
```
GET /api/v1/public/tenants/{tenantId}/reviews/verified
```

---

**Total User Endpoints: 20**
