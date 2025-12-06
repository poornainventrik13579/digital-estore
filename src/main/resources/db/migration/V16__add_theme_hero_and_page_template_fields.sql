ALTER TABLE store_themes
ADD COLUMN hero_title VARCHAR(256) NULL
COMMENT 'Main hero section title displayed on homepage';

ALTER TABLE store_themes
ADD COLUMN hero_description TEXT NULL
COMMENT 'Hero section description text displayed below title';

ALTER TABLE pages
ADD COLUMN template VARCHAR(50) NOT NULL DEFAULT 'default'
COMMENT 'Page layout template (default, about, contact, legal, etc.)';