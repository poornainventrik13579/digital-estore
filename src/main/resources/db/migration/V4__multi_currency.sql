CREATE TABLE currencies (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID',
    currency_code VARCHAR(3) NOT NULL,
    currency_name VARCHAR(50) NOT NULL,
    is_default VARCHAR(1) NOT NULL,
    exchange_rate DECIMAL(10,4) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    status VARCHAR(2) NOT NULL,
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, currency_code)
);

CREATE TABLE product_prices (
    tenant_id INT UNSIGNED NOT NULL COMMENT 'Tenant ID',
    product_id BIGINT NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(2) NOT NULL,
    created_by VARCHAR(2) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(2) NOT NULL,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, product_id, currency_code)
);

INSERT INTO currencies (tenant_id, currency_code, currency_name, is_default, exchange_rate, symbol, status, created_by, updated_by) VALUES
(1, 'USD', 'US Dollar', '1', 1.0000, '$', '0', '1', '1'),
(1, 'EUR', 'Euro', '0', 0.8500, '€', '0', '1', '1'),
(1, 'SGD', 'Singapore Dollar', '0', 1.3500, 'S$', '0', '1', '1'); 