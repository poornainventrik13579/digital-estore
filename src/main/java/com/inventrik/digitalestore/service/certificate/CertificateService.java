package com.inventrik.digitalestore.service.certificate;

import com.inventrik.digitalestore.domain.certificate.UserCertificate;

import java.util.Optional;

public interface CertificateService {
    UserCertificate createCertificate(Integer tenantId, String userId, String sessionId, String publicKey) throws IllegalStateException;
    Optional<UserCertificate> findBySessionId(String sessionId);
    Optional<UserCertificate> findByTenantIdAndUserId(Integer tenantId, String userId);
    void deleteBySessionId(String sessionId);
    void deleteByTenantIdAndUserId(Integer tenantId, String userId);

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
        public boolean isExpired() { return System.currentTimeMillis() - createdAt > 30000; }
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
        public boolean isExpired() { return System.currentTimeMillis() > expiresAt; }
    }
}
