package com.inventrik.digitalestore.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class IdGeneratorService {
    
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final ConcurrentHashMap<Long, Boolean> usedIds = new ConcurrentHashMap<>();
    private static final AtomicLong sequenceFallback = new AtomicLong(1);
    
    // 12-digit range: 100000000000 to 999999999999
    private static final long MIN_12_DIGIT = 100000000000L;
    private static final long MAX_12_DIGIT = 999999999999L;
    
    public Long generateId(Integer tenantId, String entityType) {
        return generateUnpredictable12DigitId();
    }
    
    public Long generateUniqueId() {
        return generateId(1, "GENERIC");
    }
    
    public Integer generateTenantId() {
        Long id = generateUnpredictable12DigitId();
        return id.intValue();
    }
    
    public Long generateUserId() {
        return generateUnpredictable12DigitId();
    }
    
    private Long generateUnpredictable12DigitId() {
        // Clear cache periodically to prevent memory growth
        if (usedIds.size() > 100000) {
            usedIds.clear();
        }
        
        Long id;
        int attempts = 0;
        final int maxAttempts = 10; // Limit collision attempts
        
        do {
            // Generate random 12-digit number
            long range = MAX_12_DIGIT - MIN_12_DIGIT + 1;
            id = MIN_12_DIGIT + Math.abs(secureRandom.nextLong() % range);
            attempts++;
            
            // Fallback to timestamped sequence if too many collisions
            if (attempts >= maxAttempts) {
                // Use timestamp + sequence for uniqueness when random fails
                long timestamp = System.currentTimeMillis() % 1000000L; // 6 digits
                long sequence = sequenceFallback.getAndIncrement() % 1000000L; // 6 digits
                id = MIN_12_DIGIT + (timestamp * 1000000L) + sequence;
                break;
            }
            
        } while (usedIds.putIfAbsent(id, true) != null);
        
        return id;
    }
    
    /**
     * Optional: Clear used IDs cache (useful for testing or memory management)
     */
    public void clearCache() {
        usedIds.clear();
    }
    
    /**
     * Get cache statistics
     */
    public int getCacheSize() {
        return usedIds.size();
    }
} 