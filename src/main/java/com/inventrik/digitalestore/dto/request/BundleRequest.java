package com.inventrik.digitalestore.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BundleRequest {
    
    @NotBlank(message = "Bundle name is required")
    @Size(max = 100, message = "Bundle name must not exceed 100 characters")
    private String bundleName;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Bundle price is required")
    @DecimalMin(value = "0.01", message = "Bundle price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Bundle price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal bundlePrice;
    
    @DecimalMin(value = "0.00", message = "Discount percent cannot be negative")
    @DecimalMax(value = "100.00", message = "Discount percent cannot exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Discount percent must have at most 3 integer digits and 2 decimal places")
    private BigDecimal discountPercent = BigDecimal.ZERO;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3 uppercase letters")
    private String currency = "USD";
    
    @NotEmpty(message = "Bundle must contain at least one product")
    private List<BundleItemRequest> bundleItems;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BundleItemRequest {
        
        @NotNull(message = "Product ID is required")
        @Positive(message = "Product ID must be positive")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 100, message = "Quantity cannot exceed 100")
        private Integer quantity = 1;
    }
} 