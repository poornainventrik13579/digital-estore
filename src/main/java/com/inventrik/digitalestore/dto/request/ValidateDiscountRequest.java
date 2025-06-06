package com.inventrik.digitalestore.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateDiscountRequest {
    
    @Schema(description = "Discount code to validate", example = "SAVE20", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Discount code is required")
    private String discountCode;
    
    @Schema(description = "Order amount to apply discount to", example = "100.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Order amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Order amount must be greater than zero")
    private BigDecimal orderAmount;
    
    @Schema(description = "User ID for usage validation", example = "12345")
    private Long userId;
} 