package com.inventrik.digitalestore.domain.tenant;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
@Data
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tenant_id")
    private Integer tenantId;

    @Column(name = "shop_name", nullable = false, length = 100)
    private String shopName;

    @Column(name = "shop_email", nullable = false, length = 100)
    private String shopEmail;

    @Column(name = "shop_phone", length = 20)
    private String shopPhone;

    @Column(name = "shop_logo", length = 200)
    private String shopLogo;

    @Column(name = "domain_name", length = 100)
    private String domainName;

    @Column(name = "subdomain", length = 50)
    private String subdomain;

    @Column(name = "country_region", length = 100)
    private String countryRegion;

    @Column(name = "base_currency", length = 20)
    private String baseCurrency;

    @Column(name = "multi_currency")
    private Boolean multiCurrency;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(name = "timezone", length = 50)
    private String timezone;

    @Column(name = "status", nullable = false, length = 2)
    private String status;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;

    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        updated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
}
