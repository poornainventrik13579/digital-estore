-- Sample DigitalProductDetails data
INSERT INTO DigitalProductDetails (tenant_id, product_id, file_url, file_size, file_format, license_info, version, status, created_by, updated_by, created, updated)
VALUES
(1, 1000000001, 'https://storage.example.com/ebooks/java-guide.pdf', 5120, 'PDF', 'Personal use license', '1.0', '0', 'sy', 'sy', NOW(), NOW()),
(1, 1000000002, 'https://storage.example.com/software/photo-editor.zip', 102400, 'ZIP', 'Single user license', '2.1', '0', 'sy', 'sy', NOW(), NOW()),
(1, 1000000003, 'https://storage.example.com/music/classical-collection.zip', 256000, 'ZIP', 'Personal listening license', '1.0', '0', 'sy', 'sy', NOW(), NOW()),
(1, 1000000004, 'https://storage.example.com/videos/web-dev-course.mp4', 1048576, 'MP4', 'Educational use license', '1.5', '0', 'sy', 'sy', NOW(), NOW());

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