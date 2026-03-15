package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.dto.request.ForgotPasswordRequest;
import com.inventrik.digitalestore.dto.request.LoginRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import com.inventrik.digitalestore.service.RefreshTokenService;
import com.inventrik.digitalestore.service.certificate.CertificateService;
import com.inventrik.digitalestore.service.certificate.SessionHelper;
import jakarta.validation.Valid;
import com.inventrik.digitalestore.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/auth/platform")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CertificateService certificateService;
    private final SessionHelper sessionHelper;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @PostMapping(value = "/login", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> login(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false, defaultValue = "false") boolean privateDevice,
            HttpServletResponse response) {
        try {
            // Normalize username to lowercase for authentication
            String normalizedUsername = username.toLowerCase();

            log.info("Platform login attempt - username: {}", normalizedUsername);

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedUsername, password)
            );

            log.info("Authentication successful for user: {}", authentication.getName());

            Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid username or password"));
            }

            User user = userOpt.get();
            log.info("Found user - tenantId: {}, userId: {}, status: {}", user.getTenantId(), user.getUserId(), user.getStatus());

            String refreshToken = refreshTokenService.createRefreshToken(authentication.getName(), UUID.randomUUID().toString()).getRefreshToken();

            // Generate JWT token (used for subsequent API calls)
            Instant now = Instant.now();
            List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

            JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(appBaseUrl)
                .issuedAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .subject(authentication.getName())
                .claim("authorities", authorities)
                .build();

            String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

            if (privateDevice) {
                String sessionId = UUID.randomUUID().toString();
                certificateService.createSession(sessionId, new CertificateService.SessionData(user.getTenantId(), user.getUserId(), true));

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, sessionHelper.createSessionCookie(sessionId, 30L * 24 * 60 * 60).toString())
                        .body(Map.of(
                            "message", "Login successful",
                            "userId", user.getUserId()
                        ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "access_token", token,
                    "refresh_token", refreshToken,
                    "token_type", "Bearer",
                    "expires_in", 900,
                    "authorities", authorities,
                    "username", authentication.getName()
                ));
            }

        } catch (Exception e) {
            log.error("Authentication failed for username: {}", username, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username or password"));
        }
    }

    @PostMapping(value = "/forgot-password", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> forgotPassword(@Valid @ModelAttribute ForgotPasswordRequest request) {
        try {
            userService.sendPasswordResetEmail(request.getEmail());
            return ResponseEntity.ok(Map.of(
                "message", "If a platform admin account with that email exists, we've sent a password reset link.",
                "email", request.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "message", "If a platform admin account with that email exists, we've sent a password reset link.",
                "email", request.getEmail()
            ));
        }
    }

    @PostMapping(value = "/logout", consumes = "application/x-www-form-urlencoded")
    @Operation(summary = "Logout platform admin")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(required = false) String refreshToken) {
        String sessionId = sessionHelper.getSessionIdFromCookie(request);
        sessionHelper.performLogout(sessionId);

        if (refreshToken != null) {
            refreshTokenService.revokeRefreshToken(refreshToken);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionHelper.clearSessionCookie().toString())
                .body(Map.of("message", "Logout successful"));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String oldRefreshToken = request.get("refresh_token");
        if (oldRefreshToken == null || oldRefreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "refresh_token is required"));
        }

        com.inventrik.digitalestore.domain.auth.RefreshToken token = refreshTokenService.findByRefreshToken(oldRefreshToken);
        if (!refreshTokenService.isValid(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired refresh token"));
        }

        String username = token.getUsername();
        Instant now = Instant.now();
        List<String> authorities = userRepository.findByUsername(username)
                .map(user -> List.of("ROLE_" + user.getUserRole().name()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(appBaseUrl)
                .issuedAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .subject(username)
                .claim("authorities", authorities)
                .build();

        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        com.inventrik.digitalestore.domain.auth.RefreshToken newToken =
                        refreshTokenService.rotateToken(oldRefreshToken, username);

        return ResponseEntity.ok(Map.of(
                "access_token", accessToken,
                "refresh_token", newToken.getRefreshToken(),
                "token_type", "Bearer",
                "expires_in", 900
        ));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'TENANT')")
    @Operation(summary = "Get current platform admin user")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.findByUsername(username));
    }
}
