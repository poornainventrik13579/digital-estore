-- V6__discount_codes.sql
-- Discount codes system migration

-- Create DiscountCodes table
CREATE TABLE DiscountCodes (
    tenant_id INT(10) UNSIGNED NOT NULL COMMENT 'Tenant ID',
    discount_id BIGINT(15) NOT NULL,
    code VARCHAR(50) NOT NULL,
    discount_type ENUM('PERCENTAGE', 'FIXED') NOT NULL DEFAULT 'PERCENTAGE',
    discount_value DECIMAL(10, 2) NOT NULL,
    min_order_amount DECIMAL(10, 2) DEFAULT 0.00,
    max_uses INT DEFAULT 0, -- 0 means unlimited
    used_count INT DEFAULT 0,
    valid_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valid_to TIMESTAMP NULL,
    status VARCHAR(2) NOT NULL DEFAULT '0', -- 0: ACTIVE, -1: INACTIVE
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, discount_id),
    UNIQUE KEY uk_discount_code (tenant_id, code),
    INDEX idx_discount_status (tenant_id, status),
    INDEX idx_discount_validity (tenant_id, valid_from, valid_to),
    INDEX idx_discount_code (tenant_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create DiscountUsage table to track usage
CREATE TABLE DiscountUsage (
    tenant_id INT(10) UNSIGNED NOT NULL COMMENT 'Tenant ID',
    usage_id BIGINT(15) NOT NULL,
    discount_id BIGINT(15) NOT NULL,
    order_id BIGINT(15) NOT NULL,
    user_id BIGINT(15) NOT NULL,
    discount_amount DECIMAL(10, 2) NOT NULL,
    used_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(2) NOT NULL DEFAULT '0', -- 0: ACTIVE, -1: INACTIVE
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, usage_id),
    INDEX idx_usage_discount (tenant_id, discount_id),
    INDEX idx_usage_order (tenant_id, order_id),
    INDEX idx_usage_user (tenant_id, user_id),
    INDEX idx_usage_date (tenant_id, used_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 