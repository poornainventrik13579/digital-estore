-- V5__reviews_system.sql
-- Reviews system migration

CREATE TABLE reviews (
    tenant_id INT(10) UNSIGNED NOT NULL COMMENT 'Tenant ID',
    review_id VARCHAR(32) NOT NULL,
    product_id VARCHAR(32) NOT NULL,
    user_id VARCHAR(32) NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified BOOLEAN DEFAULT FALSE,
    status VARCHAR(2) NOT NULL DEFAULT '0', -- 0: ACTIVE, -1: INACTIVE
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, review_id),
    INDEX idx_reviews_product (tenant_id, product_id),
    INDEX idx_reviews_user (tenant_id, user_id),
    INDEX idx_reviews_rating (tenant_id, rating),
    INDEX idx_reviews_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 