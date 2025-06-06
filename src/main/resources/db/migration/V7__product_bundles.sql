-- V7__product_bundles.sql
-- Product bundles system migration

-- Create ProductBundles table
CREATE TABLE productbundles (
    tenant_id INT(10) UNSIGNED NOT NULL COMMENT 'Tenant ID',
    bundle_id BIGINT(15) NOT NULL,
    bundle_name VARCHAR(100) NOT NULL,
    description TEXT,
    bundle_price DECIMAL(10, 2) NOT NULL,
    discount_percent DECIMAL(5, 2) DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(2) NOT NULL DEFAULT '0', -- 0: ACTIVE, -1: INACTIVE
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, bundle_id),
    INDEX idx_bundle_status (tenant_id, status),
    INDEX idx_bundle_name (tenant_id, bundle_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create BundleItems table
CREATE TABLE bundleitems (
    tenant_id INT(10) UNSIGNED NOT NULL COMMENT 'Tenant ID',
    bundle_item_id BIGINT(15) NOT NULL,
    bundle_id BIGINT(15) NOT NULL,
    product_id BIGINT(15) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    status VARCHAR(2) NOT NULL DEFAULT '0', -- 0: ACTIVE, -1: INACTIVE
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, bundle_item_id),
    INDEX idx_bundle_items_bundle (tenant_id, bundle_id),
    INDEX idx_bundle_items_product (tenant_id, product_id),
    INDEX idx_bundle_items_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 