# 🔴 **CRITICAL SECURITY & LOGICAL ISSUES FOUND**

## **Summary**
A comprehensive security audit of the codebase revealed **multiple IDOR (Insecure Direct Object Reference) vulnerabilities** and **missing data sorting** issues that need immediate attention.

---

## **🚨 CRITICAL ISSUES (IDOR Vulnerabilities)**

### **1. UserController - Users Can List All Users**
**File:** `UserController.java:31-42`

**Issue:** The `getAllUsers()` endpoint allows any authenticated user to list all users in the tenant.

```java
@GetMapping("/api/v1/tenants/{tenantId}/users")
@PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
public ResponseEntity<List<UserResponse>> getAllUsers(
        @PathVariable Integer tenantId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String username,
        @RequestParam(required = false) String email) {

    return ResponseEntity.ok(userService.getAllUsers(tenantId, status, username, email));
}
```

**Problem:** Regular users can filter by status/username/email and see all users.

**Impact:** Users can discover other users' information (usernames, emails, status).

**Fix Required:**
- Regular users (ROLE_USER) should only see their own profile
- Admins/Tenants can see all users
- Remove username/email filter parameters from public API
- Add tenant authorization check

---

### **2. PaymentController - Users Can See All Payments**
**File:** `PaymentController.java:36-45`

**Issue:** The `getAllPayments()` endpoint allows users to view all payments.

```java
@GetMapping
@PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
public ResponseEntity<List<PaymentResponse>> getAllPayments(
        @PathVariable Integer tenantId,
        @RequestParam(required = false) String orderId,
        @RequestParam(required = false) String status) {

    return ResponseEntity.ok(paymentService.getAllPayments(tenantId, orderId, status));
}
```

**Problem:** Any user can filter by orderId and see other users' payment details.

**Impact:** Users can access other users' payment information including amounts, transaction IDs.

**Fix Required:**
- Filter by authenticated user's userId for ROLE_USER
- Only admins/tenants can see all payments
- Add sorting by paymentDate DESC

---

### **3. DownloadController - Users Can View Others' Download History**
**File:** `DownloadController.java:61-70`

**Issue:** The `getUserDownloadHistory()` endpoint accepts userId as path variable.

```java
@GetMapping("/tenants/{tenantId}/users/{userId}/download-history")
@PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
public ResponseEntity<List<DownloadHistoryResponse>> getUserDownloadHistory(
        @PathVariable Integer tenantId,
        @PathVariable String userId) {

    List<DownloadHistoryResponse> history = downloadService.getUserDownloadHistory(tenantId, userId);
    return ResponseEntity.ok(history);
}
```

**Problem:** Users can change the userId in URL and view other users' download history.

**Impact:** Privacy breach - users can see what others have purchased and downloaded.

**Fix Required:**
- For ROLE_USER: Force userId to match authenticated user
- For ROLE_ADMIN/ROLE_TENANT: Allow access to any userId
- Add tenant authorization check

---

### **4. OrderController.getUser() - Already Fixed**
**Status:** ✅ **FIXED** - Now filters by authenticated user

---

## **📊 MISSING SORTING ISSUES**

### **5. PaymentRepository - No Date Sorting**
**File:** `PaymentRepository.java`

**Issue:** Queries don't have ORDER BY, so payments are not returned in chronological order.

**Queries to Fix:**
```java
// Line 15 - Add ORDER BY
List<Payment> findByTenantIdAndOrderId(Integer tenantId, String orderId);
// Should be: ORDER BY paymentDate DESC

// Line 17 - Add ORDER BY
List<Payment> findByTenantId(Integer tenantId);
// Should be: ORDER BY paymentDate DESC

// Line 19 - Add ORDER BY
List<Payment> findByTenantIdAndStatus(Integer tenantId, String status);
// Should be: ORDER BY paymentDate DESC
```

**Impact:** Payment history and analytics show data in random order.

**Fix Required:** Add `@Query` annotations with `ORDER BY p.paymentDate DESC`

---

### **6. UserRepository - No Date Sorting**
**File:** `UserRepository.java`

