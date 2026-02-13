CREATE TABLE users (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID for multi-tenancy',
    user_id VARCHAR(32) NOT NULL COMMENT 'Unique user identifier',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Unique username for login',
    first_name VARCHAR(50) COMMENT 'User first name',
    last_name VARCHAR(50) COMMENT 'User last name',
    image VARCHAR(256) COMMENT 'Profile image URL',
    phone VARCHAR(100) NOT NULL UNIQUE COMMENT 'Contact phone number',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT 'Contact email address',
    user_type ENUM('INDIVIDUAL', 'COMPANY') NOT NULL DEFAULT 'INDIVIDUAL' COMMENT 'User account type',
    company_name VARCHAR(100) COMMENT 'Company name for business accounts',
    company_registration_number VARCHAR(50) COMMENT 'Company registration ID',
    company_address1 VARCHAR(255) COMMENT 'Primary business address',
    company_address2 VARCHAR(255) COMMENT 'Secondary business address',
    company_country VARCHAR(255) COMMENT 'Country of business operation',
    company_pincode VARCHAR(255) COMMENT 'Postal/ZIP code',
    tax_id VARCHAR(50) COMMENT 'Tax identification number',
    otp VARCHAR(8) NOT NULL COMMENT 'One-time password for verification',
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed password',
    status VARCHAR(2) NOT NULL COMMENT 'Account status: -1=Inactive, 0=Active',
    created_by VARCHAR(2) NOT NULL COMMENT 'User who created this record',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_by VARCHAR(2) NOT NULL COMMENT 'User who last updated this record',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    PRIMARY KEY (tenant_id, user_id),
    INDEX idx_users_email (tenant_id, email),
    INDEX idx_users_phone (tenant_id, phone),
    INDEX idx_users_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User accounts for individual and business customers';
CREATE TABLE categories (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID for multi-tenancy',
    category_id VARCHAR(32) NOT NULL COMMENT 'Unique category identifier',
    category_name VARCHAR(50) NOT NULL COMMENT 'Display name of category',
    description TEXT COMMENT 'Category description',
    status VARCHAR(2) NOT NULL COMMENT 'Category status: -1=Inactive, 0=Active',
    created_by VARCHAR(2) NOT NULL COMMENT 'User who created this record',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_by VARCHAR(2) NOT NULL COMMENT 'User who last updated this record',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    PRIMARY KEY (tenant_id, category_id),
    INDEX idx_categories_status (tenant_id, status),
    INDEX idx_categories_name (tenant_id, category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Product categorization';
CREATE TABLE products (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID for multi-tenancy',
    product_id VARCHAR(32) NOT NULL COMMENT 'Unique product identifier',
    product_name VARCHAR(100) NOT NULL COMMENT 'Product display name',
    description TEXT COMMENT 'Detailed product description',
    default_price DECIMAL(10, 2) NOT NULL COMMENT 'Base price in default currency',
    default_currency VARCHAR(3) NOT NULL COMMENT 'ISO currency code',
    category_id VARCHAR(32) COMMENT 'Associated category ID',
    status VARCHAR(2) NOT NULL COMMENT 'Product status: -1=Inactive, 0=Active',
    created_by VARCHAR(2) NOT NULL COMMENT 'User who created this record',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_by VARCHAR(2) NOT NULL COMMENT 'User who last updated this record',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    PRIMARY KEY (tenant_id, product_id),
    INDEX idx_products_tenant_category (tenant_id, category_id),
    INDEX idx_products_tenant_status (tenant_id, status),
    INDEX idx_products_tenant_name (tenant_id, product_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Digital products catalog';
CREATE TABLE digital_product_details (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID for multi-tenancy',
    product_id VARCHAR(32) NOT NULL COMMENT 'Associated product ID',
    file_url VARCHAR(255) NOT NULL COMMENT 'Downloadable file URL',
    file_size INT COMMENT 'File size in bytes',
    file_format VARCHAR(20) COMMENT 'File extension/type',
    license_info TEXT COMMENT 'License terms and conditions',
    version VARCHAR(20) COMMENT 'Product version number',
    status VARCHAR(2) NOT NULL COMMENT 'Details status: -1=Inactive, 0=Active',
    created_by VARCHAR(2) NOT NULL COMMENT 'User who created this record',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_by VARCHAR(2) NOT NULL COMMENT 'User who last updated this record',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    PRIMARY KEY (tenant_id, product_id),
    INDEX idx_digital_details_tenant_product (tenant_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Digital product file metadata';
CREATE TABLE orders (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID for multi-tenancy',
    order_id VARCHAR(32) NOT NULL COMMENT 'Unique order identifier',
    user_id VARCHAR(32) NOT NULL COMMENT 'Customer user ID',
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Order placement timestamp',
    currency VARCHAR(3) COMMENT 'Transaction currency',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT 'Order total amount',
    exchange_rate DECIMAL(10, 2) NOT NULL COMMENT 'Currency conversion rate',
    status VARCHAR(20) DEFAULT 'Pending' COMMENT 'Order status: Pending, Processing, Completed, Cancelled, Refunded, Partially Refunded',
    created_by VARCHAR(2) NOT NULL COMMENT 'User who created this record',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_by VARCHAR(2) NOT NULL COMMENT 'User who last updated this record',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    PRIMARY KEY (tenant_id, order_id),
    INDEX idx_orders_tenant_user (tenant_id, user_id, order_date),
    INDEX idx_orders_tenant_status (tenant_id, status, order_date),
    INDEX idx_orders_tenant_date (tenant_id, order_date DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Customer orders';
CREATE TABLE order_items (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID for multi-tenancy',
    order_id VARCHAR(32) NOT NULL COMMENT 'Parent order ID',
    order_item_id VARCHAR(32) NOT NULL COMMENT 'Unique line item identifier',
    product_id VARCHAR(32) NOT NULL COMMENT 'Product identifier',
    price_at_purchase DECIMAL(10, 2) NOT NULL COMMENT 'Price when ordered',
    license_key VARCHAR(100) COMMENT 'Generated license key',
    status VARCHAR(2) NOT NULL COMMENT 'Item status: -1=Inactive, 0=Active',
    created_by VARCHAR(2) NOT NULL COMMENT 'User who created this record',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_by VARCHAR(2) NOT NULL COMMENT 'User who last updated this record',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    PRIMARY KEY (tenant_id, order_id, order_item_id),
    INDEX idx_order_items_tenant_order (tenant_id, order_id),
    INDEX idx_order_items_tenant_product (tenant_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Order line items';
CREATE TABLE payments (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID for multi-tenancy',
    payment_id VARCHAR(32) NOT NULL COMMENT 'Unique payment identifier',
    order_id VARCHAR(32) NOT NULL COMMENT 'Associated order ID',
    currency VARCHAR(3) COMMENT 'Payment currency',
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Payment timestamp',
    amount DECIMAL(10, 2) NOT NULL COMMENT 'Payment amount',
    refunded_amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT 'Total refunded amount',
    payment_method VARCHAR(50) COMMENT 'Payment method used',
    transaction_id VARCHAR(100) COMMENT 'External transaction ID',
    status VARCHAR(20) DEFAULT 'Pending' COMMENT 'Payment status',
    refund_reason TEXT COMMENT 'Reason for refund',
    created_by VARCHAR(2) NOT NULL COMMENT 'User who created this record',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_by VARCHAR(2) NOT NULL COMMENT 'User who last updated this record',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    PRIMARY KEY (tenant_id, payment_id),
    INDEX idx_payments_tenant_order (tenant_id, order_id),
    INDEX idx_payments_tenant_status (tenant_id, status, payment_date),
    INDEX idx_payments_tenant_date (tenant_id, payment_date DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Payment transactions';
CREATE TABLE payment_audit_log (
    audit_id VARCHAR(36) NOT NULL COMMENT 'Unique audit entry ID',
    payment_id VARCHAR(32) NOT NULL COMMENT 'Associated payment ID',
    event_type VARCHAR(50) NOT NULL COMMENT 'Type of payment event',
    event_details TEXT COMMENT 'Detailed event information',
    performed_by VARCHAR(50) NOT NULL COMMENT 'User who performed the action',
    timestamp TIMESTAMP NOT NULL COMMENT 'Event timestamp',
    PRIMARY KEY (audit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Payment audit trail';
INSERT INTO users (tenant_id, user_id, username, first_name, last_name, email, phone, user_type, otp, password_hash, status, created_by, updated_by)
VALUES
(1, 1000000001, 'john_doe', 'John', 'Doe', 'john.doe@example.com', '+1-555-0001', 'INDIVIDUAL', '123456', '$2a$10$dummy.hash.for.password123', '0', 'SY', 'SY'),
(1, 1000000002, 'jane_smith', 'Jane', 'Smith', 'jane.smith@example.com', '+1-555-0002', 'INDIVIDUAL', '234567', '$2a$10$dummy.hash.for.password456', '0', 'SY', 'SY'),
(1, 1000000003, 'acme_corp', 'Business', 'User', 'business@acme.com', '+1-555-0003', 'COMPANY', '345678', '$2a$10$dummy.hash.for.password789', '0', 'SY', 'SY');
INSERT INTO categories (tenant_id, category_id, category_name, description, status, created_by, updated_by)
VALUES
(1, 1000000001, 'E-books', 'Digital books and publications', '0', 'SY', 'SY'),
(1, 1000000002, 'Software', 'Digital software and applications', '0', 'SY', 'SY'),
(1, 1000000003, 'Music', 'Digital music files and albums', '0', 'SY', 'SY'),
(1, 1000000004, 'Videos', 'Digital video content', '0', 'SY', 'SY');
INSERT INTO products (tenant_id, product_id, product_name, description, default_price, default_currency, category_id, status, created_by, updated_by)
VALUES
(1, 1000000001, 'Complete Java Guide', 'Comprehensive guide to Java programming', 29.99, 'USD', 1000000001, '0', 'SY', 'SY'),
(1, 1000000002, 'Photo Editor Pro', 'Professional photo editing software', 59.99, 'USD', 1000000002, '0', 'SY', 'SY'),
(1, 1000000003, 'Classical Music Collection', 'Collection of classical music pieces', 19.99, 'USD', 1000000003, '0', 'SY', 'SY'),
(1, 1000000004, 'Web Development Course', 'Complete web development video course', 99.99, 'USD', 1000000004, '0', 'SY', 'SY');
INSERT INTO orders (tenant_id, order_id, user_id, currency, total_amount, exchange_rate, status, created_by, updated_by)
VALUES
(1, 1000000001, 1000000001, 'USD', 29.99, 1.00, 'Completed', 'SY', 'SY'),
(1, 1000000002, 1000000002, 'USD', 59.99, 1.00, 'Processing', 'SY', 'SY'),
(1, 1000000003, 1000000003, 'USD', 19.99, 1.00, 'Pending', 'SY', 'SY');
INSERT INTO order_items (tenant_id, order_id, order_item_id, product_id, price_at_purchase, license_key, status, created_by, updated_by)
VALUES
(1, 1000000001, 1000000001, 1000000001, 29.99, 'JAVA-GUIDE-2024-XYZ', '0', 'SY', 'SY'),
(1, 1000000002, 1000000002, 1000000002, 59.99, 'PHOTO-EDIT-PRO-ABC', '0', 'SY', 'SY'),
(1, 1000000003, 1000000003, 1000000003, 19.99, 'MUSIC-CLASSIC-DEF', '0', 'SY', 'SY');
INSERT INTO payments (tenant_id, payment_id, order_id, currency, amount, refunded_amount, payment_method, transaction_id, status, created_by, updated_by)
VALUES
(1, 1000000001, 1000000001, 'USD', 29.99, 0.00, 'Credit Card', 'stripe_pi_test123', 'Successful', 'SY', 'SY'),
(1, 1000000002, 1000000002, 'USD', 59.99, 0.00, 'Credit Card', 'stripe_pi_test456', 'Processing', 'SY', 'SY'),
(1, 1000000003, 1000000003, 'USD', 19.99, 0.00, 'Credit Card', 'stripe_pi_test789', 'Pending', 'SY', 'SY');
