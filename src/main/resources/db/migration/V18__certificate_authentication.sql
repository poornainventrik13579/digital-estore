CREATE TABLE user_certificates (
    id VARCHAR(255) PRIMARY KEY,
    tenant_id INT UNSIGNED NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    public_key TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'active',
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id, user_id) REFERENCES users(tenant_id, user_id) ON DELETE CASCADE,
    INDEX idx_tenant_user (tenant_id, user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
