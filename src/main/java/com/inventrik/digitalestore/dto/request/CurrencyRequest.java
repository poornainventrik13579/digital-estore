package com.inventrik.digitalestore.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRequest {
    
    @NotBlank
    @Size(min = 3, max = 3)
    private String currencyCode;
    
    @NotBlank
    @Size(max = 50)
    private String currencyName;
    
    @NotBlank
    @Size(max = 10)
    private String symbol;
    
    @NotNull
    @DecimalMin("0.0001")
    private BigDecimal exchangeRate;
    
    @NotBlank
    @Size(min = 1, max = 1)
    private String isDefault;
} 