package com.inventrik.digitalestore.dto.response;

import com.inventrik.digitalestore.domain.user.UserRole;
import com.inventrik.digitalestore.domain.user.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantUserAuthResponse {
    
    private String accessToken;
    private String tokenType = "Bearer";
    private int expiresIn = 3600; // 1 hour in seconds
    
    // User information
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private UserRole userRole;
    private UserType userType;
    
    // Tenant information
    private Integer tenantId;
    private String shopName;
    private String subdomain;
    private String domainName;
    
    // Metadata
    private LocalDateTime loginTime;
    
    public TenantUserAuthResponse(String accessToken, Long userId, String username, String email,
                                String firstName, String lastName, UserRole userRole, UserType userType,
                                Integer tenantId, String shopName, String subdomain, String domainName) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = String.format("%s %s", firstName != null ? firstName : "", lastName != null ? lastName : "").trim();
        this.userRole = userRole;
        this.userType = userType;
        this.tenantId = tenantId;
        this.shopName = shopName;
        this.subdomain = subdomain;
        this.domainName = domainName;
        this.loginTime = LocalDateTime.now();
    }
}
