-- Remove unused store_password column from tenants table
-- Admin authentication is handled via users table with BCrypt password hashing
ALTER TABLE tenants DROP COLUMN store_password;

-- Add unique index for domain_name to prevent duplicates
ALTER TABLE tenants ADD UNIQUE KEY uk_domain_name (domain_name);
