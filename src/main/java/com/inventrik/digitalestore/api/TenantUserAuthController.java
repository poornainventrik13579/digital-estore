package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.TenantUserLoginRequest;
import com.inventrik.digitalestore.dto.request.TenantUserSignupRequest;
import com.inventrik.digitalestore.dto.response.TenantUserAuthResponse;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.security.TenantAccessValidator;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/auth")
@RequiredArgsConstructor
@Tag(name = "User Authentication", description = "APIs for store customer authentication")
@Slf4j
public class TenantUserAuthController {

    private final TenantUserAuthService tenantUserAuthService;
    private final TenantAccessValidator tenantAccessValidator;

    @PostMapping("/signup")
    @Operation(summary = "Register customer to store")
    public ResponseEntity<TenantUserAuthResponse> signup(
            @PathVariable Integer tenantId,
            @Valid @RequestBody TenantUserSignupRequest signupRequest) {

        TenantUserAuthResponse response = tenantUserAuthService.signup(tenantId, signupRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login customer to store")
    public ResponseEntity<TenantUserAuthResponse> login(
            @PathVariable Integer tenantId,
            @Valid @RequestBody TenantUserLoginRequest loginRequest) {

        TenantUserAuthResponse response = tenantUserAuthService.login(tenantId, loginRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get current customer profile")
    public ResponseEntity<UserResponse> getCurrentUser(
            @PathVariable Integer tenantId,
            Authentication authentication) {

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("user_id");
        Integer tokenTenantId = jwt.getClaim("tenant_id");
        
        if (userId == null || tokenTenantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UserResponse userResponse = tenantUserAuthService.getCurrentUser(tenantId, userId);
        return ResponseEntity.ok(userResponse);
    }
    
}
