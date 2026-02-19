package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.dto.request.ForgotPasswordRequest;
import com.inventrik.digitalestore.dto.request.LoginRequest;
import com.inventrik.digitalestore.dto.request.TenantSignupRequest;
import com.inventrik.digitalestore.dto.response.TenantResponse;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import com.inventrik.digitalestore.service.certificate.CertificateService;
import com.inventrik.digitalestore.service.user.UserService;
import jakarta.validation.Valid;
import com.inventrik.digitalestore.service.tenant.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth/tenant")
@Slf4j
@RequiredArgsConstructor
public class TenantAuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CertificateService certificateService;
    private final TenantService tenantService;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @PostMapping(value = "/signup", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> tenantSignup(@Valid @ModelAttribute TenantSignupRequest request) {
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

    @PostMapping(value = "/login", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> login(
            @RequestParam Integer tenantId,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false, defaultValue = "false") boolean privateDevice,
            HttpServletResponse response) {
        try {
            // Create LoginRequest object for consistency
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setTenantId(tenantId);
            loginRequest.setUsername(username);
            loginRequest.setPassword(password);
            loginRequest.setPrivateDevice(privateDevice);

            log.info("Tenant login attempt - tenantId: {}, username: {}", tenantId, username);

            if (tenantId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tenant ID is required for tenant admin login"));
            }

            String formattedUsername = tenantId + ":" + username;

            log.info("Attempting authentication with username: {}", formattedUsername);

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(formattedUsername, password)
            );

            log.info("Authentication successful for user: {}", authentication.getName());

            Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid username or password"));
            }

            User user = userOpt.get();
            log.info("Found user - tenantId: {}, userId: {}, status: {}", user.getTenantId(), user.getUserId(), user.getStatus());

            if (loginRequest.isPrivateDevice()) {
                String sessionId = UUID.randomUUID().toString();
                certificateService.createSession(sessionId, new CertificateService.SessionData(user.getTenantId(), user.getUserId(), true));

                ResponseCookie sessionCookie = ResponseCookie.from("certSessionId", sessionId)
                        .httpOnly(true)
                        .secure(false)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(30 * 24 * 60 * 60)
                        .build();

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, sessionCookie.toString())
                        .body(Map.of("message", "Login successful", "userId", user.getUserId()));
            } else {

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
                    .claim("tenantId", loginRequest.getTenantId())
                    .build();

                String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

                return ResponseEntity.ok(Map.of(
                    "access_token", token,
                    "token_type", "Bearer",
                    "expires_in", 3600,
                    "authorities", authorities,
                    "username", authentication.getName()
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid username or password"));
        }
    }

    @PostMapping(value = "/forgot-password", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> forgotPassword(@Valid @ModelAttribute ForgotPasswordRequest request) {
        try {
            userService.sendPasswordResetEmail(request.getEmail());
            return ResponseEntity.ok(Map.of(
                "message", "If a tenant admin account with that email exists, we've sent a password reset link.",
                "email", request.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "message", "If a tenant admin account with that email exists, we've sent a password reset link.",
                "email", request.getEmail()
            ));
        }
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'TENANT')")
    @Operation(summary = "Get current tenant admin user")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.findByUsername(username));
    }
}
