package com.inventrik.digitalestore.service.certificate;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;

@Slf4j
@Service
public class CertificateServiceImpl implements CertificateService {

    private final UserCertificateRepository repository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CHALLENGE_PREFIX = "challenge:";
    private static final String CERT_SESSION_KEY_PREFIX = "session_key:"; // Stores ECDSA session key data
    private static final String CERT_LOGIN_SESSION_PREFIX = "cert_session:"; // Stores login session (certSessionId)
    private static final String USER_CURRENT_SESSION_PREFIX = "user_session_key:"; // Maps userId → active sessionKeyId
    private static final String USER_CURRENT_SESSION_EXPIRES_PREFIX = "user_session_key_expires:"; // Tracks expiresAt for atomic pointer updates

    private static final Duration CHALLENGE_TTL = Duration.ofMillis(CHALLENGE_TTL_MS);
    private static final Duration SESSION_TTL = Duration.ofDays(30);

    // When a session key is superseded we don't let it keep its full lifetime — it only needs
    // to outlive any challenge already pinned to it (CHALLENGE_TTL). This grace covers that
    // window with margin, then the stale key self-expires instead of lingering ~4h.
    private static final long SUPERSEDED_KEY_GRACE_SECONDS = 120L;

    public CertificateServiceImpl(UserCertificateRepository repository,
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
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
        // Revoke OTHER sessions' certs first — current session's row is never REVOKED
        // in this txn.
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
        return repository
                .findFirstByTenantIdAndUserIdAndStatusOrderByUpdatedDesc(tenantId, userId, CertificateStatus.ACTIVE)
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
        int updated = repository.revokeByTenantIdAndUserId(tenantId, userId, CertificateStatus.REVOKED,
                CertificateStatus.ACTIVE);
        log.info("Revoked {} certificate(s) for tenantId: {}, userId: {}", updated, tenantId, userId);
    }

    @Override
    public String createChallenge(String userId, Integer tenantId) {
        String challengeId = UUID.randomUUID().toString();
        String sessionKeyId = (String) redisTemplate.opsForValue().get(USER_CURRENT_SESSION_PREFIX + userId);
        ChallengeData data = new ChallengeData(userId, tenantId, System.currentTimeMillis(), sessionKeyId);

        String key = CHALLENGE_PREFIX + challengeId;
        redisTemplate.opsForValue().set(key, data, CHALLENGE_TTL);

        return challengeId;
    }

