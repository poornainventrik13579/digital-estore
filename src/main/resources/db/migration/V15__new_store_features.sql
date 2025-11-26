-- V15__new_store_features.sql
-- Add Tenant, StoreTheme, Page, and Tax tables

-- Create Tenants table
CREATE TABLE tenants (
    tenant_id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Tenant ID',
    shop_name VARCHAR(100) NOT NULL,
    shop_email VARCHAR(100) NOT NULL,
    shop_phone VARCHAR(20),
    shop_logo VARCHAR(200),
    domain_name VARCHAR(100),
    subdomain VARCHAR(50),
    country_region VARCHAR(100),
    store_password VARCHAR(250),
    base_currency VARCHAR(20),
    multi_currency TINYINT(1),
    tax_id VARCHAR(50),
    timezone VARCHAR(50),
    status VARCHAR(2) NOT NULL DEFAULT '0',
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id),
    UNIQUE KEY uk_shop_email (shop_email),
    UNIQUE KEY uk_subdomain (subdomain)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create StoreThemes table
CREATE TABLE store_themes (
    tenant_id INT(10) UNSIGNED NOT NULL COMMENT 'Tenant ID',
    theme_id INT(10) NOT NULL,
    theme_name VARCHAR(100),
    tagline VARCHAR(256),
    description VARCHAR(256),
    banner_image VARCHAR(256),
    join_cta VARCHAR(256),
    copyright_text VARCHAR(256),
    status VARCHAR(2) NOT NULL DEFAULT '0',
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, theme_id),
    INDEX idx_theme_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Pages table
CREATE TABLE pages (
    tenant_id INT(10) UNSIGNED NOT NULL COMMENT 'Tenant ID',
    page_id BIGINT(15) NOT NULL,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    content LONGTEXT,
    meta_title VARCHAR(256),
    meta_description VARCHAR(256),
    status VARCHAR(20) NOT NULL,
    visibility VARCHAR(20) NOT NULL,
    is_default TINYINT(1),
    language VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL,
    PRIMARY KEY (tenant_id, page_id),
    UNIQUE KEY uk_tenant_slug (tenant_id, slug),
    INDEX idx_page_status (tenant_id, status),
    INDEX idx_page_visibility (tenant_id, visibility)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Taxes table
CREATE TABLE taxes (
    tenant_id INT(10) UNSIGNED NOT NULL COMMENT 'Tenant ID',
    tax_id BIGINT(15) NOT NULL,
    code VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    value DECIMAL(10, 2) NOT NULL,
    default_flag VARCHAR(2),
    start_date DATE,
    end_date DATE,
    status VARCHAR(2) NOT NULL DEFAULT '0',
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, tax_id),
    UNIQUE KEY uk_tenant_code (tenant_id, code),
    INDEX idx_tax_status (tenant_id, status),
    INDEX idx_tax_default (tenant_id, default_flag),
    INDEX idx_tax_dates (tenant_id, start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
