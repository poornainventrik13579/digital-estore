package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.dto.request.ForgotPasswordRequest;
import com.inventrik.digitalestore.dto.request.LoginRequest;
import com.inventrik.digitalestore.dto.request.SignupRequest;
import com.inventrik.digitalestore.dto.request.UserRequest;
import jakarta.validation.Valid;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.certificate.CertificateService;
import com.inventrik.digitalestore.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.inventrik.digitalestore.service.RefreshTokenService;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class UserAuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CertificateService certificateService;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @PostMapping(value = "/signup", consumes = "application/x-www-form-urlencoded")
    @Operation(summary = "Register a new user (public endpoint)")
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
                "tenantId", user.getTenantId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/login", consumes = "application/x-www-form-urlencoded")
    @Operation(summary = "Login user")
    public ResponseEntity<?> login(@Valid @ModelAttribute LoginRequest loginRequest, HttpServletResponse response) {
        String username = null;
        try {
            log.info("Login attempt - tenantId: {}, username: {}", loginRequest.getTenantId(), loginRequest.getUsername());

            username = loginRequest.getTenantId() != null
                ? loginRequest.getTenantId() + ":" + loginRequest.getUsername().toLowerCase()
                : "1" + ":" + loginRequest.getUsername().toLowerCase();

            log.info("Attempting authentication with username: {}", username);

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword())
            );

            log.info("Authentication successful for user: {}", authentication.getName());

            Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByUsername(loginRequest.getUsername().toLowerCase());
            }

            if (userOpt.isEmpty()) {
                log.error("User not found after authentication: {}", username);
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid username or password"));
            }

            User user = userOpt.get();
            log.info("Found user - tenantId: {}, userId: {}, status: {}", user.getTenantId(), user.getUserId(), user.getStatus());

            String refreshToken = refreshTokenService.createRefreshToken(authentication.getName(), UUID.randomUUID().toString()).getRefreshToken();

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

                // Certificate auth uses challenge-response, no JWT token
                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, sessionCookie.toString())
                        .body(Map.of(
                            "message", "Login successful",
                            "userId", user.getUserId()
                        ));
            } else {
                Instant now = Instant.now();
                List<String> authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

                JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                    .issuer(appBaseUrl)
                    .issuedAt(now)
                    .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                    .subject(authentication.getName())
                    .claim("authorities", authorities);

                if (loginRequest.getTenantId() != null) {
                    claimsBuilder.claim("tenantId", loginRequest.getTenantId());
                }

                JwtClaimsSet claims = claimsBuilder.build();
                String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

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
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
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

    @PostMapping(value = "/forgot-password", consumes = "application/x-www-form-urlencoded")
    @Operation(summary = "Request password reset")
    public ResponseEntity<?> forgotPassword(@Valid @ModelAttribute ForgotPasswordRequest request) {
        try {
            userService.sendPasswordResetEmail(request.getEmail());
            return ResponseEntity.ok(Map.of(
                "message", "If an account with that email exists, we've sent a password reset link.",
                "email", request.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "message", "If an account with that email exists, we've sent a password reset link.",
                "email", request.getEmail()
            ));
        }
    }


    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = false) String refreshToken) {
        String sessionId = getSessionIdFromCookie(request);
  
        if (sessionId != null) {
            Optional<User> userOpt = getUserFromSession(sessionId);
            userOpt.ifPresent(user -> {
                certificateService.deleteBySessionId(sessionId);
                certificateService.removeSessionKey(user.getUserId());
                refreshTokenService.revokeAllByUsername(user.getUsername());
            });
            certificateService.removeSession(sessionId);
        }

        if (refreshToken != null) {
            refreshTokenService.revokeRefreshToken(refreshToken);
        }
  
        ResponseCookie sessionCookie = ResponseCookie.from("certSessionId", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
  
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionCookie.toString())
                .body(Map.of("message", "Logout successful"));
    }
  
    private String getSessionIdFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("certSessionId".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
  
    private Optional<User> getUserFromSession(String sessionId) {
        CertificateService.SessionData sessionData = certificateService.getSession(sessionId);
        if (sessionData != null && sessionData.isAuthenticated()) {
            return userRepository.findByTenantIdAndUserId(sessionData.getTenantId(), sessionData.getUserId());
        }
        return Optional.empty();
    }

}
