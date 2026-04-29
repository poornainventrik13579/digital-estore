package com.inventrik.digitalestore.service.certificate;

import com.inventrik.digitalestore.domain.certificate.UserCertificate;

import java.util.Optional;

public interface CertificateService {
    // Challenge lifetime. Single source of truth for both Redis TTL
    // (CertificateServiceImpl) and
    // in-code expiry check (ChallengeData.isExpired). 60s gives enough headroom for
    // page fan-out
    // (e.g. /taxes + /discounts) under slow networks without widening the replay
    // window too far.
    long CHALLENGE_TTL_MS = 60_000L;

    UserCertificate createCertificate(Integer tenantId, String userId, String sessionId, String publicKey)
            throws IllegalStateException;

    /**
     * Atomically rotate the master public key for (tenantId, userId):
     * - revoke every ACTIVE cert for this user EXCEPT the one keyed by
     * currentSessionId
     * - upsert currentSessionId's row with the new publicKey, status=ACTIVE
     * Runs in a single transaction so concurrent reads never see a REVOKED state
     * on the current session's row.
     */
    UserCertificate rotateMasterKey(Integer tenantId, String userId, String currentSessionId, String publicKey);

    Optional<UserCertificate> findBySessionId(String sessionId);

    Optional<UserCertificate> findByTenantIdAndUserId(Integer tenantId, String userId);

    void revokeBySessionId(String sessionId);

    void revokeByTenantIdAndUserId(Integer tenantId, String userId);

    boolean reactivateBySessionId(String sessionId);

    String createChallenge(String userId, Integer tenantId);

    Optional<ChallengeData> getChallenge(String challengeId);

    void storeSessionKey(String userId, String sessionPublicKey, long expiresAt);

    Optional<SessionKeyData> getSessionKey(String userId);
    Optional<SessionKeyData> getSessionKeyById(String sessionKeyId);

    void removeSessionKey(String userId);

    void createSession(String sessionId, SessionData data);

    SessionData getSession(String sessionId);

    void removeSession(String sessionId);

    @lombok.Getter @lombok.Setter @lombok.NoArgsConstructor
    class ChallengeData {
        private String userId;
        private Integer tenantId;
        private long createdAt;
        private boolean used;
        private String sessionKeyId;

        public ChallengeData(String userId, long createdAt) {
            this.userId = userId;
            this.createdAt = createdAt;
        }

        public ChallengeData(String userId, Integer tenantId, long createdAt, String sessionKeyId) {
            this.userId = userId;
            this.tenantId = tenantId;
            this.createdAt = createdAt;
            this.sessionKeyId = sessionKeyId;
        }

        @com.fasterxml.jackson.annotation.JsonIgnore
        public boolean isExpired() { return System.currentTimeMillis() - createdAt > CHALLENGE_TTL_MS; }
        @com.fasterxml.jackson.annotation.JsonIgnore
        public boolean isValid() { return !used && !isExpired(); }
    }

    @lombok.Getter @lombok.Setter @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    class SessionData {
        private Integer tenantId;
        private String userId;
        private boolean authenticated;
    }

    @lombok.Getter @lombok.Setter @lombok.NoArgsConstructor
    class SessionKeyData {
        private String sessionKeyId;
        private String publicKey;
        private long expiresAt;

        public SessionKeyData(String sessionKeyId, String publicKey, long expiresAt) {
            this.sessionKeyId = sessionKeyId;
            this.publicKey = publicKey;
            this.expiresAt = expiresAt;
        }

        @com.fasterxml.jackson.annotation.JsonIgnore
        public boolean isExpired() { return System.currentTimeMillis() > expiresAt; }
    }
}
