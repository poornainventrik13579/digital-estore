package com.inventrik.digitalestore.filter;

import com.inventrik.digitalestore.service.certificate.CertificateService;
import com.inventrik.digitalestore.util.CryptoUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.PublicKey;
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

    public CertificateSignatureFilter(CertificateService certificateService, CryptoUtil cryptoUtil) {
        this.certificateService = certificateService;
        this.cryptoUtil = cryptoUtil;
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

        if (!requestPath.startsWith("/api/v1/cert-auth/")) {
            chain.doFilter(request, response);
            return;
        }

        if (isPublicEndpoint(requestPath)) {
            chain.doFilter(request, response);
            return;
        }

        String challengeId = httpRequest.getHeader(HEADER_CHALLENGE);
        String signature = httpRequest.getHeader(HEADER_SIGNATURE);

        if (challengeId == null || signature == null) {
            rejectRequest(httpResponse, "Missing authentication headers");
            return;
        }

        if (performSignatureVerification(challengeId, signature)) {
            chain.doFilter(request, response);
        } else {
            rejectRequest(httpResponse, "Invalid signature");
        }
    }

    private boolean performSignatureVerification(String challengeId, String signature) {
        try {
            CertificateService.ChallengeData challengeData = certificateService.getChallenge(challengeId).orElse(null);

            if (challengeData == null || !challengeData.isValid()) {
                return false;
            }

            var sessionKeys = certificateService.getSessionKeys(challengeData.getUserId());
            if (sessionKeys.isEmpty()) {
                return false;
            }

            for (String publicKeyBase64 : sessionKeys) {
                try {
                    PublicKey publicKey = cryptoUtil.importPublicKey(publicKeyBase64);

                    if (cryptoUtil.verifySignature(challengeId, signature, publicKey)) {
                        certificateService.markChallengeUsed(challengeId);
                        return true;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
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
