## 2. Database Migrations Configuration

### Configuration Files
- `src/main/resources/application.properties`
  - Add: spring.flyway.enabled=true, spring.flyway.locations=classpath:db/migration
- `src/main/resources/application-dev.properties`
  - Dev-specific Flyway settings
- `src/main/resources/application-prod.properties`
  - Prod-specific Flyway settings

### Sample Data
- `src/main/resources/db/migration/V2__sample_data.sql` (UPDATE)
  - Add sample: Users, Categories, Products, DigitalProductDetails, Orders, OrderItems

### Dependencies
- `pom.xml` (UPDATE)
  - Add Flyway Maven plugin dependency

---

## 3. Multi-currency Support

### Entities
- `src/main/java/com/inventrik/digitalestore/domain/currency/Currency.java`
  - Fields: tenantId, currencyCode, currencyName, symbol, exchangeRate, isDefault, status
- `src/main/java/com/inventrik/digitalestore/domain/product/ProductPrice.java`
  - Fields: tenantId, productId, currencyCode, price, status

### Repositories
- `src/main/java/com/inventrik/digitalestore/repository/CurrencyRepository.java`
  - Methods: findByTenantId, findByTenantIdAndCurrencyCode, findDefaultCurrency
- `src/main/java/com/inventrik/digitalestore/repository/ProductPriceRepository.java`
  - Methods: findByTenantIdAndProductId, findByTenantIdAndProductIdAndCurrencyCode

### Services
- `src/main/java/com/inventrik/digitalestore/service/currency/CurrencyService.java`
  - Methods: getAllCurrencies, convertAmount, getExchangeRate, updateExchangeRates
- `src/main/java/com/inventrik/digitalestore/service/currency/CurrencyServiceImpl.java`
  - Currency conversion logic, exchange rate management

### Controllers
- `src/main/java/com/inventrik/digitalestore/api/CurrencyController.java`
  - Endpoints: /currencies, /currencies/{code}, /currencies/convert

### DTOs
- `src/main/java/com/inventrik/digitalestore/dto/request/CurrencyRequest.java`
  - Fields: currencyCode, currencyName, symbol, exchangeRate, isDefault
- `src/main/java/com/inventrik/digitalestore/dto/response/CurrencyResponse.java`
  - Fields: currencyCode, currencyName, symbol, exchangeRate, isDefault, status

### Database Migration
- `src/main/resources/db/migration/V4__multi_currency.sql`
  - CREATE TABLE Currencies, CREATE TABLE ProductPrices

---

## 4. Reviews System

### Entities
- `src/main/java/com/inventrik/digitalestore/domain/review/Review.java`
  - Fields: reviewId, tenantId, productId, userId, rating, comment, status, verified

### Repositories
- `src/main/java/com/inventrik/digitalestore/repository/ReviewRepository.java`
  - Methods: findByTenantIdAndProductId, findByTenantIdAndUserId, findByRatingGreaterThan

### Services
- `src/main/java/com/inventrik/digitalestore/service/review/ReviewService.java`
  - Methods: createReview, getProductReviews, getUserReviews, getAverageRating
- `src/main/java/com/inventrik/digitalestore/service/review/ReviewServiceImpl.java`
  - Review validation, rating calculations, review moderation

### Controllers
- `src/main/java/com/inventrik/digitalestore/api/ReviewController.java`
  - Endpoints: /reviews, /reviews/product/{productId}, /reviews/user/{userId}

### DTOs
- `src/main/java/com/inventrik/digitalestore/dto/request/ReviewRequest.java`
  - Fields: productId, rating, comment
- `src/main/java/com/inventrik/digitalestore/dto/response/ReviewResponse.java`
  - Fields: reviewId, productId, userId, rating, comment, reviewDate, verified

### Database Migration
- `src/main/resources/db/migration/V5__reviews_system.sql`
  - CREATE TABLE Reviews

---

## 5. Multi-language Support

### Entities
- `src/main/java/com/inventrik/digitalestore/domain/category/CategoryTranslation.java`
  - Fields: tenantId, categoryId, languageCode, categoryName, description, status
- `src/main/java/com/inventrik/digitalestore/domain/product/ProductTranslation.java`
  - Fields: tenantId, productId, languageCode, productName, description, status

### Repositories
- `src/main/java/com/inventrik/digitalestore/repository/CategoryTranslationRepository.java`
  - Methods: findByTenantIdAndCategoryIdAndLanguageCode, findByTenantIdAndLanguageCode
- `src/main/java/com/inventrik/digitalestore/repository/ProductTranslationRepository.java`
  - Methods: findByTenantIdAndProductIdAndLanguageCode, findByTenantIdAndLanguageCode

