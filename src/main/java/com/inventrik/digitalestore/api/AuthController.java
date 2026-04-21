package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.domain.auth.RefreshToken;
import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.dto.request.ForgotPasswordRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.JwtDenylistService;
import com.inventrik.digitalestore.service.JwtTokenService;
import com.inventrik.digitalestore.service.RefreshTokenService;
import com.inventrik.digitalestore.service.certificate.CertificateService;
import com.inventrik.digitalestore.service.certificate.SessionHelper;
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
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth/platform")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CertificateService certificateService;
    private final SessionHelper sessionHelper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final JwtDenylistService jwtDenylistService;
    private final org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @PostMapping(value = "/login", consumes = "application/x-www-form-urlencoded")
    @Operation(summary = "Platform admin login")
    public ResponseEntity<?> login(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false, defaultValue = "false") boolean privateDevice) {
        try {
            String normalizedUsername = username.toLowerCase();
            log.info("Platform login attempt for: {}", normalizedUsername);

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedUsername, password));

            User user = userRepository.findByUsername(authentication.getName())
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

            // Platform admins have no tenant — loginIdentifier is plain username
            List<String> authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            String accessToken = jwtTokenService.buildAccessToken(authentication.getName(), authorities, null);
            String refreshTokenValue = refreshTokenService
                    .createRefreshToken(authentication.getName(), UUID.randomUUID().toString())
                    .getRefreshToken();

            return ResponseEntity.ok(Map.of(
                    "access_token", accessToken,
                    "refresh_token", refreshTokenValue,
                    "token_type", "Bearer",
                    "expires_in", JwtTokenService.ACCESS_TOKEN_SECONDS,
                    "authorities", authorities,
                    "username", authentication.getName()));

        } catch (Exception e) {
            log.error("Platform login failed for username: {}", username, e);
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

        // Platform admin: loginIdentifier is plain username, no tenantId
        String loginIdentifier = stored.getUsername();
        User user = userRepository.findByUsername(loginIdentifier).orElse(null);
        if (user == null) {
            // User deleted after token was issued — treat token as invalid
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired refresh token"));
        }

        List<String> authorities = List.of("ROLE_" + user.getUserRole().name());
        String accessToken = jwtTokenService.buildAccessToken(loginIdentifier, authorities, null);
        RefreshToken rotated = refreshTokenService.rotateToken(oldTokenValue, loginIdentifier);

        return ResponseEntity.ok(Map.of(
                "access_token", accessToken,
                "refresh_token", rotated.getRefreshToken(),
                "token_type", "Bearer",
                "expires_in", JwtTokenService.ACCESS_TOKEN_SECONDS));
    }

    @PostMapping(value = "/logout", consumes = "application/x-www-form-urlencoded")
    @Operation(summary = "Logout platform admin")
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
                "message", "If a platform admin account with that email exists, we've sent a password reset link.",
                "email", request.getEmail()));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'TENANT')")
    @Operation(summary = "Get current platform admin user")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userService.findByUsername(authentication.getName()));
    }
}
