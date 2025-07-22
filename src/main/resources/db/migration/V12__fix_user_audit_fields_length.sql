-- Update audit field values to use consistent 'system' username
-- The schema changes were already applied, this migration just cleans up data

-- Update any existing short values to 'system' for consistency
UPDATE users SET created_by = 'system' WHERE created_by IN ('sy', '0', '1') AND LENGTH(created_by) <= 2;
UPDATE users SET updated_by = 'system' WHERE updated_by IN ('sy', '0', '1') AND LENGTH(updated_by) <= 2;

UPDATE products SET created_by = 'system' WHERE created_by IN ('sy', '0', '1') AND LENGTH(created_by) <= 2;
UPDATE products SET updated_by = 'system' WHERE updated_by IN ('sy', '0', '1') AND LENGTH(updated_by) <= 2;

UPDATE categories SET created_by = 'system' WHERE created_by IN ('sy', '0', '1') AND LENGTH(created_by) <= 2;
UPDATE categories SET updated_by = 'system' WHERE updated_by IN ('sy', '0', '1') AND LENGTH(updated_by) <= 2;

UPDATE orders SET created_by = 'system' WHERE created_by IN ('sy', '0', '1') AND LENGTH(created_by) <= 2;
UPDATE orders SET updated_by = 'system' WHERE updated_by IN ('sy', '0', '1') AND LENGTH(updated_by) <= 2;

UPDATE payments SET created_by = 'system' WHERE created_by IN ('sy', '0', '1') AND LENGTH(created_by) <= 2;
UPDATE payments SET updated_by = 'system' WHERE updated_by IN ('sy', '0', '1') AND LENGTH(updated_by) <= 2;

UPDATE reviews SET created_by = 'system' WHERE created_by IN ('sy', '0', '1') AND LENGTH(created_by) <= 2;
UPDATE reviews SET updated_by = 'system' WHERE updated_by IN ('sy', '0', '1') AND LENGTH(updated_by) <= 2;

-- Update other tables that exist (using the actual table names from SHOW TABLES)
UPDATE order_items SET created_by = 'system' WHERE created_by IN ('sy', '0', '1') AND LENGTH(created_by) <= 2;
UPDATE order_items SET updated_by = 'system' WHERE updated_by IN ('sy', '0', '1') AND LENGTH(updated_by) <= 2;

UPDATE digital_downloads SET created_by = 'system' WHERE created_by IN ('sy', '0', '1') AND LENGTH(created_by) <= 2;
UPDATE digital_downloads SET updated_by = 'system' WHERE updated_by IN ('sy', '0', '1') AND LENGTH(updated_by) <= 2;

UPDATE digital_product_details SET created_by = 'system' WHERE created_by IN ('sy', '0', '1') AND LENGTH(created_by) <= 2;
UPDATE digital_product_details SET updated_by = 'system' WHERE updated_by IN ('sy', '0', '1') AND LENGTH(updated_by) <= 2;

UPDATE productbundles SET created_by = 'system' WHERE created_by IN ('sy', '0', '1') AND LENGTH(created_by) <= 2;
UPDATE productbundles SET updated_by = 'system' WHERE updated_by IN ('sy', '0', '1') AND LENGTH(updated_by) <= 2;

UPDATE bundleitems SET created_by = 'system' WHERE created_by IN ('sy', '0', '1') AND LENGTH(created_by) <= 2;
UPDATE bundleitems SET updated_by = 'system' WHERE updated_by IN ('sy', '0', '1') AND LENGTH(updated_by) <= 2;

UPDATE discount_codes SET created_by = 'system' WHERE created_by IN ('sy', '0', '1') AND LENGTH(created_by) <= 2;
UPDATE discount_codes SET updated_by = 'system' WHERE updated_by IN ('sy', '0', '1') AND LENGTH(updated_by) <= 2;

UPDATE discount_usage SET created_by = 'system' WHERE created_by IN ('sy', '0', '1') AND LENGTH(created_by) <= 2;
UPDATE discount_usage SET updated_by = 'system' WHERE updated_by IN ('sy', '0', '1') AND LENGTH(updated_by) <= 2; 