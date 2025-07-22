package com.inventrik.digitalestore.service;

import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdGeneratorService {
    
    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
    
    public Long generateId(Integer tenantId, String entityType) {
        String key = tenantId + ":" + entityType;
        long baseTime = System.currentTimeMillis();
        
        return counters.computeIfAbsent(key, k -> new AtomicLong(baseTime))
                      .incrementAndGet();
    }
    
    public Long generateUniqueId() {
        return Math.abs(UUID.randomUUID().getMostSignificantBits());
    }
} 