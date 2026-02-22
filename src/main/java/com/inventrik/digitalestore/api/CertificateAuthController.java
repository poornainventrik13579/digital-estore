package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.domain.certificate.UserCertificate;
import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.dto.request.RegisterCertificateRequest;
import com.inventrik.digitalestore.dto.request.RegisterSessionKeyRequest;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.certificate.CertificateService;
import com.inventrik.digitalestore.util.CryptoUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/cert-auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class CertificateAuthController {

    private final UserRepository userRepository;
    private final CertificateService certificateService;
    private final CryptoUtil cryptoUtil;

    @PostMapping("/register-key")
    public ResponseEntity<?> registerKey(@RequestBody RegisterCertificateRequest request, HttpServletRequest httpRequest) {
        String sessionId = getSessionIdFromCookie(httpRequest);
        if (sessionId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No session"));
        }

        if (request.getPublicKey() == null || request.getPublicKey().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Public key is required"));
        }

        // Get userId from request body (sent by frontend) or fallback to session
        String userId = request.getUserId();
        Optional<User> userOpt;

        if (userId != null && !userId.trim().isEmpty()) {
            // Use userId from request body - need tenantId from session for lookup
            CertificateService.SessionData sessionData = certificateService.getSession(sessionId);
            if (sessionData == null || !sessionData.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid session"));
            }
            userOpt = userRepository.findByTenantIdAndUserId(sessionData.getTenantId(), userId);
        } else {
            // Fallback to session-based user lookup
            userOpt = getUserFromSession(sessionId);
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid session or userId"));
        }

        User user = userOpt.get();

        // Delete any old certificates for this user to avoid duplicates
        certificateService.deleteByTenantIdAndUserId(user.getTenantId(), user.getUserId());

        try {
            certificateService.createCertificate(user.getTenantId(), user.getUserId(), sessionId, request.getPublicKey());
            return ResponseEntity.ok(Map.of("message", "Public key registered successfully", "userId", user.getUserId()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Master key already registered for this session"));
        }
    }

    @PostMapping("/challenge")
    public ResponseEntity<?> generateChallenge(HttpServletRequest httpRequest) {
        String sessionId = getSessionIdFromCookie(httpRequest);
        if (sessionId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No session"));
        }

        Optional<User> userOpt = getUserFromSession(sessionId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid session"));
        }

        User user = userOpt.get();

        if (certificateService.findByTenantIdAndUserId(user.getTenantId(), user.getUserId()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Public key not registered"));
        }

        String challengeId = certificateService.createChallenge(user.getUserId(), user.getTenantId());

        return ResponseEntity.ok(Map.of("challenge", challengeId));
    }

    @PostMapping("/register-session-key")
    public ResponseEntity<?> registerSessionKey(@RequestBody RegisterSessionKeyRequest request, HttpServletRequest httpRequest) {
        String sessionId = getSessionIdFromCookie(httpRequest);
        if (sessionId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No session"));
        }

        Optional<User> userOpt = getUserFromSession(sessionId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid session"));
        }

        User user = userOpt.get();

        Optional<UserCertificate> certOpt = certificateService.findByTenantIdAndUserId(user.getTenantId(), user.getUserId());
        if (certOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Master key not registered"));
        }

        UserCertificate certificate = certOpt.get();

        try {
            PublicKey masterPublicKey = cryptoUtil.importPublicKey(certificate.getPublicKey());
            boolean valid = cryptoUtil.verifySignature(request.getSessionPublicKey(), request.getMasterSignature(), masterPublicKey);

            if (!valid) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid master signature"));
            }

            certificateService.storeSessionKey(user.getUserId(), request.getSessionPublicKey(), request.getExpiresAt());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Verification failed"));
        }

        return ResponseEntity.ok(Map.of("message", "Session key registered successfully", "userId", user.getUserId()));
    }

    @GetMapping("/check-session")
    public ResponseEntity<?> checkSession(HttpServletRequest request) {
        String sessionId = getSessionIdFromCookie(request);
        if (sessionId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No session"));
        }

        Optional<User> userOpt = getUserFromSession(sessionId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid session"));
        }

        User user = userOpt.get();

        return ResponseEntity.ok(Map.of("message", "Session valid", "userId", user.getUserId()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = getSessionIdFromCookie(request);
        if (sessionId != null) {
            Optional<User> userOpt = getUserFromSession(sessionId);
            userOpt.ifPresent(user -> {
                certificateService.deleteBySessionId(sessionId);
                certificateService.removeSessionKey(user.getUserId());
            });
            certificateService.removeSession(sessionId);
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
                .body(Map.of("message", "Logged out"));
    }

    private String getSessionIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("certSessionId".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private Optional<User> getUserFromSession(String sessionId) {
        CertificateService.SessionData sessionData = certificateService.getSession(sessionId);

        if (sessionData != null && sessionData.isAuthenticated()) {
            return userRepository.findByTenantIdAndUserId(sessionData.getTenantId(), sessionData.getUserId());
        }

        var certOpt = certificateService.findBySessionId(sessionId);
        if (certOpt.isPresent()) {
            var existingSession = certificateService.getSession(sessionId);
            if (existingSession == null || !existingSession.isAuthenticated()) {
                UserCertificate cert = certOpt.get();
                CertificateService.SessionData newSessionData = new CertificateService.SessionData(cert.getTenantId(), cert.getUserId(), true);
                certificateService.createSession(sessionId, newSessionData);
            }
            return userRepository.findByTenantIdAndUserId(certOpt.get().getTenantId(), certOpt.get().getUserId());
        }

        return Optional.empty();
    }
}
