ALTER TABLE users
ADD COLUMN user_role ENUM('USER', 'ADMIN', 'MANAGER') NOT NULL DEFAULT 'USER'
COMMENT 'User role for access control';
UPDATE users
SET user_role = 'ADMIN'
WHERE username = 'admin'
LIMIT 1;
CREATE INDEX idx_users_role ON users(user_role);
CREATE TABLE IF NOT EXISTS user_certificates (
    session_id     VARCHAR(255)  NOT NULL PRIMARY KEY COMMENT 'Session identifier (FIXED: was VARCHAR(32))',
    tenant_id      INT UNSIGNED  NOT NULL COMMENT 'Tenant ID for multi-tenancy',
    user_id        VARCHAR(32)   NOT NULL COMMENT 'Associated user ID',
    public_key     VARCHAR(124)   NOT NULL COMMENT 'ECDSA public key (FIXED: was TEXT)',
    created        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Certificate creation timestamp',
    updated        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    FOREIGN KEY (tenant_id, user_id) REFERENCES users(tenant_id, user_id) ON DELETE CASCADE,
    INDEX idx_tenant_user (tenant_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Certificate authentication session data';
