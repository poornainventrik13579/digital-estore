-- Performance optimization indexes for multi-tenant queries
-- These indexes will significantly improve query performance for common operations

-- Products table indexes
CREATE INDEX idx_products_tenant_category ON products(tenant_id, category_id);
CREATE INDEX idx_products_tenant_status ON products(tenant_id, status);
CREATE INDEX idx_products_tenant_name ON products(tenant_id, product_name);

-- Orders table indexes  
CREATE INDEX idx_orders_tenant_user ON orders(tenant_id, user_id, order_date);
CREATE INDEX idx_orders_tenant_status ON orders(tenant_id, status, order_date);
CREATE INDEX idx_orders_tenant_date ON orders(tenant_id, order_date DESC);

-- Order Items table indexes
CREATE INDEX idx_order_items_tenant_order ON order_items(tenant_id, order_id);
CREATE INDEX idx_order_items_tenant_product ON order_items(tenant_id, product_id);

-- Payments table indexes
CREATE INDEX idx_payments_tenant_order ON payments(tenant_id, order_id);
CREATE INDEX idx_payments_tenant_status ON payments(tenant_id, status, payment_date);
CREATE INDEX idx_payments_tenant_date ON payments(tenant_id, payment_date DESC);

-- Users table indexes
CREATE INDEX idx_users_tenant_email ON users(tenant_id, email);
CREATE INDEX idx_users_tenant_phone ON users(tenant_id, phone);
CREATE INDEX idx_users_tenant_status ON users(tenant_id, status);

-- Categories table indexes
CREATE INDEX idx_categories_tenant_status ON categories(tenant_id, status);
CREATE INDEX idx_categories_tenant_name ON categories(tenant_id, category_name);

-- Digital Product Details table indexes
CREATE INDEX idx_digital_details_tenant_product ON digital_product_details(tenant_id, product_id);

-- Reviews table indexes (if exists)
-- CREATE INDEX idx_reviews_tenant_product ON reviews(tenant_id, product_id);
-- CREATE INDEX idx_reviews_tenant_user ON reviews(tenant_id, user_id);

-- Discount codes table indexes (if exists)  
-- CREATE INDEX idx_discount_codes_tenant_code ON discount_codes(tenant_id, code);
-- CREATE INDEX idx_discount_codes_tenant_status ON discount_codes(tenant_id, status); 