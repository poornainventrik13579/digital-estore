package com.inventrik.digitalestore.service.tenant;

import com.inventrik.digitalestore.dto.request.TenantRequest;
import com.inventrik.digitalestore.dto.request.TenantUpdateRequest;
import com.inventrik.digitalestore.dto.response.TenantResponse;

import java.util.List;

public interface TenantService {
    
    List<TenantResponse> getAllTenants();
    TenantResponse getTenant(Integer tenantId);
    TenantResponse createTenant(String username, TenantRequest tenantRequest);
    TenantResponse updateTenant(Integer tenantId, String username, TenantUpdateRequest updateRequest);
    void deleteTenant(Integer tenantId);
    TenantResponse getTenantByDomain(String domainName);
    TenantResponse getTenantBySubdomain(String subdomain);
    List<TenantResponse> getTenantsByStatus(String status);
    List<TenantResponse> getTenantsByCountry(String countryRegion);
    boolean existsByEmail(String shopEmail);
    boolean existsByDomain(String domainName);
    boolean existsBySubdomain(String subdomain);
}
