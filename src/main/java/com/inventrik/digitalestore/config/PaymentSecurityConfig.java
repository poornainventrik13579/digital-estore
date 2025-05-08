package com.inventrik.digitalestore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
    public SecurityFilterChain paymentSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/v1/tenants/*/payments/**")
            .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for REST API
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Public webhook endpoint - secured by signature verification instead
                .requestMatchers("/api/webhooks/stripe").permitAll()
                // Read-only operations require PAYMENT_READ authority
                .requestMatchers(HttpMethod.GET, "/api/v1/tenants/*/payments/**").hasAuthority("PAYMENT_READ")
                // Write operations require PAYMENT_WRITE authority
                .requestMatchers(HttpMethod.POST, "/api/v1/tenants/*/payments/**").hasAuthority("PAYMENT_WRITE")
                .requestMatchers(HttpMethod.PUT, "/api/v1/tenants/*/payments/**").hasAuthority("PAYMENT_WRITE")
                // Cancel and refund operations require PAYMENT_ADMIN authority
                .requestMatchers("/api/v1/tenants/*/payments/*/cancel").hasAuthority("PAYMENT_ADMIN")
                .requestMatchers("/api/v1/tenants/*/payments/*/refund").hasAuthority("PAYMENT_ADMIN")
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
        configuration.setAllowedOrigins(Arrays.asList("https://yourfrontend.com", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/tenants/*/payments/**", configuration);
        source.registerCorsConfiguration("/api/webhooks/stripe", configuration);
        return source;
    }
    
    /**
     * Configure rate limiting for payment endpoints.
     * This uses a simple in-memory implementation suitable for single-server deployments.
     * For production, consider using a distributed rate limiter like Redis.
     */
    @Bean
    public javax.servlet.Filter paymentRateLimitFilter() {
        return new org.springframework.web.filter.OncePerRequestFilter() {
            private final java.util.Map<String, java.util.concurrent.atomic.AtomicInteger> requestCounts = 
                    new java.util.concurrent.ConcurrentHashMap<>();
            private final java.util.Map<String, Long> blockUntil = 
                    new java.util.concurrent.ConcurrentHashMap<>();
                    
            @Override
            protected void doFilterInternal(
                    javax.servlet.http.HttpServletRequest request,
                    javax.servlet.http.HttpServletResponse response,
                    javax.servlet.FilterChain filterChain) 
                    throws javax.servlet.ServletException, java.io.IOException {
                
                // Only apply to payment endpoints
                String path = request.getRequestURI();
                if (!path.contains("/payments")) {
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // Use IP as the client identifier (in production use a more robust identifier)
                String clientId = request.getRemoteAddr();
                long now = System.currentTimeMillis();
                
                // Check if client is blocked
                if (blockUntil.containsKey(clientId) && blockUntil.get(clientId) > now) {
                    response.setStatus(429); // Too Many Requests
                    response.getWriter().write("Rate limit exceeded. Please try again later.");
                    return;
                }
                
                // Reset counters every minute
                String timeWindow = clientId + ":" + (now / 60000);
                requestCounts.putIfAbsent(timeWindow, new java.util.concurrent.atomic.AtomicInteger(0));
                int count = requestCounts.get(timeWindow).incrementAndGet();
                
                // Rate limit: 30 requests per minute for payment endpoints
                if (count > 30) {
                    // Block for 1 minute
                    blockUntil.put(clientId, now + 60000);
                    response.setStatus(429); // Too Many Requests
                    response.getWriter().write("Rate limit exceeded. Please try again later.");
                    return;
                }
                
                filterChain.doFilter(request, response);
            }
        };
    }
}