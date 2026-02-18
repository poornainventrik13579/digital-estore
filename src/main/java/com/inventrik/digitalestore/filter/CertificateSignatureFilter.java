package com.inventrik.digitalestore.filter;

import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.domain.user.UserRole;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.certificate.CertificateService;
import com.inventrik.digitalestore.util.CryptoUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
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

    public CertificateSignatureFilter(CertificateService certificateService, CryptoUtil cryptoUtil, UserRepository userRepository) {
        this.certificateService = certificateService;
        this.cryptoUtil = cryptoUtil;
        this.userRepository = userRepository;
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
            String userId = performSignatureVerification(challengeId, signature);
            if (userId != null) {
                chain.doFilter(request, response);
            } else {
                rejectRequest(httpResponse, "Invalid signature");
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private String performSignatureVerification(String challengeId, String signature) {
        try {
            // Get challenge from Redis
            CertificateService.ChallengeData challengeData = certificateService.getChallenge(challengeId).orElse(null);

            if (challengeData == null || !challengeData.isValid()) {
                return null;
            }

            // Get user's session key from Redis
            var sessionKeyOpt = certificateService.getSessionKey(challengeData.getUserId());
            if (sessionKeyOpt.isEmpty()) {
                return null;
            }

            CertificateService.SessionKeyData sessionKey = sessionKeyOpt.get();

            try {
                PublicKey publicKey = cryptoUtil.importPublicKey(sessionKey.getPublicKey());

                boolean signatureValid = cryptoUtil.verifySignature(challengeId, signature, publicKey);

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
                        }
                    }

                    // If no authorities found, deny access
                    if (authorities.isEmpty()) {
                        return null;
                    }

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        authorities
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    return userId;
                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.contains(path);
    }

    private void rejectRequest(HttpServletResponse response, String reason) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + reason + "\"}");
    }
}