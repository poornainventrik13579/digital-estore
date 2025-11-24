package com.inventrik.digitalestore.service.tenant;

import com.inventrik.digitalestore.domain.tenant.Tenant;
import com.inventrik.digitalestore.dto.request.TenantLoginRequest;
import com.inventrik.digitalestore.dto.request.TenantSignupRequest;
import com.inventrik.digitalestore.dto.response.TenantAuthResponse;
import com.inventrik.digitalestore.exception.BusinessException;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.TenantRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TenantAuthServiceImpl implements TenantAuthService {
    
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final IdGeneratorService idGeneratorService;
    
    @Override
    public TenantAuthResponse signup(TenantSignupRequest signupRequest) {
        log.info("Processing tenant signup for email: {}", signupRequest.getShopEmail());
        
        if (emailExists(signupRequest.getShopEmail())) {
            throw new BusinessException("A tenant with this email already exists");
        }
        
        if (subdomainExists(signupRequest.getSubdomain())) {
            throw new BusinessException("A tenant with this subdomain already exists");
        }

        if (domainExists(signupRequest.getDomainName())) {
            throw new BusinessException("A tenant with this domain name already exists");
        }
        
        Tenant tenant = new Tenant();
        tenant.setTenantId(idGeneratorService.generateTenantId());
        tenant.setShopName(signupRequest.getShopName());
        tenant.setShopEmail(signupRequest.getShopEmail());
        tenant.setShopPhone(signupRequest.getShopPhone());
        tenant.setShopLogo(signupRequest.getShopLogo());
        tenant.setDomainName(signupRequest.getDomainName());
        tenant.setSubdomain(signupRequest.getSubdomain());
        tenant.setCountryRegion(signupRequest.getCountryRegion());
        tenant.setBaseCurrency(signupRequest.getBaseCurrency());
        tenant.setMultiCurrency(signupRequest.getMultiCurrency());
        tenant.setTaxId(signupRequest.getTaxId());
        tenant.setTimezone(signupRequest.getTimezone());
        tenant.setStatus("A");
        tenant.setCreatedBy("tenant-self");
        tenant.setUpdatedBy("tenant-self");
        
        String hashedPassword = passwordEncoder.encode(signupRequest.getPassword());
        tenant.setStorePassword(hashedPassword);
        
        tenant = tenantRepository.save(tenant);
        
        log.info("Tenant created successfully with ID: {}", tenant.getTenantId());
        
        String token = generateJwtToken(tenant);
        
        return new TenantAuthResponse(
            token,
            tenant.getTenantId(),
            tenant.getShopName(),
            tenant.getShopEmail(),
            tenant.getSubdomain(),
            tenant.getDomainName()
        );
    }
    
    @Override
    public TenantAuthResponse login(TenantLoginRequest loginRequest) {
        log.info("Processing tenant login for email: {}", loginRequest.getEmail());
        
        Tenant tenant = tenantRepository.findByShopEmail(loginRequest.getEmail())
            .orElseThrow(() -> new BusinessException("Invalid email or password"));
        
        if (!"A".equals(tenant.getStatus())) {
            throw new BusinessException("Tenant account is not active");
        }
        
        if (tenant.getStorePassword() == null || 
            !passwordEncoder.matches(loginRequest.getPassword(), tenant.getStorePassword())) {
            throw new BusinessException("Invalid email or password");
        }
        
        log.info("Tenant login successful for ID: {}", tenant.getTenantId());
        
        String token = generateJwtToken(tenant);
        
        return new TenantAuthResponse(
            token,
            tenant.getTenantId(),
            tenant.getShopName(),
            tenant.getShopEmail(),
            tenant.getSubdomain(),
            tenant.getDomainName()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return tenantRepository.existsByShopEmail(email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean subdomainExists(String subdomain) {
        return tenantRepository.existsBySubdomain(subdomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean domainExists(String domainName) {
        return tenantRepository.existsByDomainName(domainName);
    }
    
    private String generateJwtToken(Tenant tenant) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("http://localhost:8080")
            .issuedAt(now)
            .expiresAt(now.plus(1, ChronoUnit.HOURS))
            .subject(tenant.getShopEmail())
            .claim("tenant_id", tenant.getTenantId())
            .claim("shop_name", tenant.getShopName())
            .claim("subdomain", tenant.getSubdomain())
            .claim("domain_name", tenant.getDomainName())
            .claim("user_type", "tenant")
            .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
