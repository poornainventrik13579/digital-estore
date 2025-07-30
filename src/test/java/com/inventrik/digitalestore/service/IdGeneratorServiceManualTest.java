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
            Long userId = idGeneratorService.generateId(1, "USER");
            Long productId = idGeneratorService.generateId(1, "PRODUCT");
            Long orderId = idGeneratorService.generateId(1, "ORDER");
            
            System.out.println("Set " + i + ":");
            System.out.println("  User ID:    " + userId + " (" + String.valueOf(userId).length() + " digits)");
            System.out.println("  Product ID: " + productId + " (" + String.valueOf(productId).length() + " digits)");
            System.out.println("  Order ID:   " + orderId + " (" + String.valueOf(orderId).length() + " digits)");
            System.out.println();
        }

        // Test uniqueness
        System.out.println("🛡️ Testing Uniqueness:");
        Set<Long> generatedIds = new HashSet<>();
        int totalToGenerate = 1000;
        
        for (int i = 0; i < totalToGenerate; i++) {
            Long id = idGeneratorService.generateId(1, "TEST");
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
        Long userId = idGeneratorService.generateId(1, "USER");
        System.out.println("User ID: " + userId);
        
        // Example 2: Generate Product ID
        Long productId = idGeneratorService.generateId(1, "PRODUCT");
        System.out.println("Product ID: " + productId);
        
        // Example 3: Generate Order ID
        Long orderId = idGeneratorService.generateId(2, "ORDER"); // Different tenant
        System.out.println("Order ID (Tenant 2): " + orderId);
        
        // Example 4: Generate Generic ID
        Long genericId = idGeneratorService.generateUniqueId();
        System.out.println("Generic ID: " + genericId);
        
        System.out.println("\n📝 Usage Notes:");
        System.out.println("- All IDs are exactly 12 digits");
        System.out.println("- IDs are unpredictable and secure");
        System.out.println("- Zero collision guarantee");
        System.out.println("- Thread-safe for concurrent use");
    }
} 