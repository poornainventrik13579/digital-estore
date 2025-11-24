package com.inventrik.digitalestore.service.tenant;

import com.inventrik.digitalestore.domain.tenant.Tenant;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubdomainResolverService {
    
    private final TenantRepository tenantRepository;
    
    public Integer resolveTenantId(String subdomain) {
        Tenant tenant = tenantRepository.findByDomainName(subdomain)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Store not found: '" + subdomain + "'. Please check the subdomain."));
        
        if (!"A".equals(tenant.getStatus())) {
            throw new ResourceNotFoundException(
                "Store '" + subdomain + "' is currently inactive.");
        }
        
        return tenant.getTenantId();
    }
    
    public Tenant resolveTenant(String subdomain) {
        Tenant tenant = tenantRepository.findByDomainName(subdomain)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Store not found: '" + subdomain + "'. Please check the subdomain."));
        
        if (!"A".equals(tenant.getStatus())) {
            throw new ResourceNotFoundException(
                "Store '" + subdomain + "' is currently inactive.");
        }
        
        return tenant;
    }
    
    public boolean isSubdomainAvailable(String subdomain) {
        try {
            resolveTenant(subdomain);
            return false;
        } catch (ResourceNotFoundException e) {
            return true;
        }
    }
    
    public boolean isValidSubdomainFormat(String subdomain) {
        if (subdomain == null || subdomain.trim().isEmpty()) {
            return false;
        }
        
        String pattern = "^[a-z0-9][a-z0-9-]{1,61}[a-z0-9]$";
        return subdomain.toLowerCase().matches(pattern);
    }
}
