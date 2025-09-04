package com.inventrik.digitalestore.service.user;

import com.inventrik.digitalestore.domain.tenant.Tenant;
import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.domain.user.UserRole;
import com.inventrik.digitalestore.dto.request.TenantUserLoginRequest;
import com.inventrik.digitalestore.dto.request.TenantUserSignupRequest;
import com.inventrik.digitalestore.dto.response.TenantUserAuthResponse;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.exception.BusinessException;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.TenantRepository;
import com.inventrik.digitalestore.repository.UserRepository;
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
public class TenantUserAuthServiceImpl implements TenantUserAuthService {
    
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final IdGeneratorService idGeneratorService;
    private final UserService userService;
    
    @Override
    public TenantUserAuthResponse signup(String subdomain, TenantUserSignupRequest signupRequest) {
        log.info("Processing user signup for tenant subdomain: {} with email: {}", subdomain, signupRequest.getEmail());
        
        // 1. Find and validate tenant
        Tenant tenant = tenantRepository.findBySubdomain(subdomain)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with subdomain: " + subdomain));
        
        if (!"A".equals(tenant.getStatus())) {
            throw new BusinessException("Tenant store is not active");
        }
        
        // 2. Check for duplicate users within tenant
        if (userRepository.existsByTenantIdAndUsername(tenant.getTenantId(), signupRequest.getUsername())) {
            throw new BusinessException("Username already exists in this store");
        }
        
        if (userRepository.existsByTenantIdAndEmail(tenant.getTenantId(), signupRequest.getEmail())) {
            throw new BusinessException("Email already exists in this store");
        }
        
        if (signupRequest.getPhone() != null && 
            userRepository.existsByTenantIdAndPhone(tenant.getTenantId(), signupRequest.getPhone())) {
            throw new BusinessException("Phone number already exists in this store");
        }
        
        // 3. Create new user
        User user = new User();
        user.setTenantId(tenant.getTenantId());
        user.setUserId(idGeneratorService.generateUserId());
        user.setUsername(signupRequest.getUsername());
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setEmail(signupRequest.getEmail());
        user.setPhone(signupRequest.getPhone());
        user.setImage(signupRequest.getImage());
        user.setUserType(signupRequest.getUserType());
        
        // Set role based on business logic (first user becomes tenant admin)
        long userCount = userRepository.countByTenantId(tenant.getTenantId());
        user.setUserRole(userCount == 0 ? UserRole.TENANT_ADMIN : UserRole.USER);
        
        // Company details for business users
        if (signupRequest.getUserType() != null && "BUSINESS".equals(signupRequest.getUserType().name())) {
            user.setCompanyName(signupRequest.getCompanyName());
            user.setCompanyRegistrationNumber(signupRequest.getCompanyRegistrationNumber());
            user.setCompanyAddress1(signupRequest.getCompanyAddress1());
            user.setCompanyAddress2(signupRequest.getCompanyAddress2());
            user.setCompanyCountry(signupRequest.getCompanyCountry());
            user.setCompanyPincode(signupRequest.getCompanyPincode());
            user.setTaxId(signupRequest.getTaxId());
        }
        
        // Hash password and set audit fields
        user.setPasswordHash(passwordEncoder.encode(signupRequest.getPassword()));
        user.setStatus("A"); // Active
        user.setCreatedBy("self-registration");
        user.setUpdatedBy("self-registration");
        
        // Save user
        user = userRepository.save(user);
        
        log.info("User created successfully with ID: {} for tenant: {}", user.getUserId(), tenant.getTenantId());
        
        // 4. Generate JWT token
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
    public TenantUserAuthResponse login(String subdomain, TenantUserLoginRequest loginRequest) {
        log.info("Processing user login for tenant subdomain: {} with email: {}", subdomain, loginRequest.getEmail());
        
        // 1. Find and validate tenant
        Tenant tenant = tenantRepository.findBySubdomain(subdomain)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with subdomain: " + subdomain));
        
        if (!"A".equals(tenant.getStatus())) {
            throw new BusinessException("Tenant store is not active");
        }
        
        // 2. Find user within tenant
        User user = userRepository.findByTenantIdAndEmail(tenant.getTenantId(), loginRequest.getEmail())
            .orElseThrow(() -> new BusinessException("Invalid email or password"));
        
        // 3. Check user status
        if (!"A".equals(user.getStatus())) {
            throw new BusinessException("User account is not active");
        }
        
        // 4. Verify password
        if (user.getPasswordHash() == null || 
            !passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Invalid email or password");
        }
        
        log.info("User login successful for ID: {} in tenant: {}", user.getUserId(), tenant.getTenantId());
        
        // 5. Generate JWT token
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
    public boolean usernameExistsInTenant(String subdomain, String username) {
        Tenant tenant = tenantRepository.findBySubdomain(subdomain)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with subdomain: " + subdomain));
        
        return userRepository.existsByTenantIdAndUsername(tenant.getTenantId(), username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean emailExistsInTenant(String subdomain, String email) {
        Tenant tenant = tenantRepository.findBySubdomain(subdomain)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with subdomain: " + subdomain));
        
        return userRepository.existsByTenantIdAndEmail(tenant.getTenantId(), email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean tenantExistsAndActive(String subdomain) {
        return tenantRepository.findBySubdomain(subdomain)
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
            .claim("auth_type", "user") // Distinguish from tenant auth
            .build();
        
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
