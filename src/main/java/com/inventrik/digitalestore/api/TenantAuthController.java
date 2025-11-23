package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.TenantLoginRequest;
import com.inventrik.digitalestore.dto.request.TenantSignupRequest;
import com.inventrik.digitalestore.dto.response.TenantAuthResponse;
import com.inventrik.digitalestore.dto.response.TenantResponse;
import com.inventrik.digitalestore.service.tenant.TenantAuthService;
import com.inventrik.digitalestore.service.tenant.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tenant-auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Tenant Authentication", description = "APIs for tenant registration and authentication")
@Slf4j
public class TenantAuthController {
    
    private final TenantAuthService tenantAuthService;
    private final TenantService tenantService;
    
    @PostMapping("/signup")
    @Operation(summary = "Register a new tenant", 
               description = "Create a new tenant store and return authentication token")
    public ResponseEntity<TenantAuthResponse> signup(
            @Valid @RequestBody TenantSignupRequest signupRequest) {
        
        log.info("Tenant signup request received for email: {}", signupRequest.getShopEmail());
        
        TenantAuthResponse response = tenantAuthService.signup(signupRequest);
        
        log.info("Tenant signup successful for tenant ID: {}", response.getTenantId());
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PostMapping("/login")
    @Operation(summary = "Tenant login", 
               description = "Authenticate tenant and return JWT token")
    public ResponseEntity<TenantAuthResponse> login(
            @Valid @RequestBody TenantLoginRequest loginRequest) {
        
        log.info("Tenant login request received for email: {}", loginRequest.getEmail());
        
        TenantAuthResponse response = tenantAuthService.login(loginRequest);
        
        log.info("Tenant login successful for tenant ID: {}", response.getTenantId());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "Tenant login (Form)", 
               description = "Authenticate tenant using form data and return JWT token")
    public ResponseEntity<TenantAuthResponse> loginForm(
            @Valid @ModelAttribute TenantLoginRequest loginRequest) {
        
        log.info("Tenant form login request received for email: {}", loginRequest.getEmail());
        
        TenantAuthResponse response = tenantAuthService.login(loginRequest);
        
        log.info("Tenant form login successful for tenant ID: {}", response.getTenantId());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current tenant profile", 
               description = "Get authenticated tenant's profile information")
    public ResponseEntity<TenantResponse> getCurrentTenant(Authentication authentication) {
        
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Integer tenantId = jwt.getClaim("tenant_id");
        
        if (tenantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        log.info("Get current tenant profile request for tenant ID: {}", tenantId);
        
        TenantResponse tenantResponse = tenantService.getTenant(tenantId);
        
        return ResponseEntity.ok(tenantResponse);
    }
    
    @GetMapping("/check/email/{email}")
    @Operation(summary = "Check if email exists",
               description = "Validate if a tenant email already exists")
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(
            @Parameter(description = "Email to check", required = true)
            @PathVariable
            @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
            @Size(max = 255)
            String email) {

        boolean exists = tenantAuthService.emailExists(email);

        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/check/subdomain/{subdomain}")
    @Operation(summary = "Check if subdomain exists",
               description = "Validate if a subdomain already exists")
    public ResponseEntity<Map<String, Boolean>> checkSubdomainExists(
            @Parameter(description = "Subdomain to check", required = true)
            @PathVariable
            @Pattern(regexp = "^[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?$")
            @Size(min = 3, max = 63)
            String subdomain) {

        boolean exists = tenantAuthService.subdomainExists(subdomain);

        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/check/domain/{domainName}")
    @Operation(summary = "Check if domain exists",
               description = "Validate if a domain name already exists")
    public ResponseEntity<Map<String, Boolean>> checkDomainExists(
            @Parameter(description = "Domain name to check", required = true)
            @PathVariable
            @Pattern(regexp = "^[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?(\\.[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?)*$")
            @Size(max = 253)
            String domainName) {

        boolean exists = tenantAuthService.domainExists(domainName);

        return ResponseEntity.ok(Map.of("exists", exists));
    }
}
