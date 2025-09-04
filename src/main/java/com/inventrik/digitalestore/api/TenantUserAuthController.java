package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.TenantUserLoginRequest;
import com.inventrik.digitalestore.dto.request.TenantUserSignupRequest;
import com.inventrik.digitalestore.dto.response.TenantUserAuthResponse;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.service.user.TenantUserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Store User Authentication", description = "Shopify-style store user authentication APIs")
@Slf4j
public class TenantUserAuthController {
    
    private final TenantUserAuthService tenantUserAuthService;
    
    @PostMapping("/{subdomain}/auth/users/signup")
    @Operation(summary = "Register user to store")
    public ResponseEntity<TenantUserAuthResponse> signup(
            @PathVariable String subdomain,
            @Valid @RequestBody TenantUserSignupRequest signupRequest) {
        
        TenantUserAuthResponse response = tenantUserAuthService.signup(subdomain, signupRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PostMapping("/{subdomain}/auth/users/login")
    @Operation(summary = "Login user to store")
    public ResponseEntity<TenantUserAuthResponse> login(
            @PathVariable String subdomain,
            @Valid @RequestBody TenantUserLoginRequest loginRequest) {
        
        TenantUserAuthResponse response = tenantUserAuthService.login(subdomain, loginRequest);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping(value = "/{subdomain}/auth/users/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "Login user via form data")
    public ResponseEntity<TenantUserAuthResponse> loginForm(
            @PathVariable String subdomain,
            @Valid @ModelAttribute TenantUserLoginRequest loginRequest) {
        
        TenantUserAuthResponse response = tenantUserAuthService.login(subdomain, loginRequest);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/auth/users/profile")
    @Operation(summary = "Get user profile")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("user_id");
        Integer tenantId = jwt.getClaim("tenant_id");
        
        if (userId == null || tenantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UserResponse userResponse = tenantUserAuthService.getCurrentUser(tenantId, userId);
        return ResponseEntity.ok(userResponse);
    }
    
    @GetMapping("/{subdomain}/auth/users/check/username/{username}")
    @Operation(summary = "Check if username exists in store")
    public ResponseEntity<Map<String, Boolean>> checkUsernameExists(
            @PathVariable String subdomain,
            @PathVariable String username) {
        
        boolean exists = tenantUserAuthService.usernameExistsInTenant(subdomain, username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
    
    @GetMapping("/{subdomain}/auth/users/check/email/{email}")
    @Operation(summary = "Check if email exists in store")
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(
            @PathVariable String subdomain,
            @PathVariable String email) {
        
        boolean exists = tenantUserAuthService.emailExistsInTenant(subdomain, email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}
