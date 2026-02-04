CREATE TABLE user_certificates (
    session_id     VARCHAR(32)   NOT NULL PRIMARY KEY,
    tenant_id      INT UNSIGNED  NOT NULL,
    user_id        VARCHAR(32)   NOT NULL,
    public_key     TEXT          NOT NULL,
    created        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id, user_id) REFERENCES users(tenant_id, user_id) ON DELETE CASCADE,
    INDEX idx_tenant_user (tenant_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
