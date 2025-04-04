package com.inventrik.digitalestore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for the Digital E-Store application.
 * This is the entry point for the Spring Boot application.
 */
@SpringBootApplication
@EnableJpaAuditing
public class DigitalEstoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalEstoreApplication.class, args);
    }
}
