-- Create DigitalProductDetails table (EXACTLY as per design brief)
CREATE TABLE DigitalProductDetails (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID',
    product_id BIGINT NOT NULL,
    file_url VARCHAR(255) NOT NULL, -- could be an S3 URL or similar
    file_size INT, -- size in KB/MB
    file_format VARCHAR(20), -- e.g., PDF, MP3, MP4, etc.
    license_info TEXT, -- terms or license keys
    version VARCHAR(20),
    status VARCHAR(2) NOT NULL, -- -1 INACTIVE, 0 ACTIVE
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, product_id)
    -- FOREIGN KEY (product_id) REFERENCES Products(product_id)
);

-- Create DigitalDownloads table (EXACTLY as per design brief)
CREATE TABLE DigitalDownloads (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID',
    download_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    download_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    status VARCHAR(2) NOT NULL DEFAULT '0', -- 0:ACTIVE , -1 INACTIVE
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, download_id)
    -- FOREIGN KEY (order_item_id) REFERENCES OrderItems(order_item_id)
);