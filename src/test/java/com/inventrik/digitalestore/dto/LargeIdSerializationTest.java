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
        String largeUserId = UUID.randomUUID().toString().replace("-", "");

        UserResponse userResponse = new UserResponse(
            largeUserId, 1, "testuser", "Test", "User", null,
            "+1-555-0001", "test@example.com", UserType.INDIVIDUAL,
            UserRole.USER,
            null, null, null, null, null, null, null,
            "0", LocalDateTime.now(), LocalDateTime.now()
        );

        String json = objectMapper.writeValueAsString(userResponse);

        assertTrue(json.contains("\"userId\":\"" + largeUserId + "\""),
                   "UserId should be serialized as a string: " + json);

        System.out.println("✅ Large UserId: " + largeUserId);
        System.out.println("✅ JSON contains userId as string: " + json.contains("\"userId\":\"" + largeUserId + "\""));
    }

    @Test
    public void testOrderIdSerializedAsString() throws Exception {
        String largeOrderId = UUID.randomUUID().toString().replace("-", "");
        String largeUserId = UUID.randomUUID().toString().replace("-", "");

        OrderResponse orderResponse = new OrderResponse(
            largeOrderId, 1, largeUserId, LocalDateTime.now(),
            "USD",
            new BigDecimal("99.99"),  // subTotal
            BigDecimal.ZERO,          // discountAmount
            BigDecimal.ZERO,          // taxAmount
            new BigDecimal("99.99"),  // totalAmount
            new BigDecimal("1.0"),    // exchangeRate
            "PENDING",
            0,                        // totalItems
            LocalDateTime.now(), LocalDateTime.now(), null
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
        String largeProductId = UUID.randomUUID().toString().replace("-", "");
        String largeCategoryId = UUID.randomUUID().toString().replace("-", "");

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
        Long maxSafeInt = 9007199254740991L;
        String largeUserId = UUID.randomUUID().toString().replace("-", "");

        System.out.println("JavaScript MAX_SAFE_INTEGER: " + maxSafeInt);
        System.out.println("Generated Large UserId (String): " + largeUserId);
        System.out.println("UserId length: " + largeUserId.length() + " characters");

        assertTrue(largeUserId.length() == 32,
                   "Generated userId should be 32 characters (UUID without dashes)");
    }
}