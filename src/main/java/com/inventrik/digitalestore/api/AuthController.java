package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.dto.request.ForgotPasswordRequest;
import com.inventrik.digitalestore.dto.request.LoginRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import com.inventrik.digitalestore.service.certificate.CertificateService;
import jakarta.validation.Valid;
import com.inventrik.digitalestore.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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
@RequestMapping("/api/v1/auth/platform")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CertificateService certificateService;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @PostMapping(value = "/login", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> login(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false, defaultValue = "false") boolean privateDevice,
            HttpServletResponse response) {
        try {
            // Create LoginRequest object for consistency
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername(username);
            loginRequest.setPassword(password);
            loginRequest.setPrivateDevice(privateDevice);

            log.info("Platform login attempt - username: {}", username);

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
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

    @GetMapping("/me")
    @SecurityRequirement(name = "oauth2")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'TENANT')")
    @Operation(summary = "Get current platform admin user")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.findByUsername(username));
    }
}
