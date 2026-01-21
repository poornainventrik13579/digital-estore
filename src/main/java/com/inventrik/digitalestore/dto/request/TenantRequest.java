package com.inventrik.digitalestore.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class TenantRequest {

    @NotBlank(message = "Shop name is required")
    private String shopName;

    @NotBlank(message = "Shop email is required")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Invalid email format")
    @Email(message = "Invalid email format")
    @Size(max = 320)
    private String shopEmail;
  
    @Pattern(regexp = "^\\+?[\\d\\s\\-()]+$", message = "Invalid phone format")
    @Size(max = 15)
    private String shopPhone;

    private String shopLogo;
    private String domainName;
    private String subdomain;
    private String countryRegion;
    private String baseCurrency;
    private Boolean multiCurrency;
    private String taxId;
    private String timezone;
}
