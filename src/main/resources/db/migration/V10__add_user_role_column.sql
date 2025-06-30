-- Migration to add user_role column to users table
-- This adds proper role-based access control

-- Add user_role column with default value
ALTER TABLE users 
ADD COLUMN user_role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Update existing users - make first user with username 'admin' an ADMIN
UPDATE users 
SET user_role = 'ADMIN' 
WHERE username = 'admin' 
LIMIT 1;

-- Create index for performance
CREATE INDEX idx_users_role ON users(user_role);

-- Add check constraint to ensure valid roles
ALTER TABLE users 
ADD CONSTRAINT chk_user_role 
CHECK (user_role IN ('USER', 'ADMIN', 'MANAGER')); 