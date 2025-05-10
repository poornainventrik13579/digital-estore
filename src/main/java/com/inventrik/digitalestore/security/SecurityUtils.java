package com.inventrik.digitalestore.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public static boolean isAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() != null && 
               SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
    }
    
    public static String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    
    public static boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
    
    public static boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
}