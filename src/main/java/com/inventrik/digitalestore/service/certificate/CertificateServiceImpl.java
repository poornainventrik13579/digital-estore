package com.inventrik.digitalestore.service.certificate;

import com.inventrik.digitalestore.domain.certificate.CertificateStatus;
import com.inventrik.digitalestore.domain.certificate.UserCertificate;
import com.inventrik.digitalestore.repository.UserCertificateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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

    private static final Duration CHALLENGE_TTL = Duration.ofSeconds(30);
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
        return repository.save(certificate);
    }

    @Override
    public Optional<UserCertificate> findBySessionId(String sessionId) {
        return repository.findBySessionIdAndStatus(sessionId, CertificateStatus.ACTIVE);
    }

    @Override
    public Optional<UserCertificate> findByTenantIdAndUserId(Integer tenantId, String userId) {
        return repository.findByTenantIdAndUserIdAndStatus(tenantId, userId, CertificateStatus.ACTIVE);
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
    @Deprecated
    @Transactional
    public void deleteBySessionId(String sessionId) {
        // Delegate to soft-delete for backward compatibility
        revokeBySessionId(sessionId);
    }

    @Override
    @Deprecated
    @Transactional
    public void deleteByTenantIdAndUserId(Integer tenantId, String userId) {
        // Delegate to soft-delete for backward compatibility
        revokeByTenantIdAndUserId(tenantId, userId);
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

        return Optional.empty();
    }

    @Override
    public boolean markChallengeUsed(String challengeId) {
        String key = CHALLENGE_PREFIX + challengeId;
        Optional<ChallengeData> dataOpt = getChallenge(challengeId);

        if (dataOpt.isPresent()) {
            // Delete immediately — the challenge is single-use and no longer needed.
            // Previously this re-saved with a new 30s TTL, which was wasteful.
            redisTemplate.delete(key);
            return true;
        }

        return false;
    }

    @Override
    public void storeSessionKey(String userId, String sessionPublicKey, long expiresAt) {
        String sessionKeyId = UUID.randomUUID().toString();
        String key = CERT_SESSION_KEY_PREFIX + sessionKeyId;

        SessionKeyData data = new SessionKeyData(sessionKeyId, sessionPublicKey, expiresAt);

        long ttl = (expiresAt - System.currentTimeMillis()) / 1000;
        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, data, ttl, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(USER_CURRENT_SESSION_PREFIX + userId, sessionKeyId, SESSION_TTL);
        }
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

        return null;
    }

    @Override
    public void removeSession(String sessionId) {
        String key = CERT_LOGIN_SESSION_PREFIX + sessionId;
        redisTemplate.delete(key);
    }
}
