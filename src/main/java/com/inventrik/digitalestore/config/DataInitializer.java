package com.inventrik.digitalestore.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DataInitializer {

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("***** MULTI-TENANT DIGITAL E-STORE INITIALIZED *****");
            log.info("Database schema and initial data created by migration V1");
            log.info("Default admin credentials: username=admin, password=admin");
            log.info("System ready for multi-tenant operations!");
        };
    }
}