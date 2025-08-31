-- Create Tenants table
CREATE TABLE tenants (
    tenant_id INT NOT NULL,
    shop_name VARCHAR(100),
    shop_email VARCHAR(100),
    shop_phone VARCHAR(20),
    shop_logo VARCHAR(200),
    domain_name VARCHAR(100),
    subdomain VARCHAR(50),
    country_region VARCHAR(100),
    store_password VARCHAR(250),
    base_currency VARCHAR(20),
    multi_currency BOOLEAN DEFAULT FALSE,
    tax_id VARCHAR(50),
    timezone VARCHAR(50),
    status VARCHAR(2) NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id)
);

-- Add indexes for performance
CREATE INDEX idx_tenants_shop_email ON tenants(shop_email);
CREATE INDEX idx_tenants_domain_name ON tenants(domain_name);
CREATE INDEX idx_tenants_subdomain ON tenants(subdomain);
CREATE INDEX idx_tenants_status ON tenants(status);
CREATE INDEX idx_tenants_country_region ON tenants(country_region);

-- Add unique constraints
ALTER TABLE tenants ADD CONSTRAINT uk_tenants_shop_email UNIQUE (shop_email);
ALTER TABLE tenants ADD CONSTRAINT uk_tenants_domain_name UNIQUE (domain_name);
ALTER TABLE tenants ADD CONSTRAINT uk_tenants_subdomain UNIQUE (subdomain);

-- Insert sample tenant data
INSERT INTO tenants (tenant_id, shop_name, shop_email, shop_phone, shop_logo, domain_name, subdomain, country_region, base_currency, multi_currency, tax_id, timezone, status, created_by, updated_by, created, updated)
VALUES
(1, 'Demo Store', 'demo@example.com', '+1-555-0100', 'https://example.com/logo.png', 'demo.example.com', 'demo', 'United States', NULL, 'USD', TRUE, 'TX123456789', 'America/New_York', 'A', 'system', 'system', NOW(), NOW()),
(2, 'Test Shop', 'test@example.com', '+1-555-0200', 'https://example.com/test-logo.png', 'test.example.com', 'test', 'Canada', NULL, 'CAD', FALSE, 'CA987654321', 'America/Toronto', 'A', 'system', 'system', NOW(), NOW());
