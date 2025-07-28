-- Add image columns and metadata to products table
ALTER TABLE products 
ADD COLUMN image1_url VARCHAR(256),
ADD COLUMN image2_url VARCHAR(256),
ADD COLUMN image3_url VARCHAR(256),
ADD COLUMN image4_url VARCHAR(256),
ADD COLUMN image5_url VARCHAR(256),
ADD COLUMN banner VARCHAR(256),
ADD COLUMN thumbnail VARCHAR(256),
ADD COLUMN metadata TEXT; 