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
            System.out.println("***** RESETTING ADMIN USER *****");
            
            // Find admin or create if doesn't exist
            User admin = userRepository.findByUsername("admin")
                    .orElseGet(() -> {
                        User newAdmin = new User();
                        newAdmin.setTenantId(1);
                        newAdmin.setUserId(1L);
                        newAdmin.setUsername("admin");
                        newAdmin.setFirstName("Admin");
                        newAdmin.setLastName("User");
                        newAdmin.setEmail("admin@example.com");
                        newAdmin.setPhone("1234567890");
                        newAdmin.setOtp("123456");
                        newAdmin.setStatus("0"); // Active
                        newAdmin.setCreatedBy("sy");
                        newAdmin.setUpdatedBy("sy");
                        newAdmin.setCreated(LocalDateTime.now());
                        newAdmin.setUpdated(LocalDateTime.now());
                        newAdmin.setOrders(new ArrayList<>());
                        return newAdmin;
                    });
                    
            // ALWAYS update password to ensure it's correct
            admin.setPasswordHash(passwordEncoder.encode("admin"));
            userRepository.save(admin);
            
            System.out.println("Admin user reset successfully with password: admin");
        };
    }
}