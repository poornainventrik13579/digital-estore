-- V007: Add soft-delete support to user_certificates

ALTER TABLE user_certificates
    ADD COLUMN status ENUM('ACTIVE', 'REVOKED', 'EXPIRED') NOT NULL DEFAULT 'ACTIVE';
