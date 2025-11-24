package com.inventrik.digitalestore.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxUpdateRequest {
    
    @Size(max = 255, message = "Tax code must not exceed 255 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Tax code must contain only uppercase letters, numbers, underscores, and hyphens")
    private String code;
    
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @DecimalMin(value = "0.00", message = "Tax value must be greater than or equal to 0")
    @DecimalMax(value = "100.00", message = "Tax value must be less than or equal to 100")
    @Digits(integer = 3, fraction = 2, message = "Tax value must have at most 3 integer digits and 2 decimal places")
    private BigDecimal value;
    
    @Pattern(regexp = "^[YN]$", message = "Default flag must be 'Y' or 'N'")
    private String defaultFlag;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @Pattern(regexp = "^[AI]$", message = "Status must be 'A' (Active) or 'I' (Inactive)")
    private String status;
    
    @AssertTrue(message = "End date must be after start date")
    public boolean isEndDateValid() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }
}
