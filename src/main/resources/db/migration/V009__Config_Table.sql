CREATE TABLE IF NOT EXISTS `config` (
    `param`       VARCHAR(255) NOT NULL,
    `tenant_id`   INT UNSIGNED NOT NULL,
    `modified`    DATETIME NULL DEFAULT NULL,
    `modified_by` VARCHAR(255) NULL DEFAULT NULL,
    `value`       VARCHAR(360) NULL DEFAULT NULL,
    PRIMARY KEY (`param`, `tenant_id`) USING BTREE
);

INSERT INTO `config` (`param`, `tenant_id`, `modified`, `modified_by`, `value`)
VALUES ('PAYMENT_PROVIDER', 1, NOW(), 'admin', 'STRIPE_REDIRECT');
