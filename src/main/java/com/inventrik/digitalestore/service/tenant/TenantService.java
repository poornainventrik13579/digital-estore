package com.inventrik.digitalestore.service.tenant;

import com.inventrik.digitalestore.dto.request.TenantRequest;
import com.inventrik.digitalestore.dto.request.TenantSignupRequest;
import com.inventrik.digitalestore.dto.response.TenantResponse;

import java.util.List;

public interface TenantService {
    List<TenantResponse> getAllTenants();
    TenantResponse getTenant(Integer tenantId);
    TenantResponse createTenant(TenantRequest request, String username);
    TenantResponse createTenantWithAdmin(TenantSignupRequest request);
    TenantResponse updateTenant(Integer tenantId, TenantRequest request, String username);
    void deleteTenant(Integer tenantId);
}
