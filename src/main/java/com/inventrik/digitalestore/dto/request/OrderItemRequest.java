package com.inventrik.digitalestore.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    
    @Schema(description = "Product ID", example = "123456789", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Product ID is required")
    private String productId;
    
    @Schema(description = "Price at purchase", example = "29.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Price at purchase is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Price must be greater than zero")
    private BigDecimal priceAtPurchase;
    
    @Schema(description = "License key (optional)", example = "XXXX-YYYY-ZZZZ-AAAA")
    private String licenseKey;
}