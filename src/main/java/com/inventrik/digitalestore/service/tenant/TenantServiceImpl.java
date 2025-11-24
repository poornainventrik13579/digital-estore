package com.inventrik.digitalestore.service.tenant;

import com.inventrik.digitalestore.domain.tenant.Tenant;
import com.inventrik.digitalestore.dto.request.TenantRequest;
import com.inventrik.digitalestore.dto.request.TenantUpdateRequest;
import com.inventrik.digitalestore.dto.response.TenantResponse;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.TenantRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {
    
    private final TenantRepository tenantRepository;
    private final IdGeneratorService idGeneratorService;
    
    private TenantResponse mapToDTO(Tenant tenant) {
        return new TenantResponse(
            tenant.getTenantId(),
            tenant.getShopName(),
            tenant.getShopEmail(),
            tenant.getShopPhone(),
            tenant.getShopLogo(),
            tenant.getDomainName(),
            tenant.getSubdomain(),
            tenant.getCountryRegion(),
            tenant.getBaseCurrency(),
            tenant.getMultiCurrency(),
            tenant.getTaxId(),
            tenant.getTimezone(),
            tenant.getStatus(),
            tenant.getCreatedBy(),
            tenant.getCreated(),
            tenant.getUpdatedBy(),
            tenant.getUpdated()
        );
    }
    
    private Tenant mapToEntity(TenantRequest request, String username) {
        Tenant tenant = new Tenant();
        tenant.setTenantId(idGeneratorService.generateTenantId());
        tenant.setShopName(request.getShopName());
        tenant.setShopEmail(request.getShopEmail());
        tenant.setShopPhone(request.getShopPhone());
        tenant.setShopLogo(request.getShopLogo());
        tenant.setDomainName(request.getDomainName());
        tenant.setSubdomain(request.getSubdomain());
        tenant.setCountryRegion(request.getCountryRegion());
        tenant.setStorePassword(request.getStorePassword());
        tenant.setBaseCurrency(request.getBaseCurrency());
        tenant.setMultiCurrency(request.getMultiCurrency());
        tenant.setTaxId(request.getTaxId());
        tenant.setTimezone(request.getTimezone());
        tenant.setStatus(request.getStatus());
        tenant.setCreatedBy(username);
        tenant.setUpdatedBy(username);
        return tenant;
    }
    
    private void updateEntityFromRequest(Tenant tenant, TenantUpdateRequest request, String username) {
        if (request.getShopName() != null) {
            tenant.setShopName(request.getShopName());
        }
        if (request.getShopEmail() != null) {
            tenant.setShopEmail(request.getShopEmail());
        }
        if (request.getShopPhone() != null) {
            tenant.setShopPhone(request.getShopPhone());
        }
        if (request.getShopLogo() != null) {
            tenant.setShopLogo(request.getShopLogo());
        }
        if (request.getDomainName() != null) {
            tenant.setDomainName(request.getDomainName());
        }
        if (request.getSubdomain() != null) {
            tenant.setSubdomain(request.getSubdomain());
        }
        if (request.getCountryRegion() != null) {
            tenant.setCountryRegion(request.getCountryRegion());
        }
        if (request.getStorePassword() != null) {
            tenant.setStorePassword(request.getStorePassword());
        }
        if (request.getBaseCurrency() != null) {
            tenant.setBaseCurrency(request.getBaseCurrency());
        }
        if (request.getMultiCurrency() != null) {
            tenant.setMultiCurrency(request.getMultiCurrency());
        }
        if (request.getTaxId() != null) {
            tenant.setTaxId(request.getTaxId());
        }
        if (request.getTimezone() != null) {
            tenant.setTimezone(request.getTimezone());
        }
        if (request.getStatus() != null) {
            tenant.setStatus(request.getStatus());
        }
        tenant.setUpdatedBy(username);
        tenant.setUpdated(LocalDateTime.now());
    }
    
    @Override
    public List<TenantResponse> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public TenantResponse getTenant(Integer tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));
        return mapToDTO(tenant);
    }
    
    @Override
    @Transactional
    public TenantResponse createTenant(String username, TenantRequest tenantRequest) {
        Tenant tenant = mapToEntity(tenantRequest, username);
        Tenant savedTenant = tenantRepository.save(tenant);
        return mapToDTO(savedTenant);
    }
    
    @Override
    @Transactional
    public TenantResponse updateTenant(Integer tenantId, String username, TenantUpdateRequest updateRequest) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));
        
        updateEntityFromRequest(tenant, updateRequest, username);
        Tenant updatedTenant = tenantRepository.save(tenant);
        return mapToDTO(updatedTenant);
    }
    
    @Override
    @Transactional
    public void deleteTenant(Integer tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));
        tenantRepository.delete(tenant);
    }
    
    @Override
    public TenantResponse getTenantByDomain(String domainName) {
        Tenant tenant = tenantRepository.findByDomainName(domainName)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with domain: " + domainName));
        return mapToDTO(tenant);
    }
    
    @Override
    public TenantResponse getTenantBySubdomain(String subdomain) {
        Tenant tenant = tenantRepository.findBySubdomain(subdomain)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with subdomain: " + subdomain));
        return mapToDTO(tenant);
    }
    
    @Override
    public List<TenantResponse> getTenantsByStatus(String status) {
        return tenantRepository.findByStatus(status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TenantResponse> getTenantsByCountry(String countryRegion) {
        return tenantRepository.findByCountryRegion(countryRegion).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByEmail(String shopEmail) {
        return tenantRepository.existsByShopEmail(shopEmail);
    }
    
    @Override
    public boolean existsByDomain(String domainName) {
        return tenantRepository.existsByDomainName(domainName);
    }
    
    @Override
    public boolean existsBySubdomain(String subdomain) {
        return tenantRepository.existsBySubdomain(subdomain);
    }
}
