package com.inventrik.digitalestore.service.tenant;

import com.inventrik.digitalestore.domain.tenant.Tenant;
import com.inventrik.digitalestore.domain.user.UserRole;
import com.inventrik.digitalestore.dto.request.TenantRequest;
import com.inventrik.digitalestore.dto.request.TenantSignupRequest;
import com.inventrik.digitalestore.dto.request.UserRequest;
import com.inventrik.digitalestore.dto.response.TenantResponse;
import com.inventrik.digitalestore.exception.BusinessException;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.TenantRepository;
import com.inventrik.digitalestore.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final UserService userService;

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
            tenant.getCreated(),
            tenant.getUpdated()
        );
    }

    @Override
    public List<TenantResponse> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TenantResponse getTenant(Integer tenantId) {
        Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));
        return mapToDTO(tenant);
    }

    @Override
    @Transactional
    public TenantResponse createTenant(TenantRequest request, String username) {
        if (tenantRepository.existsByShopEmail(request.getShopEmail())) {
            throw new BusinessException("Email already exists");
        }
        if (request.getSubdomain() != null && tenantRepository.existsBySubdomain(request.getSubdomain())) {
            throw new BusinessException("Subdomain already exists");
        }

        Tenant tenant = new Tenant();
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
        tenant.setStatus("0");
        tenant.setCreatedBy(username.substring(0, Math.min(2, username.length())));
        tenant.setUpdatedBy(username.substring(0, Math.min(2, username.length())));

        Tenant saved = tenantRepository.save(tenant);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public TenantResponse createTenantWithAdmin(TenantSignupRequest request) {
        if (tenantRepository.existsByShopEmail(request.getShopEmail())) {
            throw new BusinessException("Email already exists");
        }
        if (request.getSubdomain() != null && tenantRepository.existsBySubdomain(request.getSubdomain())) {
            throw new BusinessException("Subdomain already exists");
        }

        Tenant tenant = new Tenant();
        tenant.setShopName(request.getShopName());
        tenant.setShopEmail(request.getShopEmail());
        tenant.setShopPhone(request.getShopPhone());
        tenant.setSubdomain(request.getSubdomain());
        tenant.setCountryRegion(request.getCountryRegion());
        tenant.setBaseCurrency(request.getBaseCurrency());
        tenant.setMultiCurrency(true);
        tenant.setStatus("0");
        tenant.setCreatedBy("sy");
        tenant.setUpdatedBy("sy");

        Tenant savedTenant = tenantRepository.save(tenant);

        // Create admin user - use shop phone for admin
        UserRequest adminUser = new UserRequest();
        adminUser.setUsername(request.getAdminUsername());
        adminUser.setPassword(request.getAdminPassword());
        adminUser.setEmail(request.getAdminEmail());
        adminUser.setPhone(request.getShopPhone());
        adminUser.setUserRole(UserRole.ADMIN);

        userService.createUser(savedTenant.getTenantId(), "system", adminUser);

        return mapToDTO(savedTenant);
    }

    @Override
    @Transactional
    public TenantResponse updateTenant(Integer tenantId, TenantRequest request, String username) {
        Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

        tenant.setShopName(request.getShopName());
        tenant.setShopPhone(request.getShopPhone());
        tenant.setShopLogo(request.getShopLogo());
        tenant.setDomainName(request.getDomainName());
        tenant.setCountryRegion(request.getCountryRegion());
        tenant.setBaseCurrency(request.getBaseCurrency());
        tenant.setMultiCurrency(request.getMultiCurrency());
        tenant.setTaxId(request.getTaxId());
        tenant.setTimezone(request.getTimezone());
        tenant.setUpdatedBy(username.substring(0, Math.min(2, username.length())));

        Tenant updated = tenantRepository.save(tenant);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteTenant(Integer tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant not found with id: " + tenantId);
        }
        tenantRepository.deleteById(tenantId);
    }
}
