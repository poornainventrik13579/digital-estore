package com.inventrik.digitalestore.service;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class IdGeneratorService {

    public String generateId(Integer tenantId, String entityType) {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public String generateUniqueId() {
        return generateId(1, "GENERIC");
    }

    public String generateStandardUUID() {
        return UUID.randomUUID().toString();
    }
} 