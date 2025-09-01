-- Create Pages table
CREATE TABLE pages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    content LONGTEXT,
    meta_title VARCHAR(256),
    meta_description VARCHAR(256),
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    visibility ENUM('PUBLIC', 'PRIVATE', 'INTERNAL') NOT NULL DEFAULT 'PUBLIC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

-- Add foreign key constraint to tenants table
ALTER TABLE pages ADD CONSTRAINT fk_pages_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE;

-- Add indexes for performance
CREATE INDEX idx_pages_tenant_id ON pages(tenant_id);
CREATE INDEX idx_pages_slug ON pages(slug);
CREATE INDEX idx_pages_status ON pages(status);
CREATE INDEX idx_pages_visibility ON pages(visibility);
CREATE INDEX idx_pages_language ON pages(language);
CREATE INDEX idx_pages_is_default ON pages(is_default);
CREATE INDEX idx_pages_tenant_slug ON pages(tenant_id, slug);
CREATE INDEX idx_pages_tenant_status ON pages(tenant_id, status);
CREATE INDEX idx_pages_tenant_visibility ON pages(tenant_id, visibility);
CREATE INDEX idx_pages_tenant_language ON pages(tenant_id, language);

-- Add unique constraints
ALTER TABLE pages ADD CONSTRAINT uk_pages_tenant_slug UNIQUE (tenant_id, slug);

-- Create full-text index for content search
ALTER TABLE pages ADD FULLTEXT(title, content);

-- Insert sample page data
INSERT INTO pages (tenant_id, title, slug, content, meta_title, meta_description, status, visibility, is_default, language, created_by, updated_by, created_at, updated_at)
VALUES
-- Demo Store (tenant_id = 1) pages
(1, 'About Us', 'about-us', 
'<h1>About Demo Store</h1>
<p>Welcome to Demo Store, your trusted partner for digital products and solutions. We have been serving customers worldwide with high-quality digital content since our inception.</p>
<h2>Our Mission</h2>
<p>To provide exceptional digital products that enhance productivity and creativity for individuals and businesses alike.</p>', 
'About Demo Store - Learn More About Us', 
'Discover the story behind Demo Store and our commitment to providing quality digital products and exceptional customer service.', 
'PUBLISHED', 'PUBLIC', TRUE, 'en', 'system', 'system', NOW(), NOW()),

(1, 'Contact Us', 'contact-us',
'<h1>Contact Demo Store</h1>
<p>We would love to hear from you! Get in touch with our team for any questions, support, or feedback.</p>
<h2>Contact Information</h2>
<ul>
<li>Email: demo@example.com</li>
<li>Phone: +1-555-0100</li>
<li>Address: 123 Digital Street, Tech City, TC 12345</li>
</ul>
<h2>Business Hours</h2>
<p>Monday - Friday: 9:00 AM - 6:00 PM EST<br>
Saturday - Sunday: 10:00 AM - 4:00 PM EST</p>',
'Contact Demo Store - Get in Touch', 
'Contact Demo Store for support, questions, or feedback. Find our email, phone, and business hours information.',
'PUBLISHED', 'PUBLIC', TRUE, 'en', 'system', 'system', NOW(), NOW()),

(1, 'Privacy Policy', 'privacy-policy',
'<h1>Privacy Policy</h1>
<p>Last updated: [Date]</p>
<h2>Information We Collect</h2>
<p>We collect information you provide directly to us, such as when you create an account, make a purchase, or contact us for support.</p>
<h2>How We Use Your Information</h2>
<p>We use the information we collect to provide, maintain, and improve our services, process transactions, and communicate with you.</p>
<h2>Information Sharing</h2>
<p>We do not sell, trade, or otherwise transfer your personal information to third parties without your consent, except as described in this policy.</p>',
'Privacy Policy - Demo Store',
'Learn about how Demo Store collects, uses, and protects your personal information. Read our comprehensive privacy policy.',
'PUBLISHED', 'PUBLIC', TRUE, 'en', 'system', 'system', NOW(), NOW()),

(1, 'Terms of Service', 'terms-of-service',
'<h1>Terms of Service</h1>
<p>Last updated: [Date]</p>
<h2>Acceptance of Terms</h2>
<p>By accessing and using Demo Store, you accept and agree to be bound by the terms and provision of this agreement.</p>
<h2>Use License</h2>
<p>Permission is granted to temporarily download one copy of the materials on Demo Store for personal, non-commercial transitory viewing only.</p>
<h2>Disclaimer</h2>
<p>The materials on Demo Store are provided on an "as is" basis. Demo Store makes no warranties, expressed or implied.</p>',
'Terms of Service - Demo Store',
'Read the terms and conditions for using Demo Store services and purchasing digital products.',
'PUBLISHED', 'PUBLIC', TRUE, 'en', 'system', 'system', NOW(), NOW()),

-- Test Shop (tenant_id = 2) pages
(2, 'About Test Shop', 'about-us',
'<h1>About Test Shop</h1>
<p>Test Shop is a modern digital marketplace focused on providing innovative solutions for creative professionals and businesses.</p>
<h2>What We Offer</h2>
<p>We specialize in digital tools, templates, and resources that help our customers achieve their goals more efficiently.</p>',
'About Test Shop - Digital Solutions', 
'Learn about Test Shop and our mission to provide innovative digital solutions for creative professionals.',
'PUBLISHED', 'PUBLIC', TRUE, 'en', 'system', 'system', NOW(), NOW()),

(2, 'Contact', 'contact-us',
'<h1>Get in Touch</h1>
<p>Have questions? We are here to help!</p>
<h2>Contact Details</h2>
<ul>
<li>Email: test@example.com</li>
<li>Phone: +1-555-0200</li>
</ul>',
'Contact Test Shop - We Are Here to Help',
'Contact Test Shop for any questions or support. Find our contact information and get in touch with our team.',
'PUBLISHED', 'PUBLIC', TRUE, 'en', 'system', 'system', NOW(), NOW());