### Services
- `src/main/java/com/inventrik/digitalestore/service/translation/TranslationService.java`
  - Methods: getTranslatedCategory, getTranslatedProduct, saveTranslation
- `src/main/java/com/inventrik/digitalestore/service/translation/TranslationServiceImpl.java`
  - Translation retrieval, fallback logic

### Updated Services
- Update existing ProductService, CategoryService to support language parameter

### DTOs
- `src/main/java/com/inventrik/digitalestore/dto/request/TranslationRequest.java`
  - Fields: languageCode, name, description
- Update existing response DTOs to include translations

### Database Migration
- `src/main/resources/db/migration/V6__translations.sql`
  - CREATE TABLE CategoryTranslations, CREATE TABLE ProductTranslations

---

## 6. Discount Codes

### Entities
- `src/main/java/com/inventrik/digitalestore/domain/discount/DiscountCode.java`
  - Fields: discountId, tenantId, code, discountType, discountValue, minOrderAmount, maxUses, usedCount, validFrom, validTo, status
- `src/main/java/com/inventrik/digitalestore/domain/discount/DiscountUsage.java`
  - Fields: usageId, tenantId, discountId, orderId, userId, usedDate

### Repositories
- `src/main/java/com/inventrik/digitalestore/repository/DiscountCodeRepository.java`
  - Methods: findByTenantIdAndCode, findActiveCodes, findByTenantId
- `src/main/java/com/inventrik/digitalestore/repository/DiscountUsageRepository.java`
  - Methods: findByDiscountId, countByDiscountIdAndUserId

### Services
- `src/main/java/com/inventrik/digitalestore/service/discount/DiscountService.java`
  - Methods: validateDiscount, applyDiscount, calculateDiscountAmount, recordUsage
- `src/main/java/com/inventrik/digitalestore/service/discount/DiscountServiceImpl.java`
  - Validation logic, usage tracking, discount calculations

### Controllers
- `src/main/java/com/inventrik/digitalestore/api/DiscountController.java`
  - Endpoints: /discounts, /discounts/validate/{code}, /discounts/{discountId}

### DTOs
- `src/main/java/com/inventrik/digitalestore/dto/request/DiscountRequest.java`
  - Fields: code, discountType, discountValue, minOrderAmount, maxUses, validFrom, validTo
- `src/main/java/com/inventrik/digitalestore/dto/response/DiscountResponse.java`
  - Fields: discountId, code, discountType, discountValue, validFrom, validTo, status

### Updated Services
- Update OrderService to apply discounts during order creation

### Database Migration
- `src/main/resources/db/migration/V7__discount_codes.sql`
  - CREATE TABLE DiscountCodes, CREATE TABLE DiscountUsage

---

## 7. Product Bundles

### Entities
- `src/main/java/com/inventrik/digitalestore/domain/bundle/ProductBundle.java`
  - Fields: bundleId, tenantId, bundleName, description, bundlePrice, discountPercent, status
- `src/main/java/com/inventrik/digitalestore/domain/bundle/BundleItem.java`
  - Fields: bundleItemId, tenantId, bundleId, productId, quantity, status

### Repositories
- `src/main/java/com/inventrik/digitalestore/repository/ProductBundleRepository.java`
  - Methods: findByTenantId, findByTenantIdAndBundleId, findActiveBundles
- `src/main/java/com/inventrik/digitalestore/repository/BundleItemRepository.java`
  - Methods: findByBundleId, findByProductId

### Services
- `src/main/java/com/inventrik/digitalestore/service/bundle/BundleService.java`
  - Methods: createBundle, getBundleDetails, calculateBundlePrice, addProductToBundle
- `src/main/java/com/inventrik/digitalestore/service/bundle/BundleServiceImpl.java`
  - Bundle management, pricing calculations

### Controllers
- `src/main/java/com/inventrik/digitalestore/api/BundleController.java`
  - Endpoints: /bundles, /bundles/{bundleId}, /bundles/{bundleId}/items

### DTOs
- `src/main/java/com/inventrik/digitalestore/dto/request/BundleRequest.java`
  - Fields: bundleName, description, bundlePrice, discountPercent, productIds
- `src/main/java/com/inventrik/digitalestore/dto/response/BundleResponse.java`
  - Fields: bundleId, bundleName, description, bundlePrice, discountPercent, bundleItems

### Updated Services
- Update OrderService to handle bundle orders

### Database Migration
- `src/main/resources/db/migration/V8__product_bundles.sql`
  - CREATE TABLE ProductBundles, CREATE TABLE BundleItems