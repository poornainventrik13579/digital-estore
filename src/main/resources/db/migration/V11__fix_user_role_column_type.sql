-- Migration to fix user_role column type to ENUM
-- This aligns the database schema with Hibernate expectations

-- Drop the existing check constraint first
ALTER TABLE users DROP CONSTRAINT chk_user_role;

-- Modify the column to be ENUM type
ALTER TABLE users 
MODIFY COLUMN user_role ENUM('USER', 'ADMIN', 'MANAGER') NOT NULL DEFAULT 'USER'; 