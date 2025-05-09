package com.inventrik.digitalestore.config;

import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("***** DATA INITIALIZER IS RUNNING *****");
            
            // Check if admin user exists
            boolean adminExists = userRepository.existsByUsername("admin");
            System.out.println("Admin user exists: " + adminExists);
            
            if (!adminExists) {
                // Create admin user
                User admin = new User();
                admin.setTenantId(1);
                admin.setUserId(1L);
                admin.setUsername("admin");
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setEmail("admin@example.com");
                admin.setPhone("1234567890");
                admin.setOtp("123456");
                
                // Encode the password and print it for debugging
                String encodedPassword = passwordEncoder.encode("admin");
                System.out.println("Encoded password: " + encodedPassword);
                admin.setPasswordHash(encodedPassword);
                
                admin.setStatus("0"); // Active
                admin.setCreatedBy("sy");
                admin.setUpdatedBy("sy");
                admin.setCreated(LocalDateTime.now());
                admin.setUpdated(LocalDateTime.now());
                admin.setOrders(new ArrayList<>()); // Initialize empty orders list
                
                userRepository.save(admin);
                
                System.out.println("Admin user created successfully");
            } else {
                // Print current admin user details for debugging
                User admin = userRepository.findByUsername("admin").orElse(null);
                if (admin != null) {
                    System.out.println("Existing admin user: " + admin.getUsername());
                    System.out.println("Current password hash: " + admin.getPasswordHash());
                }
            }
        };
    }
}