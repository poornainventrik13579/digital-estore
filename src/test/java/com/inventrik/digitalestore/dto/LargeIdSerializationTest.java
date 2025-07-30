package com.inventrik.digitalestore.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.dto.response.OrderResponse;
import com.inventrik.digitalestore.dto.response.ProductResponse;
import com.inventrik.digitalestore.domain.user.UserType;
import com.inventrik.digitalestore.domain.user.UserRole;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class LargeIdSerializationTest {

    private final ObjectMapper objectMapper;
    
    public LargeIdSerializationTest() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testUserIdSerializedAsString() throws Exception {
        // Create a large user ID similar to what IdGeneratorService generates
        Long largeUserId = Math.abs(UUID.randomUUID().getMostSignificantBits());
        
        UserResponse userResponse = new UserResponse(
            largeUserId, 1, "testuser", "Test", "User", null,
            "+1-555-0001", "test@example.com", UserType.INDIVIDUAL,
            UserRole.USER, null, null, null, null, null, null,
            null, "0", LocalDateTime.now(), LocalDateTime.now()
        );

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(userResponse);
        
        // Verify that userId appears as a string in JSON (surrounded by quotes)
        assertTrue(json.contains("\"userId\":\"" + largeUserId + "\""), 
                   "UserId should be serialized as a string: " + json);
        
        // Verify it doesn't appear as a number (without quotes)
        assertFalse(json.contains("\"userId\":" + largeUserId + ","), 
                    "UserId should not be serialized as a number");
        
        System.out.println("✅ Large UserId: " + largeUserId);
        System.out.println("✅ JSON contains userId as string: " + json.contains("\"userId\":\"" + largeUserId + "\""));
    }

    @Test
    public void testOrderIdSerializedAsString() throws Exception {
        Long largeOrderId = Math.abs(UUID.randomUUID().getMostSignificantBits());
        Long largeUserId = Math.abs(UUID.randomUUID().getMostSignificantBits());
        
        OrderResponse orderResponse = new OrderResponse(
            largeOrderId, 1, largeUserId, LocalDateTime.now(),
            "USD", new BigDecimal("99.99"), new BigDecimal("1.0"),
            "PENDING", LocalDateTime.now(), LocalDateTime.now(), null
        );

        String json = objectMapper.writeValueAsString(orderResponse);
        
        assertTrue(json.contains("\"orderId\":\"" + largeOrderId + "\""),
                   "OrderId should be serialized as a string");
        assertTrue(json.contains("\"userId\":\"" + largeUserId + "\""),
                   "UserId should be serialized as a string");
        
        System.out.println("✅ Large OrderId: " + largeOrderId + ", UserId: " + largeUserId);
        System.out.println("✅ JSON contains both IDs as strings");
    }

    @Test
    public void testProductIdSerializedAsString() throws Exception {
        Long largeProductId = Math.abs(UUID.randomUUID().getMostSignificantBits());
        Long largeCategoryId = Math.abs(UUID.randomUUID().getMostSignificantBits());
        
        ProductResponse productResponse = new ProductResponse(
            largeProductId, 1, "Test Product", "Description",
            new BigDecimal("29.99"), "USD", null, null, null, null, null,
            null, null, null, largeCategoryId, "0",
            LocalDateTime.now(), LocalDateTime.now()
        );

        String json = objectMapper.writeValueAsString(productResponse);
        
        assertTrue(json.contains("\"productId\":\"" + largeProductId + "\""),
                   "ProductId should be serialized as a string");
        assertTrue(json.contains("\"categoryId\":\"" + largeCategoryId + "\""),
                   "CategoryId should be serialized as a string");
        
        System.out.println("✅ Large ProductId: " + largeProductId + ", CategoryId: " + largeCategoryId);
        System.out.println("✅ JSON contains both IDs as strings");
    }

    @Test
    public void testJavaScriptSafeInteger() {
        // Test that shows the problem with JavaScript Number.MAX_SAFE_INTEGER
        Long maxSafeInt = 9007199254740991L; // JavaScript Number.MAX_SAFE_INTEGER
        Long largeUserId = Math.abs(UUID.randomUUID().getMostSignificantBits());
        
        System.out.println("JavaScript MAX_SAFE_INTEGER: " + maxSafeInt);
        System.out.println("Generated Large UserId: " + largeUserId);
        System.out.println("Is UserId larger than MAX_SAFE_INTEGER? " + (largeUserId > maxSafeInt));
        
        // Most UUID-based IDs will be larger than JavaScript's safe integer range
        assertTrue(largeUserId > maxSafeInt, 
                   "Generated userId should exceed JavaScript's safe integer range");
    }
} 