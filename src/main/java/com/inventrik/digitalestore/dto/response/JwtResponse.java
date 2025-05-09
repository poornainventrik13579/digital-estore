package com.inventrik.digitalestore.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Data
@NoArgsConstructor
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private String username;
    private Collection<? extends GrantedAuthority> authorities;
    
    // Standard constructor
    public JwtResponse(String accessToken, String refreshToken, String username, 
                      Collection<? extends GrantedAuthority> authorities) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.authorities = authorities;
    }
    
    // Add a static factory method for error responses instead of a duplicate constructor
    public static JwtResponse createErrorResponse(String errorMessage, String refreshToken, 
                                                String username, 
                                                Collection<? extends GrantedAuthority> authorities) {
        JwtResponse response = new JwtResponse();
        response.setAccessToken(errorMessage); // Using accessToken field to hold error message
        response.setRefreshToken(refreshToken);
        response.setUsername(username);
        response.setAuthorities(authorities);
        return response;
    }
}