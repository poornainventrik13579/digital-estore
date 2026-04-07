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
    private static final int MAX_CACHE_SIZE = 10000; // Maximum number of keys to store
    
    // Store idempotency keys and their timestamps
    private final Map<String, LocalDateTime> idempotencyKeys = new ConcurrentHashMap<>();
    
    /**
     * Register a new idempotency key.
     *
     * @param key The idempotency key
     * @return true if the key was registered successfully, false if it already exists
     */
    public boolean registerKey(String key) {
        cleanExpiredKeys();

        if (idempotencyKeys.size() >= MAX_CACHE_SIZE) {
            log.warn("Idempotency cache full, evicting expired entries");
            cleanExpiredKeys();
            if (idempotencyKeys.size() >= MAX_CACHE_SIZE) {
                log.warn("Cache still full after cleanup, evicting oldest 20% of entries");
                int toRemove = MAX_CACHE_SIZE / 5;
                idempotencyKeys.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue())
                        .limit(toRemove)
                        .map(Map.Entry::getKey)
                        .collect(java.util.stream.Collectors.toList())
                        .forEach(idempotencyKeys::remove);
            }
        }

        // Atomic: returns null if key was absent (success), non-null if already present (duplicate)
        LocalDateTime existing = idempotencyKeys.putIfAbsent(key, LocalDateTime.now());
        if (existing != null) {
            log.warn("Attempt to reuse idempotency key: {}", key);
            return false;
        }
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