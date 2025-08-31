1.  **Tenants**

Master configuration table storing fundamental store information like
name, domain, contact details and operational status for each tenant

  --------------------------------------------------------------------------
  Field            Type           Description
  ---------------- -------------- ------------------------------------------
  Tenant_id        int            Unique identifier for each store/tenant in
                                  the multi-tenant system. Used as foreign
                                  key across all other tables to ensure data
                                  isolation

  Shop_name        varchar(100)   Store name displayed to customers and used
                                  in admin systems

  Shop_email       varchar(100)   Primary business email address for
                                  customer inquiries, order notifications,
                                  and official communications.

  Shop_phone       varchar(20)    Main business contact phone number
                                  displayed on website and used for customer
                                  support.

  Shop_logo        varchar(200)   Full URL path to the store\'s logo image
                                  file. Used in website header, emails and
                                  branding

  Domain_name      varchar(100)   Custom domain name (e.g., \"mystore.com\")
                                  for the store.

  Subdomain        varchar(50)    Subdomain identifier (e.g., \"mystore\" in
                                  \"mystore.platform.com\"). Used when
                                  custom domain is not available

  Country_region   varchar(100)   Primary geographic location where the
                                  business operates. Affects currency, tax
                                  calculations, and shipping defaults

  Store_password   varchar(250)   Encrypted password for password-protected
                                  stores. Used for private/wholesale stores
                                  or during development

  Base_currency    varchar(20)    Store\'s primary currency (USD, EUR, INR)

  Multi_currency   boolean        Enable multi-currency support

  Tax_id           Varchar(50)    Business tax identification number(GST,
                                  VAT number) for tax compliance and legal
                                  requirements

  Timezone         varchar(50)    Store\'s operational timezone(e.g.
                                  New_York)

  Status           varchar(2)     Store operational status: \"A\"
                                  (Active/Live), \"I\" (Inactive/Suspended)
  --------------------------------------------------------------------------

2.  **Store theme**

Visual customization settings including theme selection, branding
elements, promotional content, and legal page content.

  --------------------------------------------------------------------------
  Field            Type           Description
  ---------------- -------------- ------------------------------------------
  Tenant_id        int            Foreign key to Tenants table identifying
                                  which store this theme configuration
                                  belongs to

  Theme_id         int            Primary key - Unique identifier for this
                                  theme configuration record

  Theme_name       varchar(100)   Name of the selected theme template (e.g.,
                                  \"Modern\", \"Classic\", \"Minimal\").
                                  Used for theme switching

  Tagline          varchar(256)   Short promotional phrase displayed
                                  prominently on homepage (e.g., Quality
                                  Products)

  Description      varchar(256)   Brief store description for SEO meta tags,
                                  search results, and social media sharing
                                  previews

  Banner_image     varchar(256)   Full URL to homepage hero/banner
                                  background image. Should be
                                  high-resolution for visual impact

  Join_cta         varchar(256)   Call-to-action button text for user
                                  registration/signup (e.g., \"Learn More\",
                                  \"Sign Up\")

  Copyright_text   varchar(256)   Footer copyright notice (e.g., \"© 2025
                                  MyStore. All rights reserved.\") for legal
                                  protection and branding
  --------------------------------------------------------------------------

3.  **Pages**

Content management system for storing static pages with workflow
management, access control, and multi-language support for professional
page handling.

  ----------------------------------------------------------------------------
  Field              Type           Description
  ------------------ -------------- ------------------------------------------
  Id                 int            Primary key - Unique identifier for each
                                    page with auto-increment functionality

  Tenant_id          int            Foreign key to Tenants table identifying
                                    which store this page content belongs to.
                                    References tenants(id)

  Title              varchar(255)   Page title displayed in browser tab, page
                                    header, and navigation menus. Required
                                    field for content identification

  Slug               varchar(100)   URL-friendly identifier for the page
                                    (e.g., \"about-us\", \"contact-us\").
                                    Required and unique per tenant for SEO

  Content            longtext       Complete HTML content of the page
                                    including formatting, images, links and
                                    any embedded elements.

  Meta_title         varchar(256)   SEO meta title tag content for search
                                    engines. Appears in search results and
                                    browser tabs.

  Meta_description   varchar(256)   SEO meta description for search engine
                                    snippets. Brief summary for search results
                                    preview.

  Status             enum           Page workflow state with default
                                    \'draft\': \"draft\" (work in progress),
                                    \"published\" (live), \"archived\" (hidden
                                    but preserved)

  Visibility         enum           Access control with default \'public\':
                                    \"public\" (everyone), \"private\"
                                    (authenticated users), \"internal\"
                                    (admin)

  Created_at         timestamp      Exact date and time when page was
                                    initially created. Auto-set on insert with
                                    CURRENT_TIMESTAMP default

  Updated_at         timestamp      Date and time of last modification.
                                    Auto-updates on any change with ON UPDATE
                                    CURRENT_TIMESTAMP

  Published_at       timestamp      Exact timestamp when page was published.
                                    NULL for unpublished pages, set manually
                                    during publishing workflow

  Is_default         boolean        Marks system default template pages
                                    (About, Contact, Privacy, Terms) with
                                    default FALSE. Used for tenant setup

  Language           varchar(10)    Language code for multi-language support
                                    (e.g., \"en\", \"es\", \"fr\"). Defaults
                                    to \"en\" for English content
  ----------------------------------------------------------------------------

4.  **Tax**

Tax calculation rules and rates for different geographic regions with
custom structure and date validity periods.

+-------------+------------+------------------------------------------+
| Field       | Type       | Description                              |
+=============+============+==========================================+
| Id          | int        | Primary key - Unique identifier for each |
|             |            | tax configuration rule (composite key    |
|             |            | with tenant_id)                          |
+-------------+------------+------------------------------------------+
| Tenant_id   | int        | Foreign key to tenants table connecting  |
|             |            | tax settings to specific store           |
|             |            | (composite key with id)                  |
+-------------+------------+------------------------------------------+
| Code        | va         | Tax code identifier (e.g., \\\"GST\\\",  |
|             | rchar(255) | \\\"VAT\\\",                             |
|             |            |                                          |
|             |            | \\\"SALES_TAX\\\") for categorization    |
|             |            | and reference                            |
+-------------+------------+------------------------------------------+
| Description | va         | Human-readable description of the tax    |
|             | rchar(255) | rule for admin interface and reporting   |
|             |            | purposes                                 |
+-------------+------------+------------------------------------------+
| Value       | dec        | Tax rate or amount. For percentage-based |
|             | imal(10,2) |                                          |
|             |            | taxes, store as decimal (e.g., 18.00 for |
|             |            | 18%)                                     |
+-------------+------------+------------------------------------------+
| D           | varchar(2) | Indicates if this is the default tax     |
| efault_flag |            | rule for the tenant (e.g., \\\"Y\\\" for |
|             |            | Yes, \\\"N\\\" for No)                   |
+-------------+------------+------------------------------------------+
| Start_date  | date       | Tax rule effective start date. Tax       |
|             |            | applies from this date onwards for       |
|             |            | date-based validity                      |
+-------------+------------+------------------------------------------+
| End_date    | date       | Tax rule expiry date. Tax rule becomes   |
|             |            | inactive after this date. NULL for       |
|             |            | indefinite validity                      |
+-------------+------------+------------------------------------------+
| Modified    | datetime   | Timestamp of last modification for audit |
|             |            | trail and change tracking                |
+-------------+------------+------------------------------------------+
| Modified_by | va         | Username or identifier of user who last  |
|             | rchar(255) | modified this tax configuration for      |
|             |            | accountability                           |
+-------------+------------+------------------------------------------+
