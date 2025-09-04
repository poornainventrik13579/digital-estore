package com.inventrik.digitalestore.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class TenantAccessValidator {
    
    /**
     * Extract tenant ID from JWT token
     */
    public Integer extractTenantIdFromJwt(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return null;
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("tenant_id");
    }
    
    /**
     * Verify that the tenant ID in the JWT matches the requested tenant ID
     */
    public boolean verifyTenantAccess(Authentication authentication, Integer requestedTenantId) {
        Integer tokenTenantId = extractTenantIdFromJwt(authentication);
        return tokenTenantId != null && tokenTenantId.equals(requestedTenantId);
    }
    
    /**
     * Check if current user has system admin privileges
     */
    public boolean isSystemAdmin(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return false;
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userRole = jwt.getClaim("user_role");
        return "SYSTEM_ADMIN".equals(userRole);
    }
    
    /**
     * Check if current user has tenant admin privileges for the requested tenant
     */
    public boolean isTenantAdmin(Authentication authentication, Integer requestedTenantId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return false;
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userRole = jwt.getClaim("user_role");
        return "TENANT_ADMIN".equals(userRole) && verifyTenantAccess(authentication, requestedTenantId);
    }
}
