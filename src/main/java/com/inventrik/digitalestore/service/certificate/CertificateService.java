package com.inventrik.digitalestore.service.certificate;

import com.inventrik.digitalestore.domain.certificate.UserCertificate;

import java.util.Optional;

public interface CertificateService {
    // Challenge lifetime. Single source of truth for both Redis TTL (CertificateServiceImpl) and
    // in-code expiry check (ChallengeData.isExpired). 60s gives enough headroom for page fan-out
    // (e.g. /taxes + /discounts) under slow networks without widening the replay window too far.
    long CHALLENGE_TTL_MS = 60_000L;

    UserCertificate createCertificate(Integer tenantId, String userId, String sessionId, String publicKey) throws IllegalStateException;

    /**
     * Atomically rotate the master public key for (tenantId, userId):
     *   - revoke every ACTIVE cert for this user EXCEPT the one keyed by currentSessionId
     *   - upsert currentSessionId's row with the new publicKey, status=ACTIVE
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
    boolean markChallengeUsed(String challengeId);

    void storeSessionKey(String userId, String sessionPublicKey, long expiresAt);
    Optional<SessionKeyData> getSessionKey(String userId);
    void removeSessionKey(String userId);

    void createSession(String sessionId, SessionData data);
    SessionData getSession(String sessionId);
    void removeSession(String sessionId);

    class ChallengeData {
        private String userId;
        private Integer tenantId;
        private long createdAt;
        private boolean used;

        public ChallengeData() {}

        public ChallengeData(String userId, long createdAt) {
            this.userId = userId;
            this.createdAt = createdAt;
            this.used = false;
        }

        public ChallengeData(String userId, Integer tenantId, long createdAt) {
            this.userId = userId;
            this.tenantId = tenantId;
            this.createdAt = createdAt;
            this.used = false;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public Integer getTenantId() { return tenantId; }
        public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }
        public long getCreatedAt() { return createdAt; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
        public boolean isUsed() { return used; }
        public void setUsed(boolean used) { this.used = used; }
        @com.fasterxml.jackson.annotation.JsonIgnore
        public boolean isExpired() { return System.currentTimeMillis() - createdAt > CHALLENGE_TTL_MS; }
        @com.fasterxml.jackson.annotation.JsonIgnore
        public boolean isValid() { return !used && !isExpired(); }
    }

    class SessionData {
        private Integer tenantId;
        private String userId;
        private boolean authenticated;

        public SessionData() {}

        public SessionData(Integer tenantId, String userId, boolean authenticated) {
            this.tenantId = tenantId;
            this.userId = userId;
            this.authenticated = authenticated;
        }

        public Integer getTenantId() { return tenantId; }
        public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public boolean isAuthenticated() { return authenticated; }
        public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }
    }

    class SessionKeyData {
        private String sessionKeyId;
        private String publicKey;
        private long expiresAt;

        public SessionKeyData() {}

        public SessionKeyData(String publicKey, long expiresAt) {
            this.publicKey = publicKey;
            this.expiresAt = expiresAt;
        }

        public SessionKeyData(String sessionKeyId, String publicKey, long expiresAt) {
            this.sessionKeyId = sessionKeyId;
            this.publicKey = publicKey;
            this.expiresAt = expiresAt;
        }

        public String getSessionKeyId() { return sessionKeyId; }
        public void setSessionKeyId(String sessionKeyId) { this.sessionKeyId = sessionKeyId; }
        public String getPublicKey() { return publicKey; }
        public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
        public long getExpiresAt() { return expiresAt; }
        public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
        @com.fasterxml.jackson.annotation.JsonIgnore
        public boolean isExpired() { return System.currentTimeMillis() > expiresAt; }
    }
}
