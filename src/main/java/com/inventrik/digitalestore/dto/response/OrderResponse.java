package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private Integer tenantId;
    private Long userId;
    private LocalDateTime orderDate;
    private String currency;
    private BigDecimal totalAmount;
    private BigDecimal exchangeRate;
    private String status;
    private LocalDateTime created;
    private LocalDateTime updated;
    private List<OrderItemResponse> orderItems;
}