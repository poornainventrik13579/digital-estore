-- Create Store Themes table
CREATE TABLE store_themes (
    tenant_id INT NOT NULL,
    theme_id INT NOT NULL,
    theme_name VARCHAR(100),
    tagline VARCHAR(256),
    description VARCHAR(256),
    banner_image VARCHAR(256),
    join_cta VARCHAR(256),
    copyright_text VARCHAR(256),
    status VARCHAR(2) NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, theme_id)
);

-- Add foreign key constraint to tenants table
ALTER TABLE store_themes ADD CONSTRAINT fk_store_themes_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE;

-- Add indexes for performance
CREATE INDEX idx_store_themes_tenant_id ON store_themes(tenant_id);
CREATE INDEX idx_store_themes_theme_name ON store_themes(theme_name);
CREATE INDEX idx_store_themes_status ON store_themes(status);
CREATE INDEX idx_store_themes_tenant_status ON store_themes(tenant_id, status);

-- Add unique constraint for theme name per tenant
ALTER TABLE store_themes ADD CONSTRAINT uk_store_themes_tenant_name 
    UNIQUE (tenant_id, theme_name);

-- Insert sample store theme data
INSERT INTO store_themes (tenant_id, theme_id, theme_name, tagline, description, banner_image, join_cta, copyright_text, status, created_by, updated_by, created, updated)
VALUES
(1, 1001, 'Modern', 'Quality Digital Products at Your Fingertips', 'A modern and clean theme perfect for digital product stores', 'https://example.com/banners/modern-banner.jpg', 'Shop Now', '© 2024 Demo Store. All rights reserved.', 'A', 'system', 'system', NOW(), NOW()),
(1, 1002, 'Classic', 'Trusted Digital Solutions Since Day One', 'A classic and professional theme for established businesses', 'https://example.com/banners/classic-banner.jpg', 'Learn More', '© 2024 Demo Store. All rights reserved.', 'A', 'system', 'system', NOW(), NOW()),
(2, 2001, 'Minimal', 'Simple. Clean. Effective.', 'A minimalist theme focusing on product presentation', 'https://example.com/banners/minimal-banner.jpg', 'Get Started', '© 2024 Test Shop. All rights reserved.', 'A', 'system', 'system', NOW(), NOW());
