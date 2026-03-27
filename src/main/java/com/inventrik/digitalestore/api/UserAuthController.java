package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.domain.auth.RefreshToken;
import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.dto.request.ForgotPasswordRequest;
import com.inventrik.digitalestore.dto.request.LoginRequest;
import com.inventrik.digitalestore.dto.request.SignupRequest;
import com.inventrik.digitalestore.dto.request.UserRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.JwtTokenService;
import com.inventrik.digitalestore.service.RefreshTokenService;
import com.inventrik.digitalestore.service.certificate.CertificateService;
import com.inventrik.digitalestore.service.certificate.SessionHelper;
import com.inventrik.digitalestore.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "User Authentication", description = "Authentication APIs for users/customers")
@CrossOrigin(originPatterns = {
        "http://localhost:4200", "http://localhost:4201", "http://localhost:3000",
        "https://*.ngrok-free.app", "https://*.ngrok.io"
})
public class UserAuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CertificateService certificateService;
    private final SessionHelper sessionHelper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping(value = "/signup", consumes = "application/x-www-form-urlencoded")
    @Operation(summary = "Register a new user")
    public ResponseEntity<?> signup(@Valid @ModelAttribute SignupRequest request) {
        try {
            Integer tenantId = request.getTenantId() != null ? request.getTenantId() : 1;

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
                    "tenantId", user.getTenantId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/login", consumes = "application/x-www-form-urlencoded")
    @Operation(summary = "Login user")
    public ResponseEntity<?> login(@Valid @ModelAttribute LoginRequest loginRequest) {
        // loginIdentifier = "tenantId:username" — used for Spring Security and stored in refresh token
        // so we can do tenant-aware lookups on refresh without ambiguity across tenants
        int effectiveTenantId = loginRequest.getTenantId() != null ? loginRequest.getTenantId() : 1;
        String loginIdentifier = effectiveTenantId + ":" + loginRequest.getUsername().toLowerCase();

        try {
            log.info("User login attempt - tenantId: {}, username: {}", effectiveTenantId,
                    loginRequest.getUsername());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginIdentifier, loginRequest.getPassword()));

            // Tenant-aware lookup — avoids cross-tenant username collision
            User user = userRepository
                    .findByTenantIdAndUsername(effectiveTenantId, authentication.getName())
                    .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

            if (loginRequest.isPrivateDevice()) {
                String sessionId = UUID.randomUUID().toString();
                certificateService.createSession(sessionId,
                        new CertificateService.SessionData(user.getTenantId(), user.getUserId(), true));

                // sessionId also returned in body for Safari ITP (blocks cross-origin cookies)
                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE,
                                sessionHelper.createSessionCookie(sessionId, 30L * 24 * 60 * 60).toString())
                        .body(Map.of(
                                "message", "Login successful",
                                "userId", user.getUserId(),
                                "sessionId", sessionId));
            }

            List<String> authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            String accessToken = jwtTokenService.buildAccessToken(
                    authentication.getName(), authorities, effectiveTenantId);
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
            log.error("User login failed for loginIdentifier: {}", loginIdentifier, e);
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

        // loginIdentifier is "tenantId:username" — parse both parts for tenant-aware user lookup
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
                "message", "If an account with that email exists, we've sent a password reset link.",
                "email", request.getEmail()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<?> logout(HttpServletRequest request,
            @RequestParam(required = false) String refreshTokenValue) {
        String sessionId = sessionHelper.getSessionIdFromCookie(request);
        sessionHelper.performLogout(sessionId);

        if (refreshTokenValue != null) {
            refreshTokenService.revokeRefreshToken(refreshTokenValue);
        } else {
            // refreshToken not passed — revoke all tokens for this user so none remain valid after logout
            sessionHelper.getUserFromSession(sessionId)
                    .ifPresent(user -> refreshTokenService.revokeAllByUsername(user.getUsername()));
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionHelper.clearSessionCookie().toString())
                .body(Map.of("message", "Logout successful"));
    }
}
