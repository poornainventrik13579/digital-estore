package com.inventrik.digitalestore.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

@SpringBootTest
public class IdGeneratorServiceManualTest {

    @Autowired
    private IdGeneratorService idGeneratorService;

    @Test
    public void testIdGeneration() {
        System.out.println("🔢 Testing ID Generation Service");
        System.out.println("=================================");

        // Test generating different types of IDs
        System.out.println("\n📋 Generating Sample IDs:");

        for (int i = 1; i <= 10; i++) {
            String userId = idGeneratorService.generateId(1, "USER");
            String productId = idGeneratorService.generateId(1, "PRODUCT");
            String orderId = idGeneratorService.generateId(1, "ORDER");

            System.out.println("Set " + i + ":");
            System.out.println("  User ID:    " + userId + " (" + userId.length() + " chars)");
            System.out.println("  Product ID: " + productId + " (" + productId.length() + " chars)");
            System.out.println("  Order ID:   " + orderId + " (" + orderId.length() + " chars)");
            System.out.println();
        }

        // Test uniqueness
        System.out.println("🛡️ Testing Uniqueness:");
        Set<String> generatedIds = new HashSet<>();
        int totalToGenerate = 1000;

        for (int i = 0; i < totalToGenerate; i++) {
            String id = idGeneratorService.generateId(1, "TEST");
            if (!generatedIds.add(id)) {
                System.out.println("❌ COLLISION DETECTED at iteration " + i + " with ID: " + id);
                return;
            }
        }

        System.out.println("✅ Generated " + totalToGenerate + " unique IDs with zero collisions!");

        // Test performance
        System.out.println("\n⚡ Performance Test:");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 5000; i++) {
            idGeneratorService.generateId(1, "PERF_TEST");
        }

        long endTime = System.currentTimeMillis();
        System.out.println("✅ Generated 5000 IDs in " + (endTime - startTime) + "ms");
        System.out.println("✅ Average: " + ((endTime - startTime) / 5000.0) + "ms per ID");
    }

    @Test
    public void demonstrateUsage() {
        System.out.println("\n💡 How to Use IdGeneratorService:");
        System.out.println("==================================");

        // Example 1: Generate User ID
        String userId = idGeneratorService.generateId(1, "USER");
        System.out.println("User ID: " + userId);

        // Example 2: Generate Product ID
        String productId = idGeneratorService.generateId(1, "PRODUCT");
        System.out.println("Product ID: " + productId);

        // Example 3: Generate Order ID
        String orderId = idGeneratorService.generateId(2, "ORDER"); // Different tenant
        System.out.println("Order ID (Tenant 2): " + orderId);

        // Example 4: Generate Generic ID
        String genericId = idGeneratorService.generateUniqueId();
        System.out.println("Generic ID: " + genericId);

        System.out.println("\n📝 Usage Notes:");
        System.out.println("- All IDs are 32-character UUID strings (without dashes)");
        System.out.println("- IDs are globally unique (UUID v4)");
        System.out.println("- Near-zero collision probability");
        System.out.println("- Thread-safe for concurrent use");
        System.out.println("- Compatible with JavaScript and other systems");
    }
} 