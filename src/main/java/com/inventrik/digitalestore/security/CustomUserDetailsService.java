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
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
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
        if ("A".equals(user.getStatus())) {
            switch (user.getUserRole()) {
                case SYSTEM_ADMIN:
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    break;
                case TENANT_ADMIN:
                    authorities.add(new SimpleGrantedAuthority("ROLE_TENANT_ADMIN"));
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