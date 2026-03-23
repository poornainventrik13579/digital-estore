ALTER TABLE order_items
    ADD COLUMN quantity INT NOT NULL DEFAULT 1 AFTER product_id;

ALTER TABLE orders
    ADD COLUMN tax_amount      DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER total_amount,
    ADD COLUMN discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER tax_amount;
