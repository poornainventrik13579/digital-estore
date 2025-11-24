package com.inventrik.digitalestore.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    
    @Schema(description = "User ID", example = "123456789", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @Schema(description = "Currency code", example = "USD", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters (ISO 4217 format)")
    private String currency;
    
    @Schema(description = "Total amount", example = "99.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Total amount must be greater than zero")
    private BigDecimal totalAmount;
    
    @Schema(description = "Exchange rate", example = "1.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Exchange rate is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Exchange rate must be greater than zero")
    private BigDecimal exchangeRate;
    
    @Schema(description = "List of order items", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> orderItems;
    
    @Schema(description = "Discount code to apply", example = "SAVE20")
    @Size(max = 50, message = "Discount code must be less than 50 characters")
    private String discountCode;
}