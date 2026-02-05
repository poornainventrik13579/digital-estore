-- Add hero_title column to store_themes if not exists
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
               AND TABLE_NAME = 'store_themes'
               AND COLUMN_NAME = 'hero_title');

SET @sql := IF(@exist = 0,
               'ALTER TABLE store_themes ADD COLUMN hero_title VARCHAR(256) NULL COMMENT ''Main hero section title displayed on homepage''',
               'SELECT ''Column hero_title already exists in store_themes''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add hero_description column to store_themes if not exists
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
               AND TABLE_NAME = 'store_themes'
               AND COLUMN_NAME = 'hero_description');

SET @sql := IF(@exist = 0,
               'ALTER TABLE store_themes ADD COLUMN hero_description TEXT NULL COMMENT ''Hero section description text displayed below title''',
               'SELECT ''Column hero_description already exists in store_themes''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add template column to pages if not exists
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
               AND TABLE_NAME = 'pages'
               AND COLUMN_NAME = 'template');

SET @sql := IF(@exist = 0,
               'ALTER TABLE pages ADD COLUMN template VARCHAR(50) NOT NULL DEFAULT ''default'' COMMENT ''Page layout template (default, about, contact, legal, etc.)''',
               'SELECT ''Column template already exists in pages''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;