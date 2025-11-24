-- Initial Multi-Tenant Digital E-Store Schema
-- Created: 2025-01-04
-- This creates the complete multi-tenant schema with all modern features

-- ================================================
-- TENANTS TABLE (Main tenant management)
-- ================================================
CREATE TABLE tenants (
    tenant_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    shop_name VARCHAR(100) NOT NULL COMMENT 'Store/Shop display name',
    shop_email VARCHAR(100) NOT NULL UNIQUE COMMENT 'Primary contact email',
    domain_name VARCHAR(50) NOT NULL UNIQUE COMMENT 'Subdomain for tenant',
    store_password VARCHAR(255) NOT NULL COMMENT 'Hashed password for tenant login',
    business_type ENUM('INDIVIDUAL', 'COMPANY') NOT NULL DEFAULT 'INDIVIDUAL',
    business_registration_number VARCHAR(50) NULL,
    business_address VARCHAR(255) NULL,
    tax_identification_number VARCHAR(50) NULL,
    contact_person_name VARCHAR(100) NULL,
    contact_phone VARCHAR(20) NULL,
    billing_address TEXT NULL,
    shipping_address TEXT NULL,
    subscription_plan ENUM('FREE', 'BASIC', 'PREMIUM', 'ENTERPRISE') NOT NULL DEFAULT 'FREE',
    subscription_status ENUM('ACTIVE', 'SUSPENDED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    subscription_start_date DATE NULL,
    subscription_end_date DATE NULL,
    storage_limit_gb INT UNSIGNED DEFAULT 1 COMMENT 'Storage limit in GB',
    monthly_transaction_limit INT UNSIGNED DEFAULT 100 COMMENT 'Monthly transaction limit',
    status VARCHAR(2) NOT NULL DEFAULT 'A' COMMENT 'A=Active, I=Inactive',
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_tenants_domain (domain_name),
    INDEX idx_tenants_email (shop_email),
    INDEX idx_tenants_status (status)
) COMMENT='Multi-tenant store management';

-- ================================================
-- USERS TABLE (Multi-tenant users)
-- ================================================
CREATE TABLE users (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID',
    user_id BIGINT NOT NULL COMMENT 'User ID within tenant',
    username VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NULL,
    last_name VARCHAR(50) NULL,
    image VARCHAR(256) NULL,
    phone VARCHAR(100) NULL,
    email VARCHAR(100) NOT NULL,
    user_type ENUM('INDIVIDUAL', 'COMPANY') NOT NULL DEFAULT 'INDIVIDUAL',
    user_role ENUM('USER', 'TENANT_ADMIN', 'SYSTEM_ADMIN') NOT NULL DEFAULT 'USER',
    company_name VARCHAR(100) NULL,
    company_registration_number VARCHAR(50) NULL,
    company_address1 VARCHAR(255) NULL,
    company_address2 VARCHAR(255) NULL,
    company_country VARCHAR(255) NULL,
    company_pincode VARCHAR(255) NULL,
    tax_id VARCHAR(50) NULL,
    otp VARCHAR(8) NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(2) NOT NULL DEFAULT 'A' COMMENT 'A=Active, I=Inactive',
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, user_id),
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    UNIQUE KEY uk_users_tenant_username (tenant_id, username),
    UNIQUE KEY uk_users_tenant_email (tenant_id, email),
    INDEX idx_users_role (tenant_id, user_role),
    INDEX idx_users_status (tenant_id, status)
) COMMENT='Multi-tenant user accounts';

-- ================================================
-- CURRENCIES TABLE (Multi-tenant currencies)
-- ================================================
CREATE TABLE currencies (
    tenant_id INT UNSIGNED NOT NULL,
    currency_code VARCHAR(3) NOT NULL COMMENT 'ISO 4217 currency code',
    currency_name VARCHAR(50) NOT NULL,
    currency_symbol VARCHAR(10) NOT NULL,
    exchange_rate DECIMAL(10,4) NOT NULL DEFAULT 1.0000,
    is_base_currency BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, currency_code),
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    INDEX idx_currencies_active (tenant_id, is_active)
) COMMENT='Multi-tenant currency management';