    @Override
    public Optional<ChallengeData> getChallenge(String challengeId) {
        String key = CHALLENGE_PREFIX + challengeId;
        Object data = redisTemplate.opsForValue().get(key);

        if (data == null) {
            log.warn("getChallenge: key '{}' not present in Redis (expired or never written)", key);
            return Optional.empty();
        }

        if (data instanceof ChallengeData) {
            return Optional.of((ChallengeData) data);
        }

        if (data instanceof java.util.Map) {
            // Stale entry from prior serializer config (no Jackson type info). Use
            // ObjectMapper
            // to coerce field names/numeric types correctly, then rewrite with proper type
            // info.
            try {
                ChallengeData restored = objectMapper.convertValue(data, ChallengeData.class);
                if (restored.getUserId() != null && restored.getCreatedAt() > 0L) {
                    redisTemplate.opsForValue().set(key, restored, CHALLENGE_TTL);
                    log.info("getChallenge: rewrote stale Map entry as ChallengeData for key '{}'", key);
                    return Optional.of(restored);
                }
                log.warn("getChallenge: recovered Map missing required fields for key '{}': raw={}", key, data);
            } catch (Exception e) {
                log.warn("getChallenge: failed to recover Map entry for key '{}', raw={}", key, data, e);
            }
            return Optional.empty();
        }

        log.warn("getChallenge: unexpected type {} for key '{}'", data.getClass().getName(), key);
        return Optional.empty();
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
        final long finalTtl = Math.min(ttl, 24 * 3600L);

        // Write session key data unconditionally — each key ID is unique, no conflict possible
        redisTemplate.opsForValue().set(key, data, finalTtl, TimeUnit.SECONDS);

        // Update the user→sessionKeyId pointer atomically using WATCH-MULTI-EXEC.
        // Rapid page refreshes each register a new session key concurrently; we must
        // ensure the pointer always points to the NEWEST key (highest expiresAt).
        // Without atomicity, an older registration's second write can race ahead and
        // overwrite the newer pointer — causing the next challenge to pin a stale key.
        final String ptrKey = USER_CURRENT_SESSION_PREFIX + userId;
        final String expKey = USER_CURRENT_SESSION_EXPIRES_PREFIX + userId;
        final String finalSessionKeyId = sessionKeyId;
        final long finalExpiresAt = expiresAt;

        for (int attempt = 0; attempt < 3; attempt++) {
            List<Object> txResult = redisTemplate.execute(new SessionCallback<List<Object>>() {
                @Override
                @SuppressWarnings("unchecked")
                public List<Object> execute(RedisOperations operations) throws DataAccessException {
                    operations.watch(expKey);
                    Object storedExpObj = operations.opsForValue().get(expKey);
                    long storedExp = storedExpObj instanceof Number
                            ? ((Number) storedExpObj).longValue() : 0L;
                    if (finalExpiresAt < storedExp) {
                        // A newer registration already owns the pointer — don't overwrite
                        operations.unwatch();
                        return Collections.emptyList();
                    }
                    // Key we're replacing. ptrKey is only ever rewritten together with expKey inside
                    // this same MULTI, so the WATCH on expKey already guards this read for staleness.
                    String supersededKeyId = (String) operations.opsForValue().get(ptrKey);
                    operations.multi();
                    operations.opsForValue().set(ptrKey, finalSessionKeyId, finalTtl, TimeUnit.SECONDS);
                    operations.opsForValue().set(expKey, finalExpiresAt, finalTtl, TimeUnit.SECONDS);
                    if (supersededKeyId != null && !supersededKeyId.equals(finalSessionKeyId)) {
                        // Invalidate the old key promptly instead of leaving it valid for ~4h.
                        operations.expire(CERT_SESSION_KEY_PREFIX + supersededKeyId,
                                SUPERSEDED_KEY_GRACE_SECONDS, TimeUnit.SECONDS);
                    }
                    return operations.exec(); // null = WATCH triggered (concurrent write), retry
                }
            });
            if (txResult != null) break; // empty = no-op (older key), non-empty = success
            log.debug("storeSessionKey: WATCH triggered for userId {}, retrying ({}/3)", userId, attempt + 1);
        }
    }

    @Override
    public Optional<SessionKeyData> getSessionKey(String userId) {
        String sessionKeyId = (String) redisTemplate.opsForValue().get(USER_CURRENT_SESSION_PREFIX + userId);
        return getSessionKeyById(sessionKeyId);
    }

    @Override
    public Optional<SessionKeyData> getSessionKeyById(String sessionKeyId) {
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
        redisTemplate.delete(USER_CURRENT_SESSION_EXPIRES_PREFIX + userId);
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
            // Stale entry from prior serializer config (no Jackson type info). Use
            // ObjectMapper
            // to coerce field names/numeric types correctly, then rewrite with proper type
            // info.
            try {
                SessionData restored = objectMapper.convertValue(data, SessionData.class);
                if (restored.getUserId() != null && restored.getTenantId() != null) {
                    redisTemplate.opsForValue().set(key, restored, SESSION_TTL);
                    log.info("getSession: rewrote stale Map entry as SessionData for key '{}'", key);
                    return restored;
                }
                log.warn("getSession: recovered Map missing required fields for key '{}': raw={}", key, data);
            } catch (Exception e) {
                log.warn("getSession: failed to recover Map entry for key '{}', raw={}", key, data, e);
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
