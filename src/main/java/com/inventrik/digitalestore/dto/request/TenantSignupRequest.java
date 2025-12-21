package com.inventrik.digitalestore.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TenantSignupRequest {
    @NotBlank
    private String shopName;

    @NotBlank(message = "Shop email is required")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Invalid email format")
    @Email
    @Size(max = 320)
    private String shopEmail;

    @NotBlank
    private String adminUsername;

    @NotBlank
    private String adminPassword;

    @NotBlank(message = "Admin email is required")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Invalid email format")
    @Email
    @Size(max = 320)
    private String adminEmail;

    @NotBlank(message = "Shop phone is required")
    @Pattern(regexp = "^\\+?[\\d\\s\\-()]+$", message = "Invalid phone format")
    @Size(max = 15)
    private String shopPhone;
  
  
    private String subdomain;
    private String countryRegion;
    private String baseCurrency = "USD";
}
