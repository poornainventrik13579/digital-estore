package com.inventrik.digitalestore.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartialRefundRequest {
    
    @Schema(description = "Refund amount", example = "25.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Refund amount must be greater than zero")
    private BigDecimal refundAmount;
    
    @Schema(description = "Reason for refund", example = "Customer requested partial refund due to incomplete delivery", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Refund reason is required")
    @Size(min = 10, max = 500, message = "Refund reason must be between 10 and 500 characters")
    private String reason;
} 