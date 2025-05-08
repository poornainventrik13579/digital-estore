package com.inventrik.digitalestore.service.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing idempotency keys to prevent duplicate processing
 * of payment operations.
 */
@Service
@Slf4j
public class IdempotencyKeyService {

    private static final long EXPIRATION_MINUTES = 60; // Keys expire after 60 minutes
    
    // Store idempotency keys and their timestamps
    private final Map<String, LocalDateTime> idempotencyKeys = new ConcurrentHashMap<>();
    
    /**
     * Register a new idempotency key.
     *
     * @param key The idempotency key
     * @return true if the key was registered successfully, false if it already exists
     */
    public boolean registerKey(String key) {
        // Clean expired keys
        cleanExpiredKeys();
        
        // Check if key already exists
        if (idempotencyKeys.containsKey(key)) {
            log.warn("Attempt to reuse idempotency key: {}", key);
            return false;
        }
        
        // Register new key
        idempotencyKeys.put(key, LocalDateTime.now());
        return true;
    }
    
    /**
     * Check if a key has been registered.
     *
     * @param key The idempotency key
     * @return true if the key exists, false otherwise
     */
    public boolean isKeyRegistered(String key) {
        // Clean expired keys
        cleanExpiredKeys();
        
        return idempotencyKeys.containsKey(key);
    }
    
    /**
     * Remove a key from the registry.
     *
     * @param key The idempotency key
     */
    public void removeKey(String key) {
        idempotencyKeys.remove(key);
    }
    
    /**
     * Clean expired keys from the registry.
     */
    private void cleanExpiredKeys() {
        LocalDateTime expirationThreshold = LocalDateTime.now().minusMinutes(EXPIRATION_MINUTES);
        
        idempotencyKeys.entrySet().removeIf(entry -> entry.getValue().isBefore(expirationThreshold));
    }
}