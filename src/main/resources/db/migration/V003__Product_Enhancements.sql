CREATE TABLE productbundles (
    tenant_id INT(10) UNSIGNED NOT NULL COMMENT 'Tenant ID for multi-tenancy',
    bundle_id VARCHAR(32) NOT NULL COMMENT 'Unique bundle identifier',
    bundle_name VARCHAR(100) NOT NULL COMMENT 'Bundle display name',
    description TEXT COMMENT 'Bundle description',
    bundle_price DECIMAL(10, 2) NOT NULL COMMENT 'Bundle price',
    discount_percent DECIMAL(5, 2) DEFAULT 0.00 COMMENT 'Discount percentage',
    currency VARCHAR(3) NOT NULL DEFAULT 'USD' COMMENT 'Bundle currency',
    status VARCHAR(2) NOT NULL DEFAULT '0' COMMENT 'Bundle status: -1=Inactive, 0=Active',
    created_by VARCHAR(2) NOT NULL COMMENT 'User who created this record',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_by VARCHAR(2) NOT NULL COMMENT 'User who last updated this record',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    PRIMARY KEY (tenant_id, bundle_id),
    INDEX idx_bundle_status (tenant_id, status),
    INDEX idx_bundle_name (tenant_id, bundle_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Product bundles with pricing';
CREATE TABLE bundleitems (
    tenant_id INT(10) UNSIGNED NOT NULL COMMENT 'Tenant ID for multi-tenancy',
    bundle_item_id VARCHAR(32) NOT NULL COMMENT 'Unique bundle item identifier',
    bundle_id VARCHAR(32) NOT NULL COMMENT 'Parent bundle ID',
    product_id VARCHAR(32) NOT NULL COMMENT 'Product in bundle',
    quantity INT NOT NULL DEFAULT 1 COMMENT 'Quantity of product in bundle',
    status VARCHAR(2) NOT NULL DEFAULT '0' COMMENT 'Item status: -1=Inactive, 0=Active',
    created_by VARCHAR(2) NOT NULL COMMENT 'User who created this record',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_by VARCHAR(2) NOT NULL COMMENT 'User who last updated this record',
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    PRIMARY KEY (tenant_id, bundle_item_id),
    INDEX idx_bundle_items_bundle (tenant_id, bundle_id),
    INDEX idx_bundle_items_product (tenant_id, product_id),
    INDEX idx_bundle_items_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Products within bundles';
ALTER TABLE products
ADD COLUMN image1_url VARCHAR(256) COMMENT 'Primary product image URL',
ADD COLUMN image2_url VARCHAR(256) COMMENT 'Secondary product image URL 1',
ADD COLUMN image3_url VARCHAR(256) COMMENT 'Secondary product image URL 2',
ADD COLUMN image4_url VARCHAR(256) COMMENT 'Secondary product image URL 3',
ADD COLUMN image5_url VARCHAR(256) COMMENT 'Secondary product image URL 4',
ADD COLUMN banner VARCHAR(256) COMMENT 'Banner image URL',
ADD COLUMN thumbnail VARCHAR(256) COMMENT 'Thumbnail image URL',
ADD COLUMN metadata TEXT COMMENT 'Product metadata in JSON format';
