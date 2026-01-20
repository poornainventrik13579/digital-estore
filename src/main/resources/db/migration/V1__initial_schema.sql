-- Create Users table
CREATE TABLE users (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID',
    user_id VARCHAR(32) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    image VARCHAR(256),
    phone VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    user_type ENUM('INDIVIDUAL', 'COMPANY') NOT NULL DEFAULT 'INDIVIDUAL',
    company_name VARCHAR(100),
    company_registration_number VARCHAR(50),
    company_address1 VARCHAR(255),
    company_address2 VARCHAR(255),
    company_country VARCHAR(255),
    company_pincode VARCHAR(255),
    tax_id VARCHAR(50),
    otp VARCHAR(8) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(2) NOT NULL, -- -1 INACTIVE, 0 ACTIVE
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, user_id)
);

-- Create Categories table
CREATE TABLE categories (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID',
    category_id VARCHAR(32) NOT NULL,
    category_name VARCHAR(50) NOT NULL,
    description TEXT,
    status VARCHAR(2) NOT NULL, -- -1 INACTIVE, 0 ACTIVE
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, category_id)
);

-- Create Products table
CREATE TABLE products (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID',
    product_id VARCHAR(32) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    description TEXT,
    default_price DECIMAL(10, 2) NOT NULL,
    default_currency VARCHAR(3) NOT NULL,
    category_id VARCHAR(32),
    status VARCHAR(2) NOT NULL, -- -1 INACTIVE, 0 ACTIVE
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, product_id)
);

-- Create DigitalProductDetails table
CREATE TABLE digital_product_details (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID',
    product_id VARCHAR(32) NOT NULL,
    file_url VARCHAR(255) NOT NULL,
    file_size INT,
    file_format VARCHAR(20),
    license_info TEXT,
    version VARCHAR(20),
    status VARCHAR(2) NOT NULL, -- -1 INACTIVE, 0 ACTIVE
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, product_id)
);

-- Create Orders table
CREATE TABLE orders (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID',
    order_id VARCHAR(32) NOT NULL,
    user_id VARCHAR(32) NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    currency VARCHAR(3),
    total_amount DECIMAL(10, 2) NOT NULL,
    exchange_rate DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'Pending', -- e.g., Pending, Completed, Cancelled
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, order_id)
);

-- Create Order Items table
CREATE TABLE order_items (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID',
    order_id VARCHAR(32) NOT NULL,
    order_item_id VARCHAR(32) NOT NULL,
    product_id VARCHAR(32) NOT NULL,
    price_at_purchase DECIMAL(10, 2) NOT NULL,
    license_key VARCHAR(100),
    status VARCHAR(2) NOT NULL, -- -1 INACTIVE, 0 ACTIVE
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, order_id, order_item_id)
);

-- Create Payments table
CREATE TABLE payments (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID',
    payment_id VARCHAR(32) NOT NULL,
    order_id VARCHAR(32) NOT NULL,
    currency VARCHAR(3),
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount DECIMAL(10, 2) NOT NULL,
    refunded_amount DECIMAL(10, 2) DEFAULT 0.00,
    payment_method VARCHAR(50),
    transaction_id VARCHAR(100),
    status VARCHAR(20) DEFAULT 'Pending',
    refund_reason TEXT,
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, payment_id)
);

-- Create Payment Audit Log table
CREATE TABLE payment_audit_log (
    audit_id VARCHAR(36) NOT NULL,
    payment_id VARCHAR(32) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_details TEXT,
    performed_by VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    PRIMARY KEY (audit_id)
);