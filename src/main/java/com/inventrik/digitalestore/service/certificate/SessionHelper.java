package com.inventrik.digitalestore.service.certificate;

import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionHelper {

    private final CertificateService certificateService;
    private final UserRepository userRepository;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    private static final String CERT_SESSION_COOKIE = "certSessionId";

    public String getSessionIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (CERT_SESSION_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return request.getHeader("X-Session-ID");
    }

    public Optional<User> getUserFromSession(String sessionId) {
        CertificateService.SessionData sessionData = certificateService.getSession(sessionId);
        if (sessionData != null && sessionData.isAuthenticated()) {
            return userRepository.findByTenantIdAndUserId(sessionData.getTenantId(), sessionData.getUserId());
        }
        return Optional.empty();
    }

    public ResponseCookie createSessionCookie(String sessionId, long maxAgeSeconds) {
        return ResponseCookie.from(CERT_SESSION_COOKIE, sessionId)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    public ResponseCookie clearSessionCookie() {
        return createSessionCookie("", 0);
    }

    public void performLogout(String sessionId) {
        if (sessionId == null) return;

        Optional<User> userOpt = getUserFromSession(sessionId);
        userOpt.ifPresent(user -> {
            certificateService.revokeBySessionId(sessionId);
            certificateService.removeSessionKey(user.getUserId());
        });
        certificateService.removeSession(sessionId);
        log.info("Cert-auth logout completed for sessionId: {}", sessionId);
    }
}
