-- Create Taxes table
CREATE TABLE taxes (
    id INT NOT NULL,
    tenant_id INT NOT NULL,
    code VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    value DECIMAL(10,2) NOT NULL,
    default_flag VARCHAR(2) NOT NULL DEFAULT 'N',
    start_date DATE NOT NULL,
    end_date DATE NULL,
    modified DATETIME NOT NULL,
    modified_by VARCHAR(255) NOT NULL,
    status VARCHAR(2) NOT NULL DEFAULT 'A',
    created_by VARCHAR(50) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id, tenant_id)
);

-- Add foreign key constraint to tenants table
ALTER TABLE taxes ADD CONSTRAINT fk_taxes_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE;

-- Add indexes for performance
CREATE INDEX idx_taxes_tenant_id ON taxes(tenant_id);
CREATE INDEX idx_taxes_code ON taxes(code);
CREATE INDEX idx_taxes_status ON taxes(status);
CREATE INDEX idx_taxes_default_flag ON taxes(default_flag);
CREATE INDEX idx_taxes_start_date ON taxes(start_date);
CREATE INDEX idx_taxes_end_date ON taxes(end_date);
CREATE INDEX idx_taxes_tenant_code ON taxes(tenant_id, code);
CREATE INDEX idx_taxes_tenant_status ON taxes(tenant_id, status);
CREATE INDEX idx_taxes_tenant_default ON taxes(tenant_id, default_flag);
CREATE INDEX idx_taxes_date_range ON taxes(start_date, end_date);

-- Add unique constraints
ALTER TABLE taxes ADD CONSTRAINT uk_taxes_tenant_code UNIQUE (tenant_id, code);

-- Add check constraints
ALTER TABLE taxes ADD CONSTRAINT chk_taxes_default_flag CHECK (default_flag IN ('Y', 'N'));
ALTER TABLE taxes ADD CONSTRAINT chk_taxes_status CHECK (status IN ('A', 'I'));
ALTER TABLE taxes ADD CONSTRAINT chk_taxes_value CHECK (value >= 0 AND value <= 100);
ALTER TABLE taxes ADD CONSTRAINT chk_taxes_dates CHECK (end_date IS NULL OR end_date > start_date);

-- Insert sample tax data
INSERT INTO taxes (id, tenant_id, code, description, value, default_flag, start_date, end_date, modified, modified_by, status, created_by, updated_by, created, updated)
VALUES
-- Demo Store (tenant_id = 1) taxes
(1001, 1, 'GST_18', 'Goods and Services Tax - 18%', 18.00, 'Y', '2024-01-01', NULL, NOW(), 'system', 'A', 'system', 'system', NOW(), NOW()),
(1002, 1, 'GST_12', 'Goods and Services Tax - 12%', 12.00, 'N', '2024-01-01', NULL, NOW(), 'system', 'A', 'system', 'system', NOW(), NOW()),
(1003, 1, 'GST_5', 'Goods and Services Tax - 5%', 5.00, 'N', '2024-01-01', NULL, NOW(), 'system', 'A', 'system', 'system', NOW(), NOW()),
(1004, 1, 'SERVICE_TAX', 'Service Tax for Digital Products', 15.00, 'N', '2024-01-01', '2024-12-31', NOW(), 'system', 'A', 'system', 'system', NOW(), NOW()),

-- Test Shop (tenant_id = 2) taxes
(2001, 2, 'VAT_20', 'Value Added Tax - 20%', 20.00, 'Y', '2024-01-01', NULL, NOW(), 'system', 'A', 'system', 'system', NOW(), NOW()),
(2002, 2, 'VAT_13', 'Value Added Tax - 13%', 13.00, 'N', '2024-01-01', NULL, NOW(), 'system', 'A', 'system', 'system', NOW(), NOW()),
(2003, 2, 'HST_15', 'Harmonized Sales Tax - 15%', 15.00, 'N', '2024-01-01', NULL, NOW(), 'system', 'A', 'system', 'system', NOW(), NOW()),
(2004, 2, 'DIGITAL_TAX', 'Digital Services Tax', 10.00, 'N', '2024-06-01', '2024-12-31', NOW(), 'system', 'A', 'system', 'system', NOW(), NOW());
