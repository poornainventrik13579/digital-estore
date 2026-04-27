package com.inventrik.digitalestore.service.certificate;

import com.inventrik.digitalestore.domain.certificate.CertificateStatus;
import com.inventrik.digitalestore.domain.certificate.UserCertificate;
import com.inventrik.digitalestore.repository.UserCertificateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CertificateServiceImpl implements CertificateService {

    private final UserCertificateRepository repository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CHALLENGE_PREFIX = "challenge:";
    private static final String CERT_SESSION_KEY_PREFIX = "session_key:";      // Stores ECDSA session key data
    private static final String CERT_LOGIN_SESSION_PREFIX = "cert_session:";  // Stores login session (certSessionId)
    private static final String USER_CURRENT_SESSION_PREFIX = "user_session_key:";  // Maps userId → active sessionKeyId

    private static final Duration CHALLENGE_TTL = Duration.ofMillis(CHALLENGE_TTL_MS);
    private static final Duration SESSION_TTL = Duration.ofDays(30);

    public CertificateServiceImpl(UserCertificateRepository repository, RedisTemplate<String, Object> redisTemplate) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public UserCertificate createCertificate(Integer tenantId, String userId, String sessionId, String publicKey) {
        if (findBySessionId(sessionId).isPresent()) {
            throw new IllegalStateException("Certificate already exists for session: " + sessionId);
        }

        UserCertificate certificate = new UserCertificate();
        certificate.setSessionId(sessionId);
        certificate.setTenantId(tenantId);
        certificate.setUserId(userId);
        certificate.setPublicKey(publicKey);
        certificate.setStatus(CertificateStatus.ACTIVE);
        certificate.setCreated(LocalDateTime.now());
        certificate.setUpdated(LocalDateTime.now());
        return repository.save(certificate);
    }

    @Override
    @Transactional
    public UserCertificate rotateMasterKey(Integer tenantId, String userId, String currentSessionId, String publicKey) {
        // Revoke OTHER sessions' certs first — current session's row is never REVOKED in this txn.
        repository.revokeByTenantIdAndUserIdExcludingSession(
                tenantId, userId, currentSessionId, CertificateStatus.REVOKED, CertificateStatus.ACTIVE);

        UserCertificate cert = repository.findBySessionId(currentSessionId).orElseGet(UserCertificate::new);
        boolean isNew = cert.getCreated() == null;
        cert.setSessionId(currentSessionId);
        cert.setTenantId(tenantId);
        cert.setUserId(userId);
        cert.setPublicKey(publicKey);
        cert.setStatus(CertificateStatus.ACTIVE);
        if (isNew) {
            cert.setCreated(LocalDateTime.now());
        }
        cert.setUpdated(LocalDateTime.now());
        return repository.save(cert);
    }

    @Value("${app.cert.master-key-expiry-days:180}")
    private int masterKeyExpiryDays;

    private boolean isCertExpired(UserCertificate cert) {
        return cert.getCreated().plusDays(masterKeyExpiryDays).isBefore(LocalDateTime.now());
    }

    @Override
    public Optional<UserCertificate> findBySessionId(String sessionId) {
        return repository.findBySessionIdAndStatus(sessionId, CertificateStatus.ACTIVE)
                .filter(cert -> !isCertExpired(cert));
    }

    @Override
    public Optional<UserCertificate> findByTenantIdAndUserId(Integer tenantId, String userId) {
        return repository.findByTenantIdAndUserIdAndStatus(tenantId, userId, CertificateStatus.ACTIVE)
                .filter(cert -> !isCertExpired(cert));
    }

    @Override
    @Transactional
    public boolean reactivateBySessionId(String sessionId) {
        int updated = repository.reactivateBySessionId(sessionId, CertificateStatus.ACTIVE, CertificateStatus.REVOKED);
        if (updated > 0) {
            log.info("Reactivated cert for sessionId: {} (was REVOKED, master signature verified)", sessionId);
        }
        return updated > 0;
    }

    @Override
    @Transactional
    public void revokeBySessionId(String sessionId) {
        int updated = repository.revokeBySessionId(sessionId, CertificateStatus.REVOKED, CertificateStatus.ACTIVE);
        log.info("Revoked {} certificate(s) for sessionId: {}", updated, sessionId);
    }

    @Override
    @Transactional
    public void revokeByTenantIdAndUserId(Integer tenantId, String userId) {
        int updated = repository.revokeByTenantIdAndUserId(tenantId, userId, CertificateStatus.REVOKED, CertificateStatus.ACTIVE);
        log.info("Revoked {} certificate(s) for tenantId: {}, userId: {}", updated, tenantId, userId);
    }

    @Override
    public String createChallenge(String userId, Integer tenantId) {
        String challengeId = UUID.randomUUID().toString();
        ChallengeData data = new ChallengeData(userId, tenantId, System.currentTimeMillis());

        String key = CHALLENGE_PREFIX + challengeId;
        redisTemplate.opsForValue().set(key, data, CHALLENGE_TTL);

        return challengeId;
    }

    @Override
    public Optional<ChallengeData> getChallenge(String challengeId) {
        String key = CHALLENGE_PREFIX + challengeId;
        Object data = redisTemplate.opsForValue().get(key);

        if (data instanceof ChallengeData) {
            return Optional.of((ChallengeData) data);
        }

        if (data instanceof java.util.Map) {
            // Stale entry serialized without Jackson type info — reconstruct manually and rewrite
            try {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) data;
                Object userIdObj = map.get("userId");
                Object tenantIdObj = map.get("tenantId");
                Object createdAtObj = map.get("createdAt");
                Object usedObj = map.get("used");

                Integer tenantId = tenantIdObj instanceof Integer ? (Integer) tenantIdObj : null;
                long createdAt = createdAtObj instanceof Number ? ((Number) createdAtObj).longValue() : 0L;
                boolean used = usedObj instanceof Boolean && (Boolean) usedObj;

                if (userIdObj instanceof String && createdAt > 0L) {
                    ChallengeData restored = new ChallengeData((String) userIdObj, tenantId, createdAt);
                    restored.setUsed(used);
                    redisTemplate.opsForValue().set(key, restored, CHALLENGE_TTL);
                    log.info("getChallenge: rewrote stale Map entry as ChallengeData for key '{}'", key);
                    return Optional.of(restored);
                }
            } catch (Exception e) {
                log.warn("getChallenge: failed to recover Map entry for key '{}'", key, e);
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean markChallengeUsed(String challengeId) {
        String key = CHALLENGE_PREFIX + challengeId;
        // Atomic get-and-delete prevents TOCTOU: two concurrent requests cannot both pass with the same challenge
        Object data = redisTemplate.opsForValue().getAndDelete(key);
        return data instanceof ChallengeData;
    }

    @Override
    public void storeSessionKey(String userId, String sessionPublicKey, long expiresAt) {
        String sessionKeyId = UUID.randomUUID().toString();
        String key = CERT_SESSION_KEY_PREFIX + sessionKeyId;

        SessionKeyData data = new SessionKeyData(sessionKeyId, sessionPublicKey, expiresAt);

        long ttl = (expiresAt - System.currentTimeMillis()) / 1000;
        if (ttl <= 0) {
            throw new IllegalArgumentException("expiresAt is in the past or invalid");
        }
        ttl = Math.min(ttl, 24 * 3600L);
        redisTemplate.opsForValue().set(key, data, ttl, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(USER_CURRENT_SESSION_PREFIX + userId, sessionKeyId, ttl, TimeUnit.SECONDS);
    }

    @Override
    public Optional<SessionKeyData> getSessionKey(String userId) {
        String sessionKeyId = (String) redisTemplate.opsForValue().get(USER_CURRENT_SESSION_PREFIX + userId);

        if (sessionKeyId == null) {
            return Optional.empty();
        }

        String key = CERT_SESSION_KEY_PREFIX + sessionKeyId;
        Object data = redisTemplate.opsForValue().get(key);

        if (data instanceof SessionKeyData) {
            SessionKeyData sessionKey = (SessionKeyData) data;
            if (!sessionKey.isExpired()) {
                return Optional.of(sessionKey);
            } else {
                redisTemplate.delete(key);
            }
        }

        return Optional.empty();
    }

    @Override
    public void removeSessionKey(String userId) {
        String sessionKeyId = (String) redisTemplate.opsForValue().get(USER_CURRENT_SESSION_PREFIX + userId);

        if (sessionKeyId != null) {
            String key = CERT_SESSION_KEY_PREFIX + sessionKeyId;
            redisTemplate.delete(key);
        }

        redisTemplate.delete(USER_CURRENT_SESSION_PREFIX + userId);
    }

    @Override
    public void createSession(String sessionId, SessionData data) {
        String key = CERT_LOGIN_SESSION_PREFIX + sessionId;
        redisTemplate.opsForValue().set(key, data, SESSION_TTL);
    }

    @Override
    public SessionData getSession(String sessionId) {
        String key = CERT_LOGIN_SESSION_PREFIX + sessionId;
        Object data = redisTemplate.opsForValue().get(key);

        if (data instanceof SessionData) {
            return (SessionData) data;
        }

        if (data instanceof java.util.Map) {
            // Stale entry serialized without Jackson type info — reconstruct manually and rewrite
            try {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) data;
                Object tenantIdObj = map.get("tenantId");
                Object userIdObj = map.get("userId");
                Object authObj = map.get("authenticated");
                if (tenantIdObj instanceof Integer && userIdObj instanceof String && authObj instanceof Boolean) {
                    SessionData restored = new SessionData((Integer) tenantIdObj, (String) userIdObj, (Boolean) authObj);
                    redisTemplate.opsForValue().set(key, restored, SESSION_TTL);
                    log.info("getSession: rewrote stale Map entry as SessionData for key '{}'", key);
                    return restored;
                }
            } catch (Exception e) {
                log.warn("getSession: failed to recover Map entry for key '{}'", key, e);
            }
        }

        if (data != null) {
            log.warn("getSession: Redis key '{}' holds unexpected type {} — treating as miss",
                    key, data.getClass().getName());
        }

        Optional<UserCertificate> certOpt = repository.findBySessionIdAndStatus(sessionId, CertificateStatus.ACTIVE);
        if (certOpt.isEmpty()) {
            Optional<UserCertificate> any = repository.findBySessionId(sessionId);
            log.warn("getSession: no ACTIVE cert for sessionId={} (anyRowPresent={}, status={})",
                    sessionId, any.isPresent(), any.map(c -> c.getStatus().name()).orElse("n/a"));
            return null;
        }
        UserCertificate cert = certOpt.get();
        if (isCertExpired(cert)) {
            log.warn("getSession: cert expired for sessionId={} (created={}, expiryDays={})",
                    sessionId, cert.getCreated(), masterKeyExpiryDays);
            return null;
        }
        SessionData restored = new SessionData(cert.getTenantId(), cert.getUserId(), true);
        redisTemplate.opsForValue().set(key, restored, SESSION_TTL);
        log.info("Session restored from MySQL to Redis for sessionId: {}", sessionId);
        return restored;
    }

    @Override
    public void removeSession(String sessionId) {
        String key = CERT_LOGIN_SESSION_PREFIX + sessionId;
        redisTemplate.delete(key);
    }
}
