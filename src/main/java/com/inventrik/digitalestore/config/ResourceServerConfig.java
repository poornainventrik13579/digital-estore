package com.inventrik.digitalestore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class ResourceServerConfig {

    @Bean
    @Order(3)
    public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                // Public endpoints
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", 
                                "/swagger-resources/**", "/webjars/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/webhooks/stripe").permitAll()
                // Add this line to permit access to common static resources
                .requestMatchers("/", "/favicon.ico", "/css/**", "/js/**", "/images/**").permitAll()
                // Protected endpoints
                .requestMatchers("/api/v1/tenants/*/products/**").hasAuthority("SCOPE_read")
                .requestMatchers("/api/v1/tenants/*/users/**").hasAuthority("SCOPE_read") 
                .requestMatchers("/api/v1/tenants/*/categories/**").hasAuthority("SCOPE_read")
                .requestMatchers("/api/v1/tenants/*/orders/**").hasAuthority("SCOPE_read")
                .requestMatchers("/api/v1/tenants/*/payments/**").hasAuthority("SCOPE_read")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
            
        return http.build();
    }
}