package com.inventrik.digitalestore.service.user;

import com.inventrik.digitalestore.dto.request.TenantUserLoginRequest;
import com.inventrik.digitalestore.dto.request.TenantUserSignupRequest;
import com.inventrik.digitalestore.dto.response.TenantUserAuthResponse;
import com.inventrik.digitalestore.dto.response.UserResponse;

public interface TenantUserAuthService {
    
    /**
     * Register a new user to a specific tenant store
     */
    TenantUserAuthResponse signup(String subdomain, TenantUserSignupRequest signupRequest);
    
    /**
     * Authenticate user for a specific tenant store
     */
    TenantUserAuthResponse login(String subdomain, TenantUserLoginRequest loginRequest);
    
    /**
     * Get current user profile with tenant context
     */
    UserResponse getCurrentUser(Integer tenantId, Long userId);
    
    /**
     * Check if username exists within tenant
     */
    boolean usernameExistsInTenant(String subdomain, String username);
    
    /**
     * Check if email exists within tenant  
     */
    boolean emailExistsInTenant(String subdomain, String email);
    
    /**
     * Validate if tenant exists and is active
     */
    boolean tenantExistsAndActive(String subdomain);
}
