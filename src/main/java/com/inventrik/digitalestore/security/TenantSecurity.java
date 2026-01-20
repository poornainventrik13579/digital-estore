package com.inventrik.digitalestore.security;

import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.TenantRepository;
import com.inventrik.digitalestore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TenantSecurity {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public boolean hasTenantAccess(Authentication authentication, Integer tenantId) {
        if (authentication == null) {
            return false;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN"));

        if (isAdmin) {
            return tenantRepository.existsByTenantId(tenantId);
        }

        boolean isTenant = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_TENANT"));

        if (isTenant) {
            String username = authentication.getName();
            return userRepository.findByTenantIdAndUsername(tenantId, username).isPresent();
        }

        boolean isUser = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_USER"));

        if (isUser) {
            String username = authentication.getName();
            return userRepository.findByTenantIdAndUsername(tenantId, username).isPresent();
        }

        return false;
    }

    public void validateTenantAccess(Authentication authentication, Integer tenantId) {
        if (!hasTenantAccess(authentication, tenantId)) {
            throw new ResourceNotFoundException("Tenant not found with id: " + tenantId);
        }
    }
}
