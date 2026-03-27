package com.inventrik.digitalestore.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.domain.user.UserRole;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.certificate.CertificateService;
import com.inventrik.digitalestore.util.CryptoUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class CertificateSignatureFilter implements Filter {

    private static final String HEADER_CHALLENGE = "X-Challenge";
    private static final String HEADER_SIGNATURE = "X-Signature";

    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/api/v1/cert-auth/logout",
            "/api/v1/cert-auth/check-session",
            "/api/v1/cert-auth/register-key",
            "/api/v1/cert-auth/challenge",
            "/api/v1/cert-auth/register-session-key"
    );

    private final CertificateService certificateService;
    private final CryptoUtil cryptoUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public CertificateSignatureFilter(CertificateService certificateService, CryptoUtil cryptoUtil,
                                       UserRepository userRepository, ObjectMapper objectMapper) {
        this.certificateService = certificateService;
        this.cryptoUtil = cryptoUtil;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestPath = httpRequest.getRequestURI();
        String requestMethod = httpRequest.getMethod();

        if ("OPTIONS".equalsIgnoreCase(requestMethod)) {
            chain.doFilter(request, response);
            return;
        }

        if (isPublicEndpoint(requestPath)) {
            chain.doFilter(request, response);
            return;
        }

        String challengeId = httpRequest.getHeader(HEADER_CHALLENGE);
        String signature = httpRequest.getHeader(HEADER_SIGNATURE);

        if (challengeId != null && signature != null) {
            String sessionId = getSessionIdFromCookie(httpRequest);
            String userId = performSignatureVerification(challengeId, signature, sessionId);
            if (userId != null) {
                chain.doFilter(request, response);
            } else {
                rejectRequest(httpResponse, "Invalid signature");
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private String getSessionIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("certSessionId".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // Fallback to X-Session-ID header for Safari ITP (blocks cross-origin cookies)
        return request.getHeader("X-Session-ID");
    }

    private String performSignatureVerification(String challengeId, String signature, String sessionId) {
        try {
            log.info("Certificate signature verification started - challengeId: {}", challengeId);

            // Reject immediately if session cookie is missing or session is no longer valid (e.g. after logout)
            if (sessionId == null || certificateService.getSession(sessionId) == null) {
                log.warn("Session invalid or missing for challengeId: {}", challengeId);
                return null;
            }

            // Get challenge from Redis
            CertificateService.ChallengeData challengeData = certificateService.getChallenge(challengeId).orElse(null);

            if (challengeData == null) {
                log.warn("Challenge not found in Redis: {}", challengeId);
                return null;
            }

            if (!challengeData.isValid()) {
                log.warn("Challenge invalid - used: {}, expired: {}", challengeData.isUsed(), challengeData.isExpired());
                return null;
            }

            log.info("Challenge valid - userId: {}, tenantId: {}", challengeData.getUserId(), challengeData.getTenantId());

            // Get user's session key from Redis
            var sessionKeyOpt = certificateService.getSessionKey(challengeData.getUserId());
            if (sessionKeyOpt.isEmpty()) {
                log.warn("Session key not found for userId: {}", challengeData.getUserId());
                return null;
            }

            CertificateService.SessionKeyData sessionKey = sessionKeyOpt.get();
            log.info("Session key found for userId: {}", challengeData.getUserId());

            try {
                PublicKey publicKey = cryptoUtil.importPublicKey(sessionKey.getPublicKey());
                log.info("Public key imported");

                boolean signatureValid = cryptoUtil.verifySignature(challengeId, signature, publicKey);
                log.info("Signature valid: {}", signatureValid);

                if (signatureValid) {
                    certificateService.markChallengeUsed(challengeId);
                    String userId = challengeData.getUserId();
                    Integer tenantId = challengeData.getTenantId();

                    // Fetch user from DB to get proper role/authorities
                    var userOpt = userRepository.findByTenantIdAndUserId(tenantId, userId);

                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        String status = user.getStatus();
                        String username = user.getUsername();
                        log.info("User found - username: {}, status: {}, role: {}", username, status, user.getUserRole());

                        if ("0".equals(status)) { // Active user
                            switch (user.getUserRole()) {
                                case ADMIN:
                                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                                    break;
                                case TENANT:
                                    authorities.add(new SimpleGrantedAuthority("ROLE_TENANT"));
                                    break;
                                case USER:
                                default:
                                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                                    break;
                            }
                        } else {
                            log.warn("User not active - status: {}", status);
                        }
                    } else {
                        log.warn("User not found in DB - tenantId: {}, userId: {}", tenantId, userId);
                    }

                    // If no authorities found, deny access
                    if (authorities.isEmpty()) {
                        log.warn("No authorities found, denying access");
                        return null;
                    }

                    // Use username as principal (not userId) so controllers can findByUsername()
                    String username = userOpt.get().getUsername();
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("Certificate auth successful for username: {}", username);

                    return userId;
                } else {
                    log.warn("Signature verification failed for challengeId: {}", challengeId);
                    return null;
                }
            } catch (Exception e) {
                log.error("Exception during signature verification: {}", e.getMessage(), e);
                return null;
            }
        } catch (Exception e) {
            log.error("Exception in performSignatureVerification: {}", e.getMessage(), e);
            return null;
        }
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.contains(path);
    }

    private void rejectRequest(HttpServletResponse response, String reason) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("error", reason)));
    }
}