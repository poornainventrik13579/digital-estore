package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {
    
    private Integer tenantId;
    private String shopName;
    private String shopEmail;
    private String shopPhone;
    private String shopLogo;
    private String domainName;
    private String subdomain;
    private String countryRegion;
    private String baseCurrency;
    private Boolean multiCurrency;
    private String taxId;
    private String timezone;
    private String status;
    private String createdBy;
    private LocalDateTime created;
    private String updatedBy;
    private LocalDateTime updated;
}
