package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.domain.auth.RefreshToken;
import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.dto.request.ForgotPasswordRequest;
import com.inventrik.digitalestore.dto.request.TenantSignupRequest;
import com.inventrik.digitalestore.dto.response.TenantResponse;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.JwtDenylistService;
import com.inventrik.digitalestore.service.JwtTokenService;
import com.inventrik.digitalestore.service.RefreshTokenService;
import com.inventrik.digitalestore.service.certificate.CertificateService;
import com.inventrik.digitalestore.service.certificate.SessionHelper;
import com.inventrik.digitalestore.service.tenant.TenantService;
import com.inventrik.digitalestore.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

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
    private final SessionHelper sessionHelper;
    private final TenantService tenantService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final JwtDenylistService jwtDenylistService;
    private final org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @PostMapping(value = "/signup", consumes = "application/x-www-form-urlencoded")
    @Operation(summary = "Create a new tenant with admin account")
    public ResponseEntity<?> tenantSignup(@Valid @ModelAttribute TenantSignupRequest request) {
        try {
            TenantResponse tenant = tenantService.createTenantWithAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Tenant created successfully",
                    "tenantId", tenant.getTenantId(),
                    "shopName", tenant.getShopName(),
                    "adminUsername", request.getAdminUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/login", consumes = "application/x-www-form-urlencoded")
    @Operation(summary = "Tenant admin login")
    public ResponseEntity<?> login(
            @RequestParam Integer tenantId,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false, defaultValue = "false") boolean privateDevice) {

        // loginIdentifier is "tenantId:username" — used for Spring Security lookup
        // and stored in the refresh token so we can do tenant-aware lookups on refresh
        String loginIdentifier = tenantId + ":" + username.toLowerCase();
        try {
            log.info("Tenant login attempt - tenantId: {}, username: {}", tenantId, username);

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginIdentifier, password));

            User user = userRepository.findByTenantIdAndUsername(tenantId, authentication.getName())
                    .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

            if (privateDevice) {
                String sessionId = UUID.randomUUID().toString();
                certificateService.createSession(sessionId,
                        new CertificateService.SessionData(user.getTenantId(), user.getUserId(), true));

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE,
                                sessionHelper.createSessionCookie(sessionId, 30L * 24 * 60 * 60).toString())
                        .body(Map.of("message", "Login successful", "userId", user.getUserId(), "sessionId", sessionId));
            }

            List<String> authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            String accessToken = jwtTokenService.buildAccessToken(authentication.getName(), authorities, tenantId);
            String refreshTokenValue = refreshTokenService
                    .createRefreshToken(loginIdentifier, UUID.randomUUID().toString())
                    .getRefreshToken();

            return ResponseEntity.ok(Map.of(
                    "access_token", accessToken,
                    "refresh_token", refreshTokenValue,
                    "token_type", "Bearer",
                    "expires_in", JwtTokenService.ACCESS_TOKEN_SECONDS,
                    "authorities", authorities,
                    "username", authentication.getName()));

        } catch (Exception e) {
            log.error("Tenant login failed for loginIdentifier: {}", loginIdentifier, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        String oldTokenValue = body.get("refresh_token");
        if (oldTokenValue == null || oldTokenValue.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "refresh_token is required"));
        }

        RefreshToken stored = refreshTokenService.findByRefreshToken(oldTokenValue);
        if (!refreshTokenService.isValid(stored)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired refresh token"));
        }

        // loginIdentifier is "tenantId:username" — parse both parts for tenant-aware lookup
        String loginIdentifier = stored.getUsername();
        Integer tenantId = JwtTokenService.parseTenantId(loginIdentifier);
        String plainUsername = JwtTokenService.parseUsername(loginIdentifier);

        Optional<User> userOpt = tenantId != null
                ? userRepository.findByTenantIdAndUsername(tenantId, plainUsername)
                : userRepository.findByUsername(plainUsername);

        if (userOpt.isEmpty()) {
            // User deleted after token was issued — treat token as invalid
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired refresh token"));
        }
        User user = userOpt.get();

        List<String> authorities = List.of("ROLE_" + user.getUserRole().name());
        String accessToken = jwtTokenService.buildAccessToken(plainUsername, authorities, tenantId);
        RefreshToken rotated = refreshTokenService.rotateToken(oldTokenValue, loginIdentifier);

        return ResponseEntity.ok(Map.of(
                "access_token", accessToken,
                "refresh_token", rotated.getRefreshToken(),
                "token_type", "Bearer",
                "expires_in", JwtTokenService.ACCESS_TOKEN_SECONDS));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout tenant admin")
    public ResponseEntity<?> logout(HttpServletRequest request,
            @RequestParam(required = false) String refreshTokenValue) {
        sessionHelper.performLogout(sessionHelper.getSessionIdFromCookie(request));
        denyBearerToken(request);

        if (refreshTokenValue != null) {
            refreshTokenService.revokeRefreshToken(refreshTokenValue);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionHelper.clearSessionCookie().toString())
                .body(Map.of("message", "Logout successful"));
    }

    private void denyBearerToken(HttpServletRequest request) {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) return;
        try {
            org.springframework.security.oauth2.jwt.Jwt jwt = jwtDecoder.decode(auth.substring(7));
            jwtDenylistService.add(jwt.getId(), jwt.getExpiresAt());
        } catch (Exception ignored) {
        }
    }

    @PostMapping(value = "/forgot-password", consumes = "application/x-www-form-urlencoded")
    @Operation(summary = "Request password reset")
    public ResponseEntity<?> forgotPassword(@Valid @ModelAttribute ForgotPasswordRequest request) {
        // Always return same message — prevents user enumeration
        try {
            userService.sendPasswordResetEmail(request.getEmail());
        } catch (Exception e) {
            log.warn("Password reset failed for email: {}", request.getEmail(), e);
        }
        return ResponseEntity.ok(Map.of(
                "message", "If a tenant admin account with that email exists, we've sent a password reset link.",
                "email", request.getEmail()));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'TENANT')")
    @Operation(summary = "Get current tenant admin user")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        // Use tenantId JWT claim for tenant-scoped lookup to avoid cross-tenant username collision
        if (authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth) {
            Integer tenantId = jwtAuth.getToken().getClaim("tenantId");
            if (tenantId != null) {
                return userRepository.findByTenantIdAndUsername(tenantId, authentication.getName())
                        .map(u -> ResponseEntity.ok(userService.findByUsername(authentication.getName())))
                        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
            }
        }
        return ResponseEntity.ok(userService.findByUsername(authentication.getName()));
    }
}
