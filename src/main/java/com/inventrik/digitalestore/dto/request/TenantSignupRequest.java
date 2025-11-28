package com.inventrik.digitalestore.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantSignupRequest {
    @NotBlank
    private String shopName;

    @NotBlank
    @Email
    private String shopEmail;

    @NotBlank
    private String adminUsername;

    @NotBlank
    private String adminPassword;

    @NotBlank
    private String adminEmail;

    @NotBlank
    private String shopPhone;
    private String subdomain;
    private String countryRegion;
    private String baseCurrency = "USD";
}
