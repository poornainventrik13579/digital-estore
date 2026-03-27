package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.domain.certificate.UserCertificate;
import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.dto.request.RegisterCertificateRequest;
import com.inventrik.digitalestore.dto.request.RegisterSessionKeyRequest;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.certificate.CertificateService;
import com.inventrik.digitalestore.service.certificate.SessionHelper;
import com.inventrik.digitalestore.util.CryptoUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/cert-auth")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = {"http://localhost:4200", "http://localhost:4201", "http://localhost:3000", "https://*.ngrok-free.app", "https://*.ngrok.io"})
public class CertificateAuthController {

    private final UserRepository userRepository;
    private final CertificateService certificateService;
    private final CryptoUtil cryptoUtil;
    private final SessionHelper sessionHelper;

    @PostMapping("/register-key")
    public ResponseEntity<?> registerKey(@RequestBody RegisterCertificateRequest request, HttpServletRequest httpRequest) {
        String sessionId = sessionHelper.getSessionIdFromCookie(httpRequest);
        if (sessionId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No session"));
        }

        if (request.getPublicKey() == null || request.getPublicKey().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Public key is required"));
        }

        CertificateService.SessionData sessionData = certificateService.getSession(sessionId);
        if (sessionData == null || !sessionData.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid session"));
        }

        String userId = request.getUserId();
        Optional<User> userOpt;

        if (userId != null && !userId.trim().isEmpty()) {
            // Verify the requested userId matches the session owner — prevents registering a key on behalf of another user
            if (!userId.equals(sessionData.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Cannot register key for another user"));
            }
            userOpt = userRepository.findByTenantIdAndUserId(sessionData.getTenantId(), userId);
        } else {
            userOpt = sessionHelper.getUserFromSession(sessionId);
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid session or userId"));
        }

        User user = userOpt.get();
        certificateService.revokeByTenantIdAndUserId(user.getTenantId(), user.getUserId());
        certificateService.removeSessionKey(user.getUserId()); // evict stale session key from Redis
        certificateService.removeSession(sessionId); // evict stale Redis session
        
        String pk = request.getPublicKey().trim();
        if (pk.length() > 124) {
            return ResponseEntity.badRequest().body(Map.of("error", "Public key exceeds maximum length"));
        }
        try {
            cryptoUtil.importPublicKey(pk);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid public key format"));
        }
        
        try {
            certificateService.createCertificate(user.getTenantId(), user.getUserId(), sessionId, pk);
            return ResponseEntity.ok(Map.of("message", "Public key registered successfully", "userId", user.getUserId()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Master key already registered for this session"));
        }
    }

    @PostMapping("/challenge")
    public ResponseEntity<?> generateChallenge(HttpServletRequest httpRequest) {
        String sessionId = sessionHelper.getSessionIdFromCookie(httpRequest);
        if (sessionId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No session"));
        }

        Optional<User> userOpt = sessionHelper.getUserFromSession(sessionId);
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
        String sessionId = sessionHelper.getSessionIdFromCookie(httpRequest);
        if (sessionId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No session"));
        }

        Optional<User> userOpt = sessionHelper.getUserFromSession(sessionId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid session"));
        }

        User user = userOpt.get();

        Optional<UserCertificate> certOpt = certificateService.findByTenantIdAndUserId(user.getTenantId(), user.getUserId());
        if (certOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Master key not registered"));
        }

        try {
            PublicKey masterPublicKey = cryptoUtil.importPublicKey(certOpt.get().getPublicKey());
            boolean valid = cryptoUtil.verifySignature(request.getSessionPublicKey(), request.getMasterSignature(), masterPublicKey);

            if (!valid) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid master signature"));
            }

            certificateService.storeSessionKey(user.getUserId(), request.getSessionPublicKey(), request.getExpiresAt());
        } catch (Exception e) {
            log.error("Session key registration failed for userId: {}", user.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Verification failed"));
        }

        return ResponseEntity.ok(Map.of("message", "Session key registered successfully", "userId", user.getUserId()));
    }

    @GetMapping("/check-session")
    public ResponseEntity<?> checkSession(HttpServletRequest request) {
        String sessionId = sessionHelper.getSessionIdFromCookie(request);
        if (sessionId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No session"));
        }

        Optional<User> userOpt = sessionHelper.getUserFromSession(sessionId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid session"));
        }

        return ResponseEntity.ok(Map.of("message", "Session valid", "userId", userOpt.get().getUserId()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        sessionHelper.performLogout(sessionHelper.getSessionIdFromCookie(request));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionHelper.clearSessionCookie().toString())
                .body(Map.of("message", "Logged out"));
    }
}
