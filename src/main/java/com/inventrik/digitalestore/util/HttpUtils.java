package com.inventrik.digitalestore.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class for HTTP-related operations
 */
public class HttpUtils {

    private HttpUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Extract client IP address from HTTP request, considering proxy headers
     * @param request the HTTP request
     * @return the client IP address
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 