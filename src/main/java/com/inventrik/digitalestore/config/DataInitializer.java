package com.inventrik.digitalestore.config;

import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@Slf4j
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("***** RESETTING ADMIN USER *****");
            
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
                        return newAdmin;
                    });
                    
            admin.setPasswordHash(passwordEncoder.encode("admin"));
            userRepository.save(admin);
            
            log.info("Admin user reset successfully with password: admin");
        };
    }
}