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
        try {
            log.info("Login attempt - tenantId: {}, username: {}", loginRequest.getTenantId(), loginRequest.getUsername());
            
            String username = loginRequest.getTenantId() != null
                ? loginRequest.getTenantId() + ":" + loginRequest.getUsername()
                : "1" + ":" + loginRequest.getUsername();

            log.info("Attempting authentication with username: {}", username);

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword())
            );

            log.info("Authentication successful for user: {}", authentication.getName());

            Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByUsername(loginRequest.getUsername());
            }

            if (userOpt.isEmpty()) {
                log.error("User not found after authentication: {}", username);
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
                    .expiresAt(now.plus(1, ChronoUnit.HOURS))
                    .subject(authentication.getName())
                    .claim("authorities", authorities);

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
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid username or password"));
        }
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
    @SecurityRequirement(name = "oauth2")
    @Operation(summary = "Logout user")
    public ResponseEntity<?> logout(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            log.info("User logged out: {}", authentication.getName());
        }
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
}
