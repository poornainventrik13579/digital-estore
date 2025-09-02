package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxCalculationResponse {
    
    private BigDecimal baseAmount;
    private BigDecimal totalTaxAmount;
    private BigDecimal finalAmount;
    private List<TaxLineItem> taxBreakdown;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxLineItem {
        private String taxCode;
        private String taxDescription;
        private BigDecimal taxRate;
        private BigDecimal taxAmount;
        private Boolean isDefault;
    }
}
