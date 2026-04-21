package com.inventrik.digitalestore.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory JWT denylist keyed by JTI. Lazy eviction: entries are removed when
 * looked up past their exp. Single-instance only.
 */
@Service
public class JwtDenylistService {

    private final ConcurrentHashMap<String, Long> denylist = new ConcurrentHashMap<>();

    public void add(String jti, Instant expiresAt) {
        if (jti == null || expiresAt == null) return;
        denylist.put(jti, expiresAt.getEpochSecond());
    }

    public boolean isDenied(String jti) {
        if (jti == null) return false;
        Long exp = denylist.get(jti);
        if (exp == null) return false;
        if (exp < Instant.now().getEpochSecond()) {
            denylist.remove(jti, exp);
            return false;
        }
        return true;
    }
}
