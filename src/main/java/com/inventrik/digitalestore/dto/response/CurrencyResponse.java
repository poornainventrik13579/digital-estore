package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyResponse {
    private String currencyCode;
    private String currencyName;
    private String symbol;
    private BigDecimal exchangeRate;
    private String isDefault;
    private String status;
    private LocalDateTime created;
    private LocalDateTime updated;
} 