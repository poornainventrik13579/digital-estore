-- Create DigitalProductDetails table
CREATE TABLE DigitalProductDetails (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID',
    product_id BIGINT NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size BIGINT,
    file_format VARCHAR(50),
    license_info TEXT,
    version VARCHAR(20),
    download_limit INT,
    expiry_days INT,
    file_hash VARCHAR(64),
    status VARCHAR(2) NOT NULL DEFAULT '0', -- -1 INACTIVE, 0 ACTIVE
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, product_id),
    FOREIGN KEY (tenant_id, product_id) REFERENCES Products(tenant_id, product_id) ON DELETE CASCADE
);

-- Create DigitalDownloads table
CREATE TABLE DigitalDownloads (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID',
    download_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    download_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    download_token VARCHAR(100) UNIQUE,
    token_expiry TIMESTAMP,
    file_size_downloaded BIGINT,
    download_status VARCHAR(20) DEFAULT 'INITIATED', -- INITIATED, IN_PROGRESS, COMPLETED, FAILED, EXPIRED
    status VARCHAR(2) NOT NULL DEFAULT '0', -- -1 INACTIVE, 0 ACTIVE
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, download_id),
    INDEX idx_order_item_id (order_item_id),
    INDEX idx_download_token (download_token),
    INDEX idx_token_expiry (token_expiry),
    INDEX idx_download_status (download_status),
    INDEX idx_ip_address (ip_address)
);

-- Add indexes for better performance
CREATE INDEX idx_digital_product_details_status ON DigitalProductDetails(tenant_id, status);
CREATE INDEX idx_digital_product_details_format ON DigitalProductDetails(tenant_id, file_format);
CREATE INDEX idx_digital_downloads_date ON DigitalDownloads(tenant_id, download_date);
CREATE INDEX idx_digital_downloads_user_product ON DigitalDownloads(tenant_id, order_item_id, download_status);