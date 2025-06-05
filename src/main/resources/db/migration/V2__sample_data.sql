-- src/main/resources/db/migration/V2__sample_data.sql
-- Sample Users data
INSERT INTO Users (tenant_id, user_id, username, first_name, last_name, email, phone, user_type, otp, password_hash, status, created_by, updated_by)
VALUES
(1, 1000000001, 'john_doe', 'John', 'Doe', 'john.doe@example.com', '+1-555-0001', 'INDIVIDUAL', '123456', '$2a$10$dummy.hash.for.password123', '0', 'sy', 'sy'),
(1, 1000000002, 'jane_smith', 'Jane', 'Smith', 'jane.smith@example.com', '+1-555-0002', 'INDIVIDUAL', '234567', '$2a$10$dummy.hash.for.password456', '0', 'sy', 'sy'),
(1, 1000000003, 'acme_corp', 'Business', 'User', 'business@acme.com', '+1-555-0003', 'COMPANY', '345678', '$2a$10$dummy.hash.for.password789', '0', 'sy', 'sy');

-- Sample Categories data
INSERT INTO Categories (tenant_id, category_id, category_name, description, status, created_by, updated_by)
VALUES
(1, 1000000001, 'E-books', 'Digital books and publications', '0', 'sy', 'sy'),
(1, 1000000002, 'Software', 'Digital software and applications', '0', 'sy', 'sy'),
(1, 1000000003, 'Music', 'Digital music files and albums', '0', 'sy', 'sy'),
(1, 1000000004, 'Videos', 'Digital video content', '0', 'sy', 'sy');

-- Sample Products data
INSERT INTO Products (tenant_id, product_id, product_name, description, default_price, default_currency, category_id, status, created_by, updated_by)
VALUES
(1, 1000000001, 'Complete Java Guide', 'Comprehensive guide to Java programming', 29.99, 'USD', 1000000001, '0', 'sy', 'sy'),
(1, 1000000002, 'Photo Editor Pro', 'Professional photo editing software', 59.99, 'USD', 1000000002, '0', 'sy', 'sy'),
(1, 1000000003, 'Classical Music Collection', 'Collection of classical music pieces', 19.99, 'USD', 1000000003, '0', 'sy', 'sy'),
(1, 1000000004, 'Web Development Course', 'Complete web development video course', 99.99, 'USD', 1000000004, '0', 'sy', 'sy');

-- Sample Orders data
INSERT INTO Orders (tenant_id, order_id, user_id, currency, total_amount, exchange_rate, status, created_by, updated_by)
VALUES
(1, 1000000001, 1000000001, 'USD', 29.99, 1.00, 'Completed', 'sy', 'sy'),
(1, 1000000002, 1000000002, 'USD', 59.99, 1.00, 'Processing', 'sy', 'sy'),
(1, 1000000003, 1000000003, 'USD', 19.99, 1.00, 'Pending', 'sy', 'sy');

-- Sample OrderItems data
INSERT INTO OrderItems (tenant_id, order_id, order_item_id, product_id, price_at_purchase, license_key, status, created_by, updated_by)
VALUES
(1, 1000000001, 1000000001, 1000000001, 29.99, 'JAVA-GUIDE-2024-XYZ', '0', 'sy', 'sy'),
(1, 1000000002, 1000000002, 1000000002, 59.99, 'PHOTO-EDIT-PRO-ABC', '0', 'sy', 'sy'),
(1, 1000000003, 1000000003, 1000000003, 19.99, 'MUSIC-CLASSIC-DEF', '0', 'sy', 'sy');

-- Sample Payments data  
INSERT INTO Payments (tenant_id, payment_id, order_id, currency, amount, payment_method, transaction_id, status, created_by, updated_by)
VALUES
(1, 1000000001, 1000000001, 'USD', 29.99, 'Credit Card', 'stripe_pi_test123', 'Successful', 'sy', 'sy'),
(1, 1000000002, 1000000002, 'USD', 59.99, 'Credit Card', 'stripe_pi_test456', 'Processing', 'sy', 'sy'),
(1, 1000000003, 1000000003, 'USD', 19.99, 'Credit Card', 'stripe_pi_test789', 'Pending', 'sy', 'sy');