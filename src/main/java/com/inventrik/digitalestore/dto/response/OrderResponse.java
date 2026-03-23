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
    private String orderId;
    private Integer tenantId;
    private String userId;
    private LocalDateTime orderDate;
    private String currency;

    /** Sum of all item prices before discount. */
    private BigDecimal subTotal;

    /** Discount applied. Zero if no discount code was used. */
    private BigDecimal discountAmount;

    /** Tax applied at time of order creation. */
    private BigDecimal taxAmount;

    /** Final charged amount = subTotal - discountAmount + taxAmount. */
    private BigDecimal totalAmount;

    private BigDecimal exchangeRate;
    private String status;

    private int totalItems;

    private LocalDateTime created;
    private LocalDateTime updated;
    private List<OrderItemResponse> orderItems;
}
