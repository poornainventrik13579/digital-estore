package com.inventrik.digitalestore.service.user;

import com.inventrik.digitalestore.dto.request.TenantUserLoginRequest;
import com.inventrik.digitalestore.dto.request.TenantUserSignupRequest;
import com.inventrik.digitalestore.dto.response.TenantUserAuthResponse;
import com.inventrik.digitalestore.dto.response.UserResponse;

public interface TenantUserAuthService {
    
    TenantUserAuthResponse signup(Integer tenantId, TenantUserSignupRequest signupRequest);
    
    TenantUserAuthResponse login(Integer tenantId, TenantUserLoginRequest loginRequest);
    
    UserResponse getCurrentUser(Integer tenantId, Long userId);
    
    boolean usernameExistsInTenant(Integer tenantId, String username);
    
    boolean emailExistsInTenant(Integer tenantId, String email);
    
    boolean tenantExistsAndActive(Integer tenantId);
}