-- ================================================
-- CATEGORIES TABLE (Multi-tenant product categories)
-- ================================================
CREATE TABLE categories (
    tenant_id INT UNSIGNED NOT NULL,
    category_id BIGINT NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    parent_category_id BIGINT NULL,
    image_url VARCHAR(500) NULL,
    sort_order INT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    meta_title VARCHAR(150) NULL,
    meta_description VARCHAR(300) NULL,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, category_id),
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id, parent_category_id) REFERENCES categories(tenant_id, category_id),
    INDEX idx_categories_parent (tenant_id, parent_category_id),
    INDEX idx_categories_active (tenant_id, is_active)
) COMMENT='Multi-tenant product categories';

-- ================================================
-- PRODUCTS TABLE (Multi-tenant products)
-- ================================================
CREATE TABLE products (
    tenant_id INT UNSIGNED NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    description TEXT NULL,
    short_description VARCHAR(500) NULL,
    sku VARCHAR(50) NULL,
    barcode VARCHAR(50) NULL,
    price DECIMAL(10,2) NOT NULL,
    cost_price DECIMAL(10,2) NULL,
    compare_at_price DECIMAL(10,2) NULL,
    weight DECIMAL(8,2) NULL,
    dimensions VARCHAR(50) NULL,
    inventory_quantity INT NOT NULL DEFAULT 0,
    track_inventory BOOLEAN NOT NULL DEFAULT TRUE,
    allow_backorders BOOLEAN NOT NULL DEFAULT FALSE,
    is_digital BOOLEAN NOT NULL DEFAULT FALSE,
    requires_shipping BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    images JSON NULL COMMENT 'Array of image URLs',
    metadata JSON NULL COMMENT 'Additional product metadata',
    seo_title VARCHAR(150) NULL,
    seo_description VARCHAR(300) NULL,
    seo_keywords VARCHAR(200) NULL,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, product_id),
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    UNIQUE KEY uk_products_tenant_sku (tenant_id, sku),
    INDEX idx_products_active (tenant_id, is_active),
    INDEX idx_products_featured (tenant_id, featured),
    INDEX idx_products_digital (tenant_id, is_digital),
    FULLTEXT idx_products_search (product_name, description)
) COMMENT='Multi-tenant product catalog';

-- ================================================
-- PRODUCT PRICES (Multi-currency support)
-- ================================================
CREATE TABLE product_prices (
    tenant_id INT UNSIGNED NOT NULL,
    product_id BIGINT NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    compare_at_price DECIMAL(10,2) NULL,
    cost_price DECIMAL(10,2) NULL,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, product_id, currency_code),
    FOREIGN KEY (tenant_id, product_id) REFERENCES products(tenant_id, product_id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id, currency_code) REFERENCES currencies(tenant_id, currency_code) ON DELETE CASCADE
) COMMENT='Multi-currency product pricing';

-- ================================================
-- ORDERS TABLE (Multi-tenant orders)
-- ================================================
CREATE TABLE orders (
    tenant_id INT UNSIGNED NOT NULL,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    currency_code VARCHAR(3) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    shipping_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    billing_address JSON NOT NULL,
    shipping_address JSON NULL,
    notes TEXT NULL,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, order_id),
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id, user_id) REFERENCES users(tenant_id, user_id),
    UNIQUE KEY uk_orders_number (tenant_id, order_number),
    INDEX idx_orders_user (tenant_id, user_id),
    INDEX idx_orders_status (tenant_id, status),
    INDEX idx_orders_date (tenant_id, created)
) COMMENT='Multi-tenant order management';

-- ================================================
-- ORDER ITEMS TABLE
-- ================================================
CREATE TABLE order_items (
    tenant_id INT UNSIGNED NOT NULL,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, order_id, product_id),
    FOREIGN KEY (tenant_id, order_id) REFERENCES orders(tenant_id, order_id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id, product_id) REFERENCES products(tenant_id, product_id)
) COMMENT='Order line items';

-- ================================================
-- PAYMENTS TABLE (Multi-tenant payments)
-- ================================================
CREATE TABLE payments (
    tenant_id INT UNSIGNED NOT NULL,
    payment_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_status ENUM('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    amount DECIMAL(10,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    transaction_id VARCHAR(100) NULL,
    gateway_response JSON NULL,
    notes TEXT NULL,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, payment_id),
    FOREIGN KEY (tenant_id, order_id) REFERENCES orders(tenant_id, order_id) ON DELETE CASCADE,
    INDEX idx_payments_order (tenant_id, order_id),
    INDEX idx_payments_status (tenant_id, payment_status)
) COMMENT='Multi-tenant payment processing';

-- ================================================
-- STORE THEMES TABLE (Multi-tenant theming)
-- ================================================
CREATE TABLE store_themes (
    tenant_id INT UNSIGNED NOT NULL,
    theme_id BIGINT NOT NULL,
    theme_name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    primary_color VARCHAR(7) NOT NULL DEFAULT '#007bff',
    secondary_color VARCHAR(7) NOT NULL DEFAULT '#6c757d',
    accent_color VARCHAR(7) NOT NULL DEFAULT '#28a745',
    background_color VARCHAR(7) NOT NULL DEFAULT '#ffffff',
    text_color VARCHAR(7) NOT NULL DEFAULT '#333333',
    font_family VARCHAR(100) NOT NULL DEFAULT 'Arial, sans-serif',
    logo_url VARCHAR(500) NULL,
    favicon_url VARCHAR(500) NULL,
    custom_css TEXT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, theme_id),
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    INDEX idx_store_themes_active (tenant_id, is_active)
) COMMENT='Multi-tenant store theming';

-- ================================================
-- PAGES TABLE (Multi-tenant CMS)
-- ================================================
CREATE TABLE pages (
    tenant_id INT UNSIGNED NOT NULL,
    page_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(200) NOT NULL,
    content LONGTEXT NULL,
    excerpt TEXT NULL,
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    visibility ENUM('PUBLIC', 'PRIVATE', 'PASSWORD_PROTECTED') NOT NULL DEFAULT 'PUBLIC',
    password VARCHAR(255) NULL,
    template VARCHAR(100) NULL DEFAULT 'default',
    meta_title VARCHAR(150) NULL,
    meta_description VARCHAR(300) NULL,
    meta_keywords VARCHAR(200) NULL,
    featured_image VARCHAR(500) NULL,
    sort_order INT DEFAULT 0,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, page_id),
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    UNIQUE KEY uk_pages_tenant_slug (tenant_id, slug),
    INDEX idx_pages_status (tenant_id, status),
    INDEX idx_pages_visibility (tenant_id, visibility),
    FULLTEXT idx_pages_search (title, content)
) COMMENT='Multi-tenant content management';

-- ================================================
-- TAXES TABLE (Multi-tenant tax management)
-- ================================================
CREATE TABLE taxes (
    tenant_id INT UNSIGNED NOT NULL,
    tax_id BIGINT NOT NULL,
    tax_name VARCHAR(100) NOT NULL,
    tax_type ENUM('PERCENTAGE', 'FIXED_AMOUNT') NOT NULL DEFAULT 'PERCENTAGE',
    tax_rate DECIMAL(5,4) NOT NULL COMMENT 'Tax rate (e.g., 0.1850 for 18.5%)',
    tax_amount DECIMAL(10,2) NULL COMMENT 'Fixed tax amount (for FIXED_AMOUNT type)',
    applicable_on ENUM('SUBTOTAL', 'SHIPPING', 'TOTAL') NOT NULL DEFAULT 'SUBTOTAL',
    region VARCHAR(100) NULL COMMENT 'Geographic region/state',
    product_categories JSON NULL COMMENT 'Applicable product category IDs',
    minimum_order_amount DECIMAL(10,2) NULL COMMENT 'Minimum order amount for tax to apply',
    maximum_tax_amount DECIMAL(10,2) NULL COMMENT 'Maximum tax cap',
    is_compound BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether this tax is calculated on top of other taxes',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NULL,
    effective_until DATE NULL,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, tax_id),
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    INDEX idx_taxes_active (tenant_id, is_active),
    INDEX idx_taxes_region (tenant_id, region)
) COMMENT='Multi-tenant tax configuration';

