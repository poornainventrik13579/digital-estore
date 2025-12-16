package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long orderItemId;
    private Long orderId;
    private Long productId;
    private BigDecimal priceAtPurchase;
    private String licenseKey;
    private String status;
    private LocalDateTime created;
    private LocalDateTime updated;
    private String productName;
    private String productImageUrl;
}