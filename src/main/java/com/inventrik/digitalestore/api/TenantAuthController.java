package com.inventrik.digitalestore.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import com.inventrik.digitalestore.dto.request.ForgotPasswordRequest;
import com.inventrik.digitalestore.dto.request.LoginRequest;
import com.inventrik.digitalestore.dto.request.TenantSignupRequest;
import com.inventrik.digitalestore.dto.response.TenantResponse;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.service.user.UserService;
import com.inventrik.digitalestore.service.tenant.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Handles tenant admin authentication: signup, login, and password reset
 * For tenant-level administrators who manage specific stores
 */
@RestController
@RequestMapping("/api/v1/auth/tenant")
@RequiredArgsConstructor
public class TenantAuthController {

    private final UserService userService;
    private final TenantService tenantService;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;

    @Value("${app.base-url}")
    private String appBaseUrl;

    /**
     * Create new tenant with admin user
     * Public endpoint - no authentication required
     */
    @PostMapping("/signup")
    public ResponseEntity<?> tenantSignup(@Valid @RequestBody TenantSignupRequest request) {
        try {
            TenantResponse tenant = tenantService.createTenantWithAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Tenant created successfully",
                "tenantId", tenant.getTenantId(),
                "shopName", tenant.getShopName(),
                "adminUsername", request.getAdminUsername()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Authenticate tenant admin and generate JWT token
     * Uses tenantId:username format for authentication
     */
    @PostMapping(value = "/login", consumes = {
            "application/json",
            "application/x-www-form-urlencoded"
    })
    public ResponseEntity<?> login(@Valid @ModelAttribute LoginRequest loginRequest) {
        try {
            if (loginRequest.getTenantId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tenant ID is required for tenant admin login"));
            }

            // Tenant admins use tenantId:username format
            String username = loginRequest.getTenantId() + ":" + loginRequest.getUsername();

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword())
            );

            Instant now = Instant.now();
            List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

            JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(appBaseUrl)
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .subject(authentication.getName())
                .claim("authorities", authorities)
                .claim("tenantId", loginRequest.getTenantId()) // Add tenantId as separate claim
                .build();

            String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

            return ResponseEntity.ok(Map.of(
                "access_token", token,
                "token_type", "Bearer",
                "expires_in", 3600,
                "authorities", authorities,
                "username", authentication.getName()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid username or password"));
        }
    }

    /**
     * Password reset for tenant admins
     * Public endpoint - doesn't reveal if email exists
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            userService.sendPasswordResetEmail(request.getEmail());
            return ResponseEntity.ok(Map.of(
                "message", "If a tenant admin account with that email exists, we've sent a password reset link.",
                "email", request.getEmail()
            ));
        } catch (Exception e) {
            // Don't reveal if email exists for security
            return ResponseEntity.ok(Map.of(
                "message", "If a tenant admin account with that email exists, we've sent a password reset link.",
                "email", request.getEmail()
            ));
        }
    }

    /**
     * Get current tenant admin user details
     */
    @GetMapping("/me")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get current tenant admin user")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.findByUsername(username));
    }
}
