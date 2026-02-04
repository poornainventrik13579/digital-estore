package com.inventrik.digitalestore.service.certificate;

import com.inventrik.digitalestore.domain.certificate.UserCertificate;
import com.inventrik.digitalestore.repository.UserCertificateRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CertificateServiceImpl implements CertificateService {

    private final UserCertificateRepository repository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CHALLENGE_PREFIX = "challenge:";
    private static final String SESSION_KEY_PREFIX = "session_key:";
    private static final String SESSION_PREFIX = "cert_session:";

    private static final Duration CHALLENGE_TTL = Duration.ofSeconds(10);
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
        return repository.save(certificate);
    }

    @Override
    public Optional<UserCertificate> findBySessionId(String sessionId) {
        return repository.findBySessionId(sessionId);
    }

    @Override
    public Optional<UserCertificate> findByTenantIdAndUserId(Integer tenantId, String userId) {
        return repository.findByTenantIdAndUserId(tenantId, userId);
    }

    @Override
    @Transactional
    public void deleteBySessionId(String sessionId) {
        repository.deleteBySessionId(sessionId);
    }

    @Override
    @Transactional
    public void deleteByTenantIdAndUserId(Integer tenantId, String userId) {
        repository.deleteByTenantIdAndUserId(tenantId, userId);
    }

    @Override
    public String createChallenge(String userId) {
        String challengeId = UUID.randomUUID().toString();
        ChallengeData data = new ChallengeData(userId, System.currentTimeMillis());

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
            ChallengeData data = dataOpt.get();
            data.setUsed(true);
            redisTemplate.opsForValue().set(key, data, CHALLENGE_TTL);
            return true;
        }

        return false;
    }

    @Override
    public void storeSessionKey(String userId, String sessionPublicKey, long expiresAt) {
        String keyId = System.currentTimeMillis() + "_" + userId;
        String key = SESSION_KEY_PREFIX + userId + ":" + keyId;

        SessionKeyData data = new SessionKeyData(sessionPublicKey, expiresAt);

        long ttl = (expiresAt - System.currentTimeMillis()) / 1000;
        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, data, ttl, TimeUnit.SECONDS);
        }
    }

    @Override
    public List<String> getSessionKeys(String userId) {
        String pattern = SESSION_KEY_PREFIX + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);

        List<String> publicKeys = new java.util.ArrayList<>();
        if (keys != null) {
            for (String key : keys) {
                Object data = redisTemplate.opsForValue().get(key);
                if (data instanceof SessionKeyData) {
                    SessionKeyData sessionKey = (SessionKeyData) data;
                    if (!sessionKey.isExpired()) {
                        publicKeys.add(sessionKey.getPublicKey());
                    } else {
                        redisTemplate.delete(key);
                    }
                }
            }
        }

        return publicKeys;
    }

    @Override
    public void removeSessionKeys(String userId) {
        String pattern = SESSION_KEY_PREFIX + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public void createSession(String sessionId, SessionData data) {
        String key = SESSION_PREFIX + sessionId;
        redisTemplate.opsForValue().set(key, data, SESSION_TTL);
    }

    @Override
    public SessionData getSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        Object data = redisTemplate.opsForValue().get(key);

        if (data instanceof SessionData) {
            return (SessionData) data;
        }

        return null;
    }

    @Override
    public void removeSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        redisTemplate.delete(key);
    }
}
