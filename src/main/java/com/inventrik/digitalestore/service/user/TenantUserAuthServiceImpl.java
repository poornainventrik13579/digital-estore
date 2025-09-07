package com.inventrik.digitalestore.service.user;

import com.inventrik.digitalestore.domain.tenant.Tenant;
import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.dto.request.TenantUserLoginRequest;
import com.inventrik.digitalestore.dto.request.TenantUserSignupRequest;
import com.inventrik.digitalestore.dto.response.TenantUserAuthResponse;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.exception.BusinessException;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.TenantRepository;
import com.inventrik.digitalestore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TenantUserAuthServiceImpl implements TenantUserAuthService {
    
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final UserService userService;
    
    @Override
    public TenantUserAuthResponse signup(Integer tenantId, TenantUserSignupRequest signupRequest) {
        log.info("Processing user signup for tenant ID: {} with email: {}", tenantId, signupRequest.getEmail());
        
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + tenantId));
        
        if (!"A".equals(tenant.getStatus())) {
            throw new BusinessException("Tenant store is not active");
        }
        
        UserResponse userResponse = userService.createUserForTenant(tenant.getTenantId(), signupRequest, "self-registration");
        
        User user = userRepository.findByTenantIdAndUserId(tenant.getTenantId(), userResponse.getUserId())
            .orElseThrow(() -> new BusinessException("Failed to retrieve created user"));
        
        String token = generateJwtToken(user, tenant);
        
        return new TenantUserAuthResponse(
            token,
            user.getUserId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getUserRole(),
            user.getUserType(),
            tenant.getTenantId(),
            tenant.getShopName(),
            tenant.getSubdomain(),
            tenant.getDomainName()
        );
    }
    
    @Override
    public TenantUserAuthResponse login(Integer tenantId, TenantUserLoginRequest loginRequest) {
        log.info("Processing user login for tenant ID: {} with email: {}", tenantId, loginRequest.getEmail());
        
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + tenantId));
        
        if (!"A".equals(tenant.getStatus())) {
            throw new BusinessException("Tenant store is not active");
        }
        
        User user = userRepository.findByTenantIdAndEmail(tenant.getTenantId(), loginRequest.getEmail())
            .orElseThrow(() -> new BusinessException("Invalid email or password"));
        
        if (!"A".equals(user.getStatus())) {
            throw new BusinessException("User account is not active");
        }
        
        if (user.getPasswordHash() == null || 
            !passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Invalid email or password");
        }
        
        log.info("User login successful for ID: {} in tenant: {}", user.getUserId(), tenant.getTenantId());
        
        String token = generateJwtToken(user, tenant);
        
        return new TenantUserAuthResponse(
            token,
            user.getUserId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getUserRole(),
            user.getUserType(),
            tenant.getTenantId(),
            tenant.getShopName(),
            tenant.getSubdomain(),
            tenant.getDomainName()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Integer tenantId, Long userId) {
        User user = userRepository.findByTenantIdAndUserId(tenantId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return userService.mapToUserResponse(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean usernameExistsInTenant(Integer tenantId, String username) {
        return userRepository.existsByTenantIdAndUsername(tenantId, username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean emailExistsInTenant(Integer tenantId, String email) {
        return userRepository.existsByTenantIdAndEmail(tenantId, email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean tenantExistsAndActive(Integer tenantId) {
        return tenantRepository.findById(tenantId)
            .map(tenant -> "A".equals(tenant.getStatus()))
            .orElse(false);
    }
    
    private String generateJwtToken(User user, Tenant tenant) {
        Instant now = Instant.now();
        
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("http://localhost:8080")
            .issuedAt(now)
            .expiresAt(now.plus(1, ChronoUnit.HOURS))
            .subject(user.getEmail())
            .claim("user_id", user.getUserId())
            .claim("username", user.getUsername())
            .claim("user_role", user.getUserRole().name())
            .claim("user_type", user.getUserType().name())
            .claim("tenant_id", tenant.getTenantId())
            .claim("shop_name", tenant.getShopName())
            .claim("subdomain", tenant.getSubdomain())
            .claim("domain_name", tenant.getDomainName())
            .claim("auth_type", "user") 
            .build();
        
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
