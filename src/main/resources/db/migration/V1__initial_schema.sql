-- Create Payments table
CREATE TABLE Payments (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID',
    payment_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    currency VARCHAR(3),
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50),
    transaction_id VARCHAR(100),
    status VARCHAR(20) DEFAULT 'Pending',
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, payment_id)
);

-- Create Payment Audit Log table
CREATE TABLE payment_audit_log (
    audit_id VARCHAR(36) NOT NULL,
    payment_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_details TEXT,
    performed_by VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    PRIMARY KEY (audit_id)
);