-- ================================================
-- DISCOUNT CODES TABLE (Multi-tenant promotions)
-- ================================================
CREATE TABLE discount_codes (
    tenant_id INT UNSIGNED NOT NULL,
    discount_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    description TEXT NULL,
    discount_type ENUM('PERCENTAGE', 'FIXED_AMOUNT', 'FREE_SHIPPING') NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    minimum_order_amount DECIMAL(10,2) NULL,
    maximum_discount_amount DECIMAL(10,2) NULL,
    usage_limit INT NULL,
    usage_limit_per_customer INT NULL,
    current_usage_count INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    starts_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    applicable_products JSON NULL COMMENT 'Specific product IDs',
    applicable_categories JSON NULL COMMENT 'Specific category IDs',
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, discount_id),
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    UNIQUE KEY uk_discount_codes_tenant_code (tenant_id, code),
    INDEX idx_discount_codes_active (tenant_id, is_active),
    INDEX idx_discount_codes_dates (tenant_id, starts_at, expires_at)
) COMMENT='Multi-tenant discount codes';

-- ================================================
-- REVIEWS TABLE (Multi-tenant product reviews)
-- ================================================
CREATE TABLE reviews (
    tenant_id INT UNSIGNED NOT NULL,
    review_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(200) NULL,
    review_text TEXT NULL,
    is_verified_purchase BOOLEAN NOT NULL DEFAULT FALSE,
    is_approved BOOLEAN NOT NULL DEFAULT FALSE,
    helpful_count INT NOT NULL DEFAULT 0,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, review_id),
    FOREIGN KEY (tenant_id, product_id) REFERENCES products(tenant_id, product_id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id, user_id) REFERENCES users(tenant_id, user_id) ON DELETE CASCADE,
    UNIQUE KEY uk_reviews_tenant_product_user (tenant_id, product_id, user_id),
    INDEX idx_reviews_product (tenant_id, product_id),
    INDEX idx_reviews_approved (tenant_id, is_approved)
) COMMENT='Multi-tenant product reviews';

-- ================================================
-- DIGITAL DOWNLOADS TABLE (For digital products)
-- ================================================
CREATE TABLE digital_downloads (
    tenant_id INT UNSIGNED NOT NULL,
    download_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    download_token VARCHAR(255) NOT NULL UNIQUE,
    file_path VARCHAR(500) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    max_downloads INT NOT NULL DEFAULT 5,
    current_downloads INT NOT NULL DEFAULT 0,
    expires_at TIMESTAMP NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, download_id),
    FOREIGN KEY (tenant_id, product_id) REFERENCES products(tenant_id, product_id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id, user_id) REFERENCES users(tenant_id, user_id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id, order_id) REFERENCES orders(tenant_id, order_id) ON DELETE CASCADE,
    INDEX idx_digital_downloads_token (download_token),
    INDEX idx_digital_downloads_user (tenant_id, user_id)
) COMMENT='Digital product download management';


-- Add missing tables for multi-tenant system
-- Created: 2025-01-04

-- ================================================
-- PRODUCT_BUNDLES TABLE (Create first for FK reference)
-- ================================================
CREATE TABLE product_bundles (
    tenant_id INT UNSIGNED NOT NULL,
    bundle_id BIGINT NOT NULL,
    bundle_name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    bundle_price DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(2) NOT NULL DEFAULT '0',
    created_by VARCHAR(2) NOT NULL DEFAULT 'sy',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL DEFAULT 'sy',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, bundle_id),
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    INDEX idx_product_bundles_status (tenant_id, status)
) COMMENT='Product bundles';

-- ================================================
-- BUNDLEITEMS TABLE (Product bundle items)
-- ================================================
CREATE TABLE bundleitems (
    tenant_id INT UNSIGNED NOT NULL,
    bundle_item_id BIGINT NOT NULL,
    bundle_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    status VARCHAR(2) NOT NULL DEFAULT '0',
    created_by VARCHAR(2) NOT NULL DEFAULT 'sy',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL DEFAULT 'sy',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, bundle_item_id),
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id, bundle_id) REFERENCES product_bundles(tenant_id, bundle_id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id, product_id) REFERENCES products(tenant_id, product_id) ON DELETE CASCADE,
    INDEX idx_bundleitems_bundle (tenant_id, bundle_id),
    INDEX idx_bundleitems_product (tenant_id, product_id)
) COMMENT='Product bundle item mappings';

-- ================================================
-- DIGITAL_PRODUCT_DETAILS TABLE
-- ================================================
CREATE TABLE digital_product_details (
    tenant_id INT UNSIGNED NOT NULL,
    product_id BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    download_limit INT NOT NULL DEFAULT 5,
    expiry_days INT NOT NULL DEFAULT 30,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, product_id),
    FOREIGN KEY (tenant_id, product_id) REFERENCES products(tenant_id, product_id) ON DELETE CASCADE
) COMMENT='Digital product file details';

-- ================================================
-- PAYMENT_AUDIT TABLE
-- ================================================
CREATE TABLE payment_audit (
    tenant_id INT UNSIGNED NOT NULL,
    audit_id BIGINT NOT NULL,
    payment_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(20) NULL,
    new_status VARCHAR(20) NOT NULL,
    details TEXT NULL,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, audit_id),
    FOREIGN KEY (tenant_id, payment_id) REFERENCES payments(tenant_id, payment_id) ON DELETE CASCADE,
    INDEX idx_payment_audit_payment (tenant_id, payment_id),
    INDEX idx_payment_audit_action (tenant_id, action)
) COMMENT='Payment audit trail';

-- ================================================
-- DISCOUNT_USAGE TABLE
-- ================================================
CREATE TABLE discount_usage (
    tenant_id INT UNSIGNED NOT NULL,
    usage_id BIGINT NOT NULL,
    discount_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT NULL,
    usage_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (tenant_id, usage_id),
    FOREIGN KEY (tenant_id, discount_id) REFERENCES discount_codes(tenant_id, discount_id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id, user_id) REFERENCES users(tenant_id, user_id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id, order_id) REFERENCES orders(tenant_id, order_id) ON DELETE CASCADE,
    INDEX idx_discount_usage_discount (tenant_id, discount_id),
    INDEX idx_discount_usage_user (tenant_id, user_id)
) COMMENT='Discount code usage tracking';

-- ================================================
-- INITIAL DATA SETUP
-- ================================================

-- Insert default system tenant (ID: 1)
INSERT INTO tenants (
    tenant_id, shop_name, shop_email, domain_name, store_password,
    business_type, subscription_plan, status
) VALUES (
    1, 'System Store', 'admin@inventrik.com', 'system',
    '$2a$10$dXJ3SW6G7P2lkpBpIomIIOL.8IZE3zWpJsU8VZGDIwZE6TJQ.UzIy', -- password: admin
    'COMPANY', 'ENTERPRISE', 'A'
);

-- Insert default system admin user
INSERT INTO users (
    tenant_id, user_id, username, first_name, last_name, email,
    user_role, password_hash, status
) VALUES (
    1, 1, 'admin', 'System', 'Administrator', 'admin@inventrik.com',
    'SYSTEM_ADMIN', '$2a$10$dXJ3SW6G7P2lkpBpIomIIOL.8IZE3zWpJsU8VZGDIwZE6TJQ.UzIy', 'A'
);

-- Insert default currency (USD)
INSERT INTO currencies (
    tenant_id, currency_code, currency_name, currency_symbol,
    is_base_currency, is_active
) VALUES (
    1, 'USD', 'US Dollar', '$', TRUE, TRUE
);

-- Insert default category
INSERT INTO categories (
    tenant_id, category_id, category_name, description, is_active
) VALUES (
    1, 1, 'Default Category', 'Default product category', TRUE
);

-- Insert default store theme
INSERT INTO store_themes (
    tenant_id, theme_id, theme_name, description, is_active
) VALUES (
    1, 1, 'Default Theme', 'Default store theme', TRUE
);

-- Insert default pages
INSERT INTO pages (
    tenant_id, page_id, title, slug, content, status, visibility
) VALUES 
(1, 1, 'Home', 'home', '<h1>Welcome to Our Store</h1><p>This is the home page.</p>', 'PUBLISHED', 'PUBLIC'),
(1, 2, 'About Us', 'about', '<h1>About Us</h1><p>Learn more about our company.</p>', 'PUBLISHED', 'PUBLIC'),
(1, 3, 'Contact', 'contact', '<h1>Contact Us</h1><p>Get in touch with us.</p>', 'PUBLISHED', 'PUBLIC');

-- Insert default tax configuration
INSERT INTO taxes (
    tenant_id, tax_id, tax_name, tax_type, tax_rate, is_active
) VALUES (
    1, 1, 'Default Sales Tax', 'PERCENTAGE', 0.0000, TRUE
);
