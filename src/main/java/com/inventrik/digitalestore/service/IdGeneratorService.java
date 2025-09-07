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
        
        if (usedIds.size() > 100000) {
            usedIds.clear();
        }
        
        Long id;
        int attempts = 0;
        final int maxAttempts = 10; 
        
        do {
            
            long range = MAX_12_DIGIT - MIN_12_DIGIT + 1;
            id = MIN_12_DIGIT + Math.abs(secureRandom.nextLong() % range);
            attempts++;
            
            if (attempts >= maxAttempts) {
                
                long timestamp = System.currentTimeMillis() % 1000000L; 
                long sequence = sequenceFallback.getAndIncrement() % 1000000L; 
                id = MIN_12_DIGIT + (timestamp * 1000000L) + sequence;
                break;
            }
            
        } while (usedIds.putIfAbsent(id, true) != null);
        
        return id;
    }
    
    public void clearCache() {
        usedIds.clear();
    }
    
    public int getCacheSize() {
        return usedIds.size();
    }
} 