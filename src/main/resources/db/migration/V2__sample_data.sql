-- Sample Digital Product Details (only fields from design brief)
INSERT INTO DigitalProductDetails (tenant_id, product_id, file_url, file_size, file_format, license_info, version, status, created_by, updated_by)
VALUES
(1, 1000000001, '/files/ebook_guide.pdf', 2048, 'PDF', 'Single user license - Personal use only', '1.0', '0', 'sy', 'sy'),
(1, 1000000002, '/files/software_tool.zip', 15360, 'ZIP', 'Single computer license', '2.1', '0', 'sy', 'sy'),
(1, 1000000003, '/files/music_album.zip', 102400, 'ZIP', 'Personal use license - No redistribution', '1.0', '0', 'sy', 'sy');

-- Sample Digital Downloads (only fields from design brief)
INSERT INTO DigitalDownloads (tenant_id, download_id, order_item_id, download_date, ip_address, status, created_by, updated_by)
VALUES
(1, 2000000001, 1000000001, NOW(), '192.168.1.100', '0', 'sy', 'sy'),
(1, 2000000002, 1000000002, NOW(), '192.168.1.101', '0', 'sy', 'sy');