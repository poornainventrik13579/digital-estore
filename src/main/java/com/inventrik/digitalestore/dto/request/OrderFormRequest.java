package com.inventrik.digitalestore.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * A simplified DTO for form submissions with a single product item.
 * Multiple product orders would require multiple form submissions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderFormRequest {
    
    @Schema(description = "User ID", example = "123456789", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User ID is required")
    private String userId;
    
    @Schema(description = "Currency code", example = "USD", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String currency;
    
    @Schema(description = "Total amount", example = "99.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Total amount must be greater than zero")
    private BigDecimal totalAmount;
    
    @Schema(description = "Exchange rate", example = "1.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Exchange rate is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Exchange rate must be greater than zero")
    private BigDecimal exchangeRate;
    
    // Single product order
    @Schema(description = "Product ID", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Product ID is required")
    private String productId;
    
    @Schema(description = "Price at purchase", example = "29.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Price must be greater than zero")
    private BigDecimal price;
    
    @Schema(description = "License key (optional)", example = "XXXX-YYYY-ZZZZ")
    private String licenseKey;
    
    /**
     * Converts this simplified form to the regular OrderRequest
     */
    public OrderRequest toOrderRequest() {
        // Create the order items list with a single item
        List<OrderItemRequest> items = new ArrayList<>();
        OrderItemRequest item = new OrderItemRequest(productId, price, licenseKey);
        items.add(item);
        
        // Create and return the full order request
        return new OrderRequest(userId, currency, totalAmount, exchangeRate, items, null);
    }
}