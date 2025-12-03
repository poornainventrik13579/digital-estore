package com.inventrik.digitalestore.dto.request;

import com.inventrik.digitalestore.domain.discount.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountCodeRequest {
    
    @Schema(description = "Discount code", example = "SAVE20", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Discount code is required")
    @Size(min = 3, max = 50, message = "Discount code must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Discount code can only contain uppercase letters, numbers, hyphens and underscores")
    private String code;
    
    @Schema(description = "Discount type", example = "PERCENTAGE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Discount type is required")
    private DiscountType discountType;
    
    @Schema(description = "Discount value (percentage or fixed amount)", example = "20.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Discount value must be greater than zero")
    @DecimalMax(value = "100.00", inclusive = true, message = "Percentage discount cannot exceed 100%")
    private BigDecimal discountValue;
    
    @Schema(description = "Minimum order amount to apply discount", example = "50.00")
    @DecimalMin(value = "0.00", inclusive = true, message = "Minimum order amount cannot be negative")
    private BigDecimal minOrderAmount = BigDecimal.ZERO;
    
    @Schema(description = "Maximum number of uses (0 for unlimited)", example = "100")
    @Min(value = 0, message = "Max uses cannot be negative")
    private Integer maxUses = 0;
    
    @Schema(description = "Valid from date", example = "2024-01-01T00:00:00")
    private LocalDateTime validFrom;

    @Schema(description = "Valid until date", example = "2024-12-31T23:59:59")
    private LocalDateTime validUntil;

    @AssertTrue(message = "Valid until date must be after valid from date")
    public boolean isValidDateRange() {
        if (validFrom != null && validUntil != null) {
            return validUntil.isAfter(validFrom);
        }
        return true;
    }
    
    @AssertTrue(message = "Percentage discount cannot exceed 100%")
    public boolean isValidPercentage() {
        if (discountType == DiscountType.PERCENTAGE) {
            return discountValue.compareTo(new BigDecimal("100.00")) <= 0;
        }
        return true;
    }
} 