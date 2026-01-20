-- V3__digital_downloads.sql
-- Create DigitalDownloads table for tracking digital product downloads

-- Create DigitalDownloads table (EXACTLY as per design brief)
CREATE TABLE IF NOT EXISTS digital_downloads (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID',
    download_id VARCHAR(32) NOT NULL,
    order_item_id VARCHAR(32) NOT NULL,
    download_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    status VARCHAR(2) NOT NULL DEFAULT '0', -- 0:ACTIVE , -1 INACTIVE
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, download_id),
    INDEX idx_downloads_order_item (tenant_id, order_item_id),
    INDEX idx_downloads_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 