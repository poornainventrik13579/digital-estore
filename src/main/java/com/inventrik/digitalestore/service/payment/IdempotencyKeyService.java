package com.inventrik.digitalestore.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class IdempotencyKeyService {

    private static final Duration EXPIRATION = Duration.ofHours(1);
    private static final String KEY_PREFIX = "idempotency:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public boolean registerKey(String key) {
        String redisKey = KEY_PREFIX + key;
        
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            log.warn("Attempt to reuse idempotency key: {}", key);
            return false;
        }
        
        redisTemplate.opsForValue().set(redisKey, LocalDateTime.now().toString(), EXPIRATION);
        return true;
    }
    
    public boolean isKeyRegistered(String key) {
        String redisKey = KEY_PREFIX + key;
        return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
    }
    
    public void removeKey(String key) {
        String redisKey = KEY_PREFIX + key;
        redisTemplate.delete(redisKey);
    }
}