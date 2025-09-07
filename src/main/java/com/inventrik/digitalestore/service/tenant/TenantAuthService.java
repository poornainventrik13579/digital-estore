package com.inventrik.digitalestore.service.tenant;

import com.inventrik.digitalestore.dto.request.TenantLoginRequest;
import com.inventrik.digitalestore.dto.request.TenantSignupRequest;
import com.inventrik.digitalestore.dto.response.TenantAuthResponse;

public interface TenantAuthService {
    
    TenantAuthResponse signup(TenantSignupRequest signupRequest);
    
    TenantAuthResponse login(TenantLoginRequest loginRequest);
    
    boolean emailExists(String email);
    
    boolean subdomainExists(String subdomain);
    
    boolean domainExists(String domainName);
}
