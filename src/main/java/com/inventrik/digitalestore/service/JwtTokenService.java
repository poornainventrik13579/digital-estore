package com.inventrik.digitalestore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Centralises JWT access-token building so controllers don't duplicate
 * the claim-set construction. No DB access — pure encoding logic.
 */
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    /** Access token lifetime in seconds (15 minutes). */
    public static final int ACCESS_TOKEN_SECONDS = 900;

    private final JwtEncoder jwtEncoder;

    @Value("${app.base-url}")
    private String issuer;

    /**
     * Builds and signs a JWT access token.
     *
     * @param subject     plain username (without tenant prefix)
     * @param authorities e.g. ["ROLE_USER"], ["ROLE_TENANT"]
     * @param tenantId    null for platform admins; present for tenant/customer logins
     */
    public String buildAccessToken(String subject, List<String> authorities, Integer tenantId) {
        Instant now = Instant.now();

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ACCESS_TOKEN_SECONDS))
                .subject(subject)
                .claim("authorities", authorities);

        if (tenantId != null) {
            claims.claim("tenantId", tenantId);
        }

        return jwtEncoder.encode(JwtEncoderParameters.from(claims.build())).getTokenValue();
    }

    /**
     * Parses the tenantId from a login identifier of the form "tenantId:username".
     * Returns null for platform-admin identifiers that have no colon.
     */
    public static Integer parseTenantId(String loginIdentifier) {
        if (loginIdentifier == null || !loginIdentifier.contains(":")) return null;
        try {
            return Integer.parseInt(loginIdentifier.split(":", 2)[0]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses the plain username from a login identifier.
     * Handles both "tenantId:username" and plain "username" forms.
     */
    public static String parseUsername(String loginIdentifier) {
        if (loginIdentifier == null || !loginIdentifier.contains(":")) return loginIdentifier;
        return loginIdentifier.split(":", 2)[1];
    }
}
