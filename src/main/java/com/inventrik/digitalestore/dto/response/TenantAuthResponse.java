package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantAuthResponse {
    
    private String accessToken;
    private String tokenType = "Bearer";
    private int expiresIn = 3600; // 1 hour in seconds
    private Integer tenantId;
    private String shopName;
    private String shopEmail;
    private String subdomain;
    private String domainName;
    private LocalDateTime loginTime;
    
    public TenantAuthResponse(String accessToken, Integer tenantId, String shopName, 
                            String shopEmail, String subdomain, String domainName) {
        this.accessToken = accessToken;
        this.tenantId = tenantId;
        this.shopName = shopName;
        this.shopEmail = shopEmail;
        this.subdomain = subdomain;
        this.domainName = domainName;
        this.loginTime = LocalDateTime.now();
    }
}
