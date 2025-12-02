package com.inventrik.digitalestore.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user;
        String fullUsername; // Username to return in UserDetails (with tenant prefix for tenant users)

        // Check if username contains tenantId (format: "tenantId:username")
        // This is used for tenant-specific users (store admins, customers)
        if (username.contains(":")) {
            String[] parts = username.split(":", 2);
            try {
                Integer tenantId = Integer.parseInt(parts[0]);
                String actualUsername = parts[1];

                // Tenant-aware lookup: for store admins and customers
                user = userRepository.findByTenantIdAndUsername(tenantId, actualUsername)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "User not found with username: " + actualUsername + " in tenant: " + tenantId));

                // For tenant users, return full username with tenant prefix for JWT
                fullUsername = username; // e.g., "1:john"
            } catch (NumberFormatException e) {
                throw new UsernameNotFoundException("Invalid tenant ID in username: " + username);
            }
        } else {
            // Non-tenant lookup: for platform/system admins only
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

            // For platform admins, return username as-is
            fullUsername = user.getUsername();
        }

        return new org.springframework.security.core.userdetails.User(
                fullUsername,
                user.getPasswordHash(),
                user.getStatus().equals("0"), // enabled
                true, // account not expired
                true, // credentials not expired
                true, // account not locked
                getAuthorities(user)
        );
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add role based on user's role field (only give specific role, not all roles)
        if ("0".equals(user.getStatus())) {
            switch (user.getUserRole()) {
                case ADMIN:
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    break;
                case TENANT:
                    authorities.add(new SimpleGrantedAuthority("ROLE_TENANT"));
                    break;
                case USER:
                default:
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    break;
            }
        }
        
        return authorities;
    }
}