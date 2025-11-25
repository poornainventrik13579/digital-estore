package com.inventrik.digitalestore.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class TenantRequest {

    @NotBlank(message = "Shop name is required")
    private String shopName;

    @NotBlank(message = "Shop email is required")
    @Email(message = "Invalid email format")
    private String shopEmail;

    private String shopPhone;
    private String shopLogo;
    private String domainName;
    private String subdomain;
    private String countryRegion;
    private String storePassword;
    private String baseCurrency;
    private Boolean multiCurrency;
    private String taxId;
    private String timezone;
}
