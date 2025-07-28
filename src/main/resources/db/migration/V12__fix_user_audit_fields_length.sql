-- First increase column lengths for all audit fields to accommodate 'system' value
ALTER TABLE users MODIFY COLUMN created_by VARCHAR(50), MODIFY COLUMN updated_by VARCHAR(50);
ALTER TABLE categories MODIFY COLUMN created_by VARCHAR(50), MODIFY COLUMN updated_by VARCHAR(50);
ALTER TABLE products MODIFY COLUMN created_by VARCHAR(50), MODIFY COLUMN updated_by VARCHAR(50);
ALTER TABLE orders MODIFY COLUMN created_by VARCHAR(50), MODIFY COLUMN updated_by VARCHAR(50);
ALTER TABLE payments MODIFY COLUMN created_by VARCHAR(50), MODIFY COLUMN updated_by VARCHAR(50);
ALTER TABLE reviews MODIFY COLUMN created_by VARCHAR(50), MODIFY COLUMN updated_by VARCHAR(50);
ALTER TABLE order_items MODIFY COLUMN created_by VARCHAR(50), MODIFY COLUMN updated_by VARCHAR(50);
ALTER TABLE digital_downloads MODIFY COLUMN created_by VARCHAR(50), MODIFY COLUMN updated_by VARCHAR(50);
ALTER TABLE digital_product_details MODIFY COLUMN created_by VARCHAR(50), MODIFY COLUMN updated_by VARCHAR(50);
ALTER TABLE productbundles MODIFY COLUMN created_by VARCHAR(50), MODIFY COLUMN updated_by VARCHAR(50);
ALTER TABLE bundleitems MODIFY COLUMN created_by VARCHAR(50), MODIFY COLUMN updated_by VARCHAR(50);
ALTER TABLE discount_codes MODIFY COLUMN created_by VARCHAR(50), MODIFY COLUMN updated_by VARCHAR(50);
ALTER TABLE discount_usage MODIFY COLUMN created_by VARCHAR(50), MODIFY COLUMN updated_by VARCHAR(50);
ALTER TABLE currencies MODIFY COLUMN created_by VARCHAR(50), MODIFY COLUMN updated_by VARCHAR(50);
ALTER TABLE product_prices MODIFY COLUMN created_by VARCHAR(50), MODIFY COLUMN updated_by VARCHAR(50);

-- Now update audit field values to use consistent 'system' username
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

UPDATE currencies SET created_by = 'system' WHERE created_by IN ('sy', '0', '1') AND LENGTH(created_by) <= 2;
UPDATE currencies SET updated_by = 'system' WHERE updated_by IN ('sy', '0', '1') AND LENGTH(updated_by) <= 2;

UPDATE product_prices SET created_by = 'system' WHERE created_by IN ('sy', '0', '1') AND LENGTH(created_by) <= 2;
UPDATE product_prices SET updated_by = 'system' WHERE updated_by IN ('sy', '0', '1') AND LENGTH(updated_by) <= 2; 