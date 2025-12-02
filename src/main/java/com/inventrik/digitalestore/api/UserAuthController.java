package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.ForgotPasswordRequest;
import com.inventrik.digitalestore.dto.request.LoginRequest;
import com.inventrik.digitalestore.dto.request.SignupRequest;
import com.inventrik.digitalestore.dto.request.UserRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

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
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles user/customer authentication operations
 * Simplified endpoints without tenantId in URL path
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "User Authentication", description = "Authentication APIs for users/customers")
public class UserAuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;

    @Value("${app.base-url}")
    private String appBaseUrl;

    /**
     * User self-registration
     * Public endpoint - no authentication required
     */
    @PostMapping(value = "/signup", consumes = {
            "application/json",
            "application/x-www-form-urlencoded"
    })
    @Operation(summary = "Register a new user (public endpoint)")
    public ResponseEntity<?> signup(@Valid @ModelAttribute SignupRequest request) {
        try {
            Integer tenantId = request.getTenantId() != null ? request.getTenantId() : 1; // Default tenant

            UserRequest userRequest = new UserRequest();
            userRequest.setUsername(request.getUsername());
            userRequest.setEmail(request.getEmail());
            userRequest.setPassword(request.getPassword());
            userRequest.setFirstName(request.getFirstName());
            userRequest.setLastName(request.getLastName());
            userRequest.setPhone(request.getPhone());

            UserResponse user = userService.createUser(tenantId, "system", userRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "User registered successfully",
                "userId", user.getUserId(),
                "username", user.getUsername(),
                "tenantId", user.getTenantId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Authenticate user and generate JWT token
     * Uses tenantId:username format if tenantId provided, otherwise plain username
     */
    @PostMapping(value = "/login", consumes = {
            "application/json",
            "application/x-www-form-urlencoded"
    })
    @Operation(summary = "Login user")
    public ResponseEntity<?> login(@Valid @ModelAttribute LoginRequest loginRequest) {
        try {
            // Build username: if tenantId provided, use "tenantId:username" format, else just username
            String username = loginRequest.getTenantId() != null
                ? loginRequest.getTenantId() + ":" + loginRequest.getUsername()
                : loginRequest.getUsername();

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword())
            );

            Instant now = Instant.now();
            List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

            JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer(appBaseUrl)
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .subject(authentication.getName())
                .claim("authorities", authorities);

            // Add tenantId as separate claim if provided
            if (loginRequest.getTenantId() != null) {
                claimsBuilder.claim("tenantId", loginRequest.getTenantId());
            }

            JwtClaimsSet claims = claimsBuilder.build();

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
     * Password reset for users
     * Public endpoint - doesn't reveal if email exists
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            userService.sendPasswordResetEmail(request.getEmail());
            return ResponseEntity.ok(Map.of(
                "message", "If an account with that email exists, we've sent a password reset link.",
                "email", request.getEmail()
            ));
        } catch (Exception e) {
            // Don't reveal if email exists for security
            return ResponseEntity.ok(Map.of(
                "message", "If an account with that email exists, we've sent a password reset link.",
                "email", request.getEmail()
            ));
        }
    }
}