**Issue:** User lists are not sorted by creation date.

**Queries to Fix:**
```java
// Line 15 - Add ORDER BY
List<User> findByTenantId(Integer tenantId);
// Should be: ORDER BY created DESC

// Line 17 - Add ORDER BY
List<User> findByTenantIdAndStatus(Integer tenantId, String status);
// Should be: ORDER BY created DESC
```

**Impact:** User lists are not in logical order.

**Fix Required:** Add `@Query` annotations with `ORDER BY u.created DESC`

---

### **7. DigitalDownloadRepository - No Date Sorting**
**File:** `DigitalDownloadRepository.java`

**Issue:** Download history is not sorted by download date.

**Queries to Fix:**
- `findByTenantIdAndOrderItemId()` - Line 16
- `findByTenantId()` - Line 19
- `findByIpAddress()` - Line 22
- `findByDownloadDateBetween()` - Line 25
- `findByTenantIdAndStatus()` - Line 28
- `findByTenantIdAndUserId()` - Line 31-43

**Impact:** Download history shows older entries first instead of most recent.

**Fix Required:** Add `ORDER BY dd.downloadDate DESC` to all queries

---

## **🔧 RECOMMENDED FIXES**

### **Fix Pattern for All Controllers:**

```java
@GetMapping
@PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
public ResponseEntity<List<SomeResponse>> getAll(
        @PathVariable Integer tenantId,
        Authentication authentication) {

    // 1. Validate tenant access
    tenantSecurity.validateTenantAccess(authentication, tenantId);

    // 2. Check user role
    boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    boolean isTenant = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_TENANT"));

    String username = authentication.getName();

    // 3. Filter by user if not admin/tenant
    return ResponseEntity.ok(service.getAll(
        tenantId, username, isAdmin || isTenant
    ));
}
```

### **Fix Pattern for All Repositories:**

```java
@Query("SELECT e FROM Entity e WHERE e.tenantId = :tenantId ORDER BY e.created DESC")
List<Entity> findByTenantId(@Param("tenantId") Integer tenantId);
```

---

## **✅ ALREADY FIXED (Good Work!)**

1. ✅ **OrderController.getAllOrders()** - Now filters by authenticated user
2. ✅ **OrderRepository** - Has `ORDER BY o.orderDate DESC`
3. ✅ **Discount race condition** - Fixed
4. ✅ **Order creation race condition** - Fixed
5. ✅ **Order status transitions** - Validated
6. ✅ **TenantSecurity component** - Created and integrated

---

## **🎯 PRIORITY ORDER**

### **🔴 HIGH PRIORITY (Security Issues)**
1. Fix UserController.getAllUsers() - IDOR vulnerability
2. Fix PaymentController.getAllPayments() - IDOR vulnerability
3. Fix DownloadController.getUserDownloadHistory() - IDOR vulnerability

### **🟡 MEDIUM PRIORITY (User Experience)**
4. Add sorting to PaymentRepository
5. Add sorting to UserRepository
6. Add sorting to DigitalDownloadRepository

---

## **📝 CHECKLIST FOR OTHER CONTROLLERS**

Review these controllers for similar issues:
- [ ] ReviewController - Does it allow viewing others' reviews?
- [ ] ProductController - Any user-specific data exposed?
- [ ] CategoryController - Any user-specific data exposed?
- [ ] BundleController - Any user-specific data exposed?
- [ ] DiscountController - Any user-specific data exposed?
- [ ] TaxController - Any user-specific data exposed?

---

## **🛡️ SECURITY TESTING CHECKLIST**

After fixing, test these scenarios:

1. **Regular User** can only see their own:
   - [ ] Orders
   - [ ] Payments
   - [ ] Downloads
   - [ ] User profile

2. **Admin** can see all tenant data:
   - [ ] All users
   - [ ] All orders
   - [ ] All payments
   - [ ] All downloads

3. **Users cannot access**:
   - [ ] Other tenants' data (cross-tenant access)
   - [ ] Other users' data within same tenant

---

**Generated:** 2026-01-20
**Status:** Needs immediate attention for security issues
