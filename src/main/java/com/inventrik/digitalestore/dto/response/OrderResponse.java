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

    /** subTotal - totalAmount. Zero if no discount was applied. */
    private BigDecimal discountAmount;

    /** Tax applied to the order. Currently zero — tax flow not yet implemented. */
    private BigDecimal taxAmount;

    /** Final charged amount = subTotal - discountAmount + taxAmount. */
    private BigDecimal totalAmount;

    private BigDecimal exchangeRate;
    private String status;

    /** Number of distinct items in this order. */
    private int totalItems;

    private LocalDateTime created;
    private LocalDateTime updated;
    private List<OrderItemResponse> orderItems;
}
