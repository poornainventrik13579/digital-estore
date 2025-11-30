package com.inventrik.digitalestore.util;

import org.springframework.stereotype.Component;

@Component
public class AuditHelper {

    /**
     * Generates a 2-character audit code from username
     * System users get special codes:
     * - "system" → "00"
     * - "webhook" → "99"
     * - Regular users → 2-digit code based on user ID (01-98)
     * - Unknown users → hash-based code (10-98)
     */
    public String getAuditCode(String username, Long userId) {
        if (username == null || username.equals("system")) {
            return "00";
        }
        if (username.equals("webhook")) {
            return "99";
        }
        if (userId != null) {
            return String.format("%02d", userId % 98 + 1);
        }
        // Fallback to hash-based code if userId not available
        int hash = Math.abs(username.hashCode()) % 89 + 10;
        return String.format("%02d", hash);
    }

    /**
     * Generates audit code from username only (without userId)
     * Used when userId is not available
     */
    public String getAuditCode(String username) {
        return getAuditCode(username, null);
    }
}
