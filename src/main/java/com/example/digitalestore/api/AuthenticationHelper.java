package com.example.digitalestore.api;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * This is a temporary helper class for development. 
 * In a real application, you would use proper authentication.
 */
@ControllerAdvice
public class AuthenticationHelper {

    /**
     * Provides a default username for development when authentication is not fully set up
     */
    @ModelAttribute
    public Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            // Return a mock Authentication for development
            return new Authentication() {
                @Override
                public String getName() {
                    return "system"; // Default user for development
                }

                @Override
                public boolean isAuthenticated() {
                    return true;
                }

                @Override
                public void setAuthenticated(boolean isAuthenticated) {
                    // No-op for mock
                }

                @Override
                public Object getPrincipal() {
                    return "system";
                }

                @Override
                public Object getCredentials() {
                    return null;
                }

                @Override
                public Object getDetails() {
                    return null;
                }

                @Override
                public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                    return java.util.Collections.emptyList();
                }
            };
        }
        return auth;
    }
}