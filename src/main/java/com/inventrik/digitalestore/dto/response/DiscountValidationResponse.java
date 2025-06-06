package com.inventrik.digitalestore.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountValidationResponse {
    
    @Schema(description = "Is discount code valid", example = "true")
    private boolean valid;
    
    @Schema(description = "Discount code", example = "SAVE20")
    private String discountCode;
    
    @Schema(description = "Discount amount", example = "20.00")
    private BigDecimal discountAmount;
    
    @Schema(description = "Final amount after discount", example = "80.00")
    private BigDecimal finalAmount;
    
    @Schema(description = "Validation message", example = "Discount applied successfully")
    private String message;
    
    @Schema(description = "Error code if validation failed", example = "EXPIRED")
    private String errorCode;
    
    public static DiscountValidationResponse valid(String discountCode, BigDecimal discountAmount, BigDecimal finalAmount) {
        return new DiscountValidationResponse(true, discountCode, discountAmount, finalAmount, "Discount applied successfully", null);
    }
    
    public static DiscountValidationResponse invalid(String discountCode, String message, String errorCode) {
        return new DiscountValidationResponse(false, discountCode, BigDecimal.ZERO, BigDecimal.ZERO, message, errorCode);
    }
} 