package com.inventrik.digitalestore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
// import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security configuration specifically for payment-related endpoints.
 */
@Configuration
public class PaymentSecurityConfig {

    /**
     * Configure security for payment endpoints.
     */
    @Bean
    @Order(1) // Higher precedence than the default security config
    public SecurityFilterChain paymentSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/v1/tenants/*/payments/**", "/api/webhooks/stripe")
            .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for REST API
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Public webhook endpoint - secured by signature verification instead
                .requestMatchers("/api/webhooks/stripe").permitAll()
                // For development: temporarily permit all operations
                .requestMatchers("/api/v1/tenants/*/payments/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
            
        return http.build();
    }
    
    /**
     * Configure CORS for payment endpoints.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*")); // Allow all origins for development
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/tenants/*/payments/**", configuration);
        source.registerCorsConfiguration("/api/webhooks/stripe", configuration);
        return source;
    }
}