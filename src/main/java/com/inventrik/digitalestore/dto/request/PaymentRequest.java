package com.inventrik.digitalestore.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    
    @Schema(description = "Order ID", example = "123456789", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @Schema(description = "Currency code", example = "USD", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
    private String currency;
    
    @Schema(description = "Payment amount", example = "99.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than zero")
    @DecimalMax(value = "999999.99", inclusive = true, message = "Amount cannot exceed 999,999.99")
    @Digits(integer = 6, fraction = 2, message = "Amount must have at most 6 integer digits and 2 decimal places")
    private BigDecimal amount;
    
    @Schema(description = "Payment method", example = "stripe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Payment method is required")
    @Pattern(regexp = "^(stripe|paypal|bank_transfer|cash)$", message = "Payment method must be one of: stripe, paypal, bank_transfer, cash")
    private String paymentMethod;
    
    // For Stripe integration
    @Schema(description = "Stripe token or payment method ID", example = "pm_card_visa")
    private String paymentToken;
}