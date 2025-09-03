package com.inventrik.digitalestore.service.tenant;

import com.inventrik.digitalestore.dto.request.TenantLoginRequest;
import com.inventrik.digitalestore.dto.request.TenantSignupRequest;
import com.inventrik.digitalestore.dto.response.TenantAuthResponse;

public interface TenantAuthService {
    
    /**
     * Register a new tenant and return authentication token
     */
    TenantAuthResponse signup(TenantSignupRequest signupRequest);
    
    /**
     * Authenticate tenant and return JWT token
     */
    TenantAuthResponse login(TenantLoginRequest loginRequest);
    
    /**
     * Validate if email already exists
     */
    boolean emailExists(String email);
    
    /**
     * Validate if subdomain already exists
     */
    boolean subdomainExists(String subdomain);
    
    /**
     * Validate if domain name already exists
     */
    boolean domainExists(String domainName);
}
