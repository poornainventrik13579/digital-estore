package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.ForgotPasswordRequest;
import com.inventrik.digitalestore.dto.request.SignupRequest;
import com.inventrik.digitalestore.dto.request.UserRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

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
 * Handles user/customer authentication operations within a tenant context
 * All endpoints are tenant-scoped with tenantId in the URL path
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/auth")
@RequiredArgsConstructor
@Tag(name = "User Authentication", description = "Authentication APIs for tenant users/customers")
public class UserAuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;

    /**
     * User self-registration within a tenant
     * Public endpoint - no authentication required
     */
    @PostMapping("/signup")
    @Operation(summary = "Register a new user (public endpoint)")
    public ResponseEntity<?> signup(
            @PathVariable Integer tenantId,
            @Valid @RequestBody SignupRequest request) {
        try {
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
                "username", user.getUsername()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Authenticate user and generate JWT token
     * Uses tenantId:username format for authentication
     */
    @PostMapping("/login")
    @Operation(summary = "Login user within a tenant")
    public ResponseEntity<?> login(
            @PathVariable Integer tenantId,
            @Valid @RequestBody SignupRequest loginRequest) {
        try {
            // Users use tenantId:username format
            String username = tenantId + ":" + loginRequest.getUsername();

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword())
            );

            Instant now = Instant.now();
            List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

            JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("http://localhost:8080")
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .subject(authentication.getName())
                .claim("authorities", authorities)
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
     * Password reset for tenant users
     * Public endpoint - doesn't reveal if email exists
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset for a user")
    public ResponseEntity<?> forgotPassword(
            @PathVariable Integer tenantId,
            @Valid @RequestBody ForgotPasswordRequest request) {
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

    /**
     * Get current user details
     */
    @GetMapping("/me")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get current user")
    public ResponseEntity<UserResponse> getCurrentUser(
            @PathVariable Integer tenantId,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.findByUsername(username));
    }
}
