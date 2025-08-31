package com.inventrik.digitalestore.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantRequest {
    
    @NotBlank(message = "Shop name is required")
    @Size(max = 100, message = "Shop name must not exceed 100 characters")
    private String shopName;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Shop email is required")
    @Size(max = 100, message = "Shop email must not exceed 100 characters")
    private String shopEmail;
    
    @Size(max = 20, message = "Shop phone must not exceed 20 characters")
    private String shopPhone;
    
    @Size(max = 200, message = "Shop logo URL must not exceed 200 characters")
    private String shopLogo;
    
    @Size(max = 100, message = "Domain name must not exceed 100 characters")
    private String domainName;
    
    @Size(max = 50, message = "Subdomain must not exceed 50 characters")
    private String subdomain;
    
    @Size(max = 100, message = "Country region must not exceed 100 characters")
    private String countryRegion;
    
    @Size(max = 250, message = "Store password must not exceed 250 characters")
    private String storePassword;
    
    @Size(max = 20, message = "Base currency must not exceed 20 characters")
    private String baseCurrency;
    
    private Boolean multiCurrency;
    
    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;
    
    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;
    
    @NotBlank(message = "Status is required")
    @Size(max = 2, message = "Status must not exceed 2 characters")
    private String status;
}
