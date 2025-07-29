package com.inventrik.digitalestore.service;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class IdGeneratorService {
    
    public Long generateId(Integer tenantId, String entityType) {
        // Generate unique ID using UUID to avoid collisions
        return Math.abs(UUID.randomUUID().getMostSignificantBits());
    }
    
    public Long generateUniqueId() {
        return Math.abs(UUID.randomUUID().getMostSignificantBits());
    }
} 