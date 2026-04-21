package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.domain.certificate.CertificateStatus;
import com.inventrik.digitalestore.domain.certificate.UserCertificate;
import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.dto.request.RegisterCertificateRequest;
import com.inventrik.digitalestore.dto.request.RegisterSessionKeyRequest;
import com.inventrik.digitalestore.repository.UserCertificateRepository;
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
    private final UserCertificateRepository userCertificateRepository;

    @PostMapping("/register-key")
    public ResponseEntity<?> registerKey(@RequestBody RegisterCertificateRequest request, HttpServletRequest httpRequest) {
        String sessionId = sessionHelper.getSessionIdFromCookie(httpRequest);
        if (sessionId == null) {
            log.warn("/register-key 401: no sessionId in cookie or X-Session-ID header. cookies={}, x-session-id={}, referer={}",
                    httpRequest.getCookies() == null ? "null" : httpRequest.getCookies().length,
                    httpRequest.getHeader("X-Session-ID"),
                    httpRequest.getHeader("Referer"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No session"));
        }

        if (request.getPublicKey() == null || request.getPublicKey().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Public key is required"));
        }

        CertificateService.SessionData sessionData = certificateService.getSession(sessionId);
        if (sessionData == null || !sessionData.isAuthenticated()) {
            log.warn("/register-key 401 Invalid session: sessionId={} sessionData={} authenticated={}",
                    sessionId, sessionData, sessionData != null && sessionData.isAuthenticated());
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

        // Validate publicKey BEFORE any mutation.
        String pk = request.getPublicKey().trim();
        if (pk.length() > 124) {
            return ResponseEntity.badRequest().body(Map.of("error", "Public key exceeds maximum length"));
        }
        try {
            cryptoUtil.importPublicKey(pk);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid public key format"));
        }

        // Atomically rotate: revoke OTHER sessions' certs and upsert this session's cert.
        // Do NOT evict the Redis login session — this is the authenticated session, it stays valid.
        // Do evict the stale session key so /register-session-key writes a fresh one.
        certificateService.rotateMasterKey(user.getTenantId(), user.getUserId(), sessionId, pk);
        certificateService.removeSessionKey(user.getUserId());

        return ResponseEntity.ok(Map.of("message", "Public key registered successfully", "userId", user.getUserId()));
    }

    @PostMapping("/challenge")
    public ResponseEntity<?> generateChallenge(HttpServletRequest httpRequest) {
        String sessionId = sessionHelper.getSessionIdFromCookie(httpRequest);
        if (sessionId == null) {
            log.warn("/challenge rejected: no sessionId in cookie or X-Session-ID header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No session"));
        }

        Optional<User> userOpt = sessionHelper.getUserFromSession(sessionId);
        if (userOpt.isEmpty()) {
            CertificateService.SessionData raw = certificateService.getSession(sessionId);
            log.warn("/challenge rejected: getUserFromSession empty for sessionId={} (sessionData={}, authenticated={})",
                    sessionId, raw, raw != null && raw.isAuthenticated());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid session"));
        }

        User user = userOpt.get();

        if (certificateService.findByTenantIdAndUserId(user.getTenantId(), user.getUserId()).isEmpty()) {
            log.warn("/challenge rejected: no ACTIVE cert for tenantId={} userId={}", user.getTenantId(), user.getUserId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Public key not registered"));
        }

        String challengeId = certificateService.createChallenge(user.getUserId(), user.getTenantId());
        return ResponseEntity.ok(Map.of("challenge", challengeId));
    }

    @PostMapping("/register-session-key")
    public ResponseEntity<?> registerSessionKey(@RequestBody RegisterSessionKeyRequest request, HttpServletRequest httpRequest) {
        String sessionId = sessionHelper.getSessionIdFromCookie(httpRequest);
        if (sessionId == null) {
            log.warn("/register-session-key 401: no sessionId in cookie or X-Session-ID header. cookies={}, x-session-id={}, referer={}",
                    httpRequest.getCookies() == null ? "null" : httpRequest.getCookies().length,
                    httpRequest.getHeader("X-Session-ID"),
                    httpRequest.getHeader("Referer"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No session"));
        }

        Optional<User> userOpt = sessionHelper.getUserFromSession(sessionId);
        if (userOpt.isEmpty()) {
            CertificateService.SessionData raw = certificateService.getSession(sessionId);
            log.warn("/register-session-key 401 Invalid session: sessionId={} sessionData={} authenticated={}",
                    sessionId, raw, raw != null && raw.isAuthenticated());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid session"));
        }

        User user = userOpt.get();

        // Prefer sessionId lookup (PK) — aligns with the authenticated session.
        // Fallback chain:
        //   1. ACTIVE cert keyed by this sessionId (normal path)
        //   2. ACTIVE cert for (tenantId, userId) — another tab just rotated the master key
        //   3. REVOKED cert keyed by this sessionId — something revoked without clearing Redis;
        //      verify signature and reactivate if it matches (proves possession of master private key)
        Optional<UserCertificate> certOpt = certificateService.findBySessionId(sessionId);
        boolean needsReactivation = false;

        if (certOpt.isEmpty()) {
            Optional<UserCertificate> bySessionAny = userCertificateRepository.findBySessionId(sessionId);
            Optional<UserCertificate> byUser = certificateService.findByTenantIdAndUserId(user.getTenantId(), user.getUserId());
            log.warn("/register-session-key: no ACTIVE cert for sessionId={} (anyRowForSession={}, sessionRowStatus={}), fallback byUser.present={}",
                    sessionId,
                    bySessionAny.isPresent(),
                    bySessionAny.map(c -> c.getStatus().name()).orElse("n/a"),
                    byUser.isPresent());

            if (byUser.isPresent()) {
                certOpt = byUser;
            } else if (bySessionAny.isPresent() && bySessionAny.get().getStatus() == CertificateStatus.REVOKED) {
                // Only a REVOKED row remains for this session and no other ACTIVE cert exists.
                // Verify signature before reactivating — possession of master private key is proof
                // that this is the legitimate user, not an attacker replaying a stale session.
                certOpt = bySessionAny;
                needsReactivation = true;
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Master key not registered"));
            }
        }

        try {
            PublicKey masterPublicKey = cryptoUtil.importPublicKey(certOpt.get().getPublicKey());
            boolean valid = cryptoUtil.verifySignature(request.getSessionPublicKey(), request.getMasterSignature(), masterPublicKey);

            if (!valid) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid master signature"));
            }

            if (needsReactivation) {
                certificateService.reactivateBySessionId(sessionId);
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
