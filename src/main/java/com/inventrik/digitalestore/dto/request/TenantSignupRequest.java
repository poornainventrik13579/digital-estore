package com.inventrik.digitalestore.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TenantSignupRequest {
    
    @NotBlank(message = "Shop name is required")
    @Size(min = 2, max = 100, message = "Shop name must be between 2 and 100 characters")
    private String shopName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Valid email address is required")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String shopEmail;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 40, message = "Password must be between 8 and 40 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
             message = "Password must contain at least one lowercase letter, one uppercase letter, and one digit")
    private String password;
    
    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must be less than 20 characters")
    private String shopPhone;

    @NotBlank(message = "Shop logo is required")
    @Size(max = 200, message = "Logo URL must be less than 200 characters")
    private String shopLogo;

    @NotBlank(message = "Domain name is required")
    @Size(min = 4, max = 100, message = "Domain name must be between 4 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9.-]*[a-zA-Z0-9]\\.[a-zA-Z]{2,}$",
             message = "Domain name must be a valid format (e.g., mystore.example.com)")
    private String domainName;
    
    @NotBlank(message = "Subdomain is required")
    @Size(min = 3, max = 50, message = "Subdomain must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]$", 
             message = "Subdomain can only contain letters, numbers, and hyphens (not at start/end)")
    private String subdomain;
    
    @NotBlank(message = "Country/Region is required")
    @Size(max = 100, message = "Country/Region must be less than 100 characters")
    private String countryRegion;
    
    @NotBlank(message = "Base currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code (e.g., USD, EUR)")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter code in uppercase")
    private String baseCurrency;
    
    private Boolean multiCurrency = false;
    
    @Size(max = 50, message = "Tax ID must be less than 50 characters")
    private String taxId;

    @NotBlank(message = "Timezone is required")
    @Size(max = 50, message = "Timezone must be less than 50 characters")
    private String timezone;
}
