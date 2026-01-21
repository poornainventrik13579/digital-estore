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
    @Size(max = 100)
    private String shopEmail;

    @NotBlank(message = "Shop phone is required")
    @Pattern(regexp = "^\\+?[\\d\\s\\-()]+$", message = "Invalid phone format")
    @Size(max = 15)
    private String shopPhone;

    @NotBlank(message = "Shop logo URL is required")
    @Size(max = 200)
    @Pattern(regexp = "^https?://.+", message = "Logo must be a valid URL starting with http:// or https://")
    private String shopLogo;

    @NotBlank(message = "Domain name is required")
    @Size(max = 100)
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]\\.[a-zA-Z]{2,}$", message = "Invalid domain format (e.g., store.com)")
    private String domainName;

    @NotBlank
    private String adminUsername;

    @NotBlank
    private String adminPassword;

    @NotBlank(message = "Admin email is required")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Invalid email format")
    @Email
    @Size(max = 320)
    private String adminEmail;

    @Size(max = 50)
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Subdomain can only contain lowercase letters, numbers, and hyphens")
    private String subdomain;

    private String countryRegion;

    private String baseCurrency = "USD";

    @Size(max = 50)
    private String timezone;

    @Size(max = 50)
    @Pattern(regexp = "^[A-Z0-9-]{8,20}$", message = "Tax ID must be 8-20 characters, uppercase letters and numbers only")
    private String taxId;
}
