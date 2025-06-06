package com.inventrik.digitalestore.service.currency;

import com.inventrik.digitalestore.dto.request.CurrencyRequest;
import com.inventrik.digitalestore.dto.response.CurrencyResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CurrencyService {
    
    List<CurrencyResponse> getAllCurrencies(Integer tenantId);
    
    CurrencyResponse getCurrency(Integer tenantId, String currencyCode);
    
    CurrencyResponse createCurrency(Integer tenantId, CurrencyRequest request);
    
    CurrencyResponse updateCurrency(Integer tenantId, String currencyCode, CurrencyRequest request);
    
    void deleteCurrency(Integer tenantId, String currencyCode);
    
    BigDecimal convertAmount(BigDecimal amount, String fromCurrency, String toCurrency, Integer tenantId);
    
    BigDecimal getExchangeRate(String fromCurrency, String toCurrency, Integer tenantId);
    
    CurrencyResponse getDefaultCurrency(Integer tenantId);
} 