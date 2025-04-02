package com.example.digitalestore.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {
    @Size(max = 100, message = "Product name must be less than 100 characters")
    private String productName;
    
    private String description;
    
    private BigDecimal defaultPrice;
    
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String defaultCurrency;
    
    private Long categoryId;
    
    private String status;
}