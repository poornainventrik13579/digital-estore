CREATE TABLE digital_downloads (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID for multi-tenancy',
    download_id VARCHAR(32) NOT NULL COMMENT 'Unique download identifier',
    order_item_id VARCHAR(32) NOT NULL COMMENT 'Associated order item ID',
    download_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Download timestamp',
    ip_address VARCHAR(45) COMMENT 'Client IP address (supports IPv6)',
    status VARCHAR(2) NOT NULL DEFAULT '0' COMMENT 'Download status: -1=Inactive, 0=Active',
    created_by VARCHAR(2) NOT NULL COMMENT 'User who created this record',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_by VARCHAR(2) NOT NULL COMMENT 'User who last updated this record',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    PRIMARY KEY (tenant_id, download_id),
    INDEX idx_downloads_order_item (tenant_id, order_item_id),
    INDEX idx_downloads_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tracks digital product downloads';
CREATE TABLE currencies (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID for multi-tenancy',
    currency_code VARCHAR(3) NOT NULL COMMENT 'ISO 4217 currency code',
    currency_name VARCHAR(50) NOT NULL COMMENT 'Display name of currency',
    is_default VARCHAR(1) NOT NULL COMMENT 'Is default currency: 1=Yes, 0=No',
    exchange_rate DECIMAL(10,4) NOT NULL COMMENT 'Exchange rate from base currency',
    symbol VARCHAR(10) NOT NULL COMMENT 'Currency symbol',
    status VARCHAR(2) NOT NULL COMMENT 'Currency status: -1=Inactive, 0=Active',
    created_by VARCHAR(2) NOT NULL COMMENT 'User who created this record',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_by VARCHAR(2) NOT NULL COMMENT 'User who last updated this record',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    PRIMARY KEY (tenant_id, currency_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Supported currencies and exchange rates';
CREATE TABLE product_prices (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID for multi-tenancy',
    product_id VARCHAR(32) NOT NULL COMMENT 'Associated product ID',
    currency_code VARCHAR(3) NOT NULL COMMENT 'ISO currency code',
    price DECIMAL(10, 2) NOT NULL COMMENT 'Price in this currency',
    status VARCHAR(2) NOT NULL COMMENT 'Price status: -1=Inactive, 0=Active',
    created_by VARCHAR(2) NOT NULL COMMENT 'User who created this record',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_by VARCHAR(2) NOT NULL COMMENT 'User who last updated this record',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    PRIMARY KEY (tenant_id, product_id, currency_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Multi-currency product pricing';
INSERT INTO currencies (tenant_id, currency_code, currency_name, is_default, exchange_rate, symbol, status, created_by, updated_by)
VALUES
(1, 'USD', 'US Dollar', '1', 1.0000, '$', '0', 'SY', 'SY'),
(1, 'EUR', 'Euro', '0', 0.8500, '€', '0', 'SY', 'SY'),
(1, 'SGD', 'Singapore Dollar', '0', 1.3500, 'S$', '0', 'SY', 'SY');
CREATE TABLE reviews (
    tenant_id INT(10) UNSIGNED NOT NULL COMMENT 'Tenant ID for multi-tenancy',
    review_id VARCHAR(32) NOT NULL COMMENT 'Unique review identifier',
    product_id VARCHAR(32) NOT NULL COMMENT 'Reviewed product ID',
    user_id VARCHAR(32) NOT NULL COMMENT 'Reviewer user ID',
    rating INT NOT NULL COMMENT 'Rating from 1 to 5 stars',
    comment TEXT COMMENT 'Review text content',
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Review submission date',
    verified BOOLEAN DEFAULT FALSE COMMENT 'Purchase verified status',
    status VARCHAR(2) NOT NULL DEFAULT '0' COMMENT 'Review status: -1=Inactive, 0=Active',
    created_by VARCHAR(2) NOT NULL COMMENT 'User who created this record',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_by VARCHAR(2) NOT NULL COMMENT 'User who last updated this record',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    PRIMARY KEY (tenant_id, review_id),
    INDEX idx_reviews_product (tenant_id, product_id),
    INDEX idx_reviews_user (tenant_id, user_id),
    INDEX idx_reviews_rating (tenant_id, rating),
    INDEX idx_reviews_status (tenant_id, status),
    CONSTRAINT chk_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Customer product reviews and ratings';
CREATE TABLE discount_codes (
    tenant_id INT(10) UNSIGNED NOT NULL COMMENT 'Tenant ID for multi-tenancy',
    discount_id VARCHAR(32) NOT NULL COMMENT 'Unique discount identifier',
    code VARCHAR(50) NOT NULL COMMENT 'Discount code string',
    discount_type ENUM('PERCENTAGE', 'FIXED') NOT NULL DEFAULT 'PERCENTAGE' COMMENT 'Type of discount',
    discount_value DECIMAL(10, 2) NOT NULL COMMENT 'Discount amount or percentage',
    min_order_amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT 'Minimum order to apply discount',
    max_uses INT DEFAULT 0 COMMENT 'Maximum usage count (0=unlimited)',
    used_count INT DEFAULT 0 COMMENT 'Current usage count',
    valid_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Discount start date',
    valid_to TIMESTAMP NULL COMMENT 'Discount expiration date',
    status VARCHAR(2) NOT NULL DEFAULT '0' COMMENT 'Discount status: -1=Inactive, 0=Active',
    created_by VARCHAR(2) NOT NULL COMMENT 'User who created this record',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_by VARCHAR(2) NOT NULL COMMENT 'User who last updated this record',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    PRIMARY KEY (tenant_id, discount_id),
    UNIQUE KEY uk_discount_code (tenant_id, code),
    INDEX idx_discount_status (tenant_id, status),
    INDEX idx_discount_validity (tenant_id, valid_from, valid_to),
    INDEX idx_discount_code (tenant_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Discount and coupon codes';
CREATE TABLE discount_usage (
    tenant_id INT(10) UNSIGNED NOT NULL COMMENT 'Tenant ID for multi-tenancy',
    usage_id VARCHAR(32) NOT NULL COMMENT 'Unique usage identifier',
    discount_id VARCHAR(32) NOT NULL COMMENT 'Associated discount code ID',
    order_id VARCHAR(32) NOT NULL COMMENT 'Order where discount applied',
    user_id VARCHAR(32) NOT NULL COMMENT 'User who used the discount',
    discount_amount DECIMAL(10, 2) NOT NULL COMMENT 'Amount discounted',
    used_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Usage timestamp',
    status VARCHAR(2) NOT NULL DEFAULT '0' COMMENT 'Usage status: -1=Inactive, 0=Active',
    created_by VARCHAR(2) NOT NULL COMMENT 'User who created this record',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_by VARCHAR(2) NOT NULL COMMENT 'User who last updated this record',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    PRIMARY KEY (tenant_id, usage_id),
    INDEX idx_usage_discount (tenant_id, discount_id),
    INDEX idx_usage_order (tenant_id, order_id),
    INDEX idx_usage_user (tenant_id, user_id),
    INDEX idx_usage_date (tenant_id, used_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Discount code usage tracking';
