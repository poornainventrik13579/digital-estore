-- Add this to the existing V2__sample_data.sql file

-- Sample Digital Product Details
INSERT INTO DigitalProductDetails (tenant_id, product_id, file_url, file_size, file_format, license_info, version, download_limit, expiry_days, file_hash, status, created_by, updated_by)
VALUES
(1, 1000000001, '/files/ebook_guide.pdf', 2048576, 'PDF', 'Single user license - Personal use only', '1.0', 5, 30, 'sha256hash1', '0', 'sy', 'sy'),
(1, 1000000002, '/files/software_tool.zip', 15728640, 'ZIP', 'Single computer license', '2.1', 3, 365, 'sha256hash2', '0', 'sy', 'sy'),
(1, 1000000003, '/files/music_album.zip', 104857600, 'ZIP', 'Personal use license - No redistribution', '1.0', NULL, NULL, 'sha256hash3', '0', 'sy', 'sy');

-- Sample Digital Downloads (for demonstration)
INSERT INTO DigitalDownloads (tenant_id, download_id, order_item_id, download_date, ip_address, user_agent, download_token, token_expiry, file_size_downloaded, download_status, status, created_by, updated_by)
VALUES
(1, 2000000001, 1000000001, NOW(), '192.168.1.100', 'Mozilla/5.0', 'token123-456', DATE_ADD(NOW(), INTERVAL 24 HOUR), 2048576, 'COMPLETED', '0', 'sy', 'sy'),
(1, 2000000002, 1000000002, NOW(), '192.168.1.101', 'Mozilla/5.0', 'token789-012', DATE_ADD(NOW(), INTERVAL 24 HOUR), NULL, 'INITIATED', '0', 'sy', 'sy');