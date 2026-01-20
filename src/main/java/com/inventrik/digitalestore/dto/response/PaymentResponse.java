package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String paymentId;
    private Integer tenantId;
    private String orderId;
    private String currency;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
    private BigDecimal refundedAmount;
    private BigDecimal remainingAmount;
    private String paymentMethod;
    private String transactionId;
    private String status;
    private String refundReason;
    private LocalDateTime created;
    private LocalDateTime updated;
    
    // For Stripe client-side integration
    private String clientSecret;
}