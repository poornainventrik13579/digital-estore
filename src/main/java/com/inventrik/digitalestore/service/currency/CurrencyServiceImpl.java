package com.inventrik.digitalestore.service.currency;

import com.inventrik.digitalestore.domain.currency.Currency;
import com.inventrik.digitalestore.dto.request.CurrencyRequest;
import com.inventrik.digitalestore.dto.response.CurrencyResponse;
import com.inventrik.digitalestore.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CurrencyServiceImpl implements CurrencyService {
    
    private final CurrencyRepository currencyRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<CurrencyResponse> getAllCurrencies(Integer tenantId) {
        return currencyRepository.findByTenantIdAndStatus(tenantId, "0")
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public CurrencyResponse getCurrency(Integer tenantId, String currencyCode) {
        Currency currency = currencyRepository.findByTenantIdAndCurrencyCodeAndStatus(tenantId, currencyCode, "0")
                .orElseThrow(() -> new RuntimeException("Currency not found"));
        return convertToResponse(currency);
    }
    
    @Override
    public CurrencyResponse createCurrency(Integer tenantId, CurrencyRequest request) {
        if ("1".equals(request.getIsDefault())) {
            currencyRepository.findDefaultCurrency(tenantId)
                    .ifPresent(defaultCurrency -> {
                        defaultCurrency.setIsDefault("0");
                        currencyRepository.save(defaultCurrency);
                    });
        }
        
        Currency currency = new Currency();
        currency.setTenantId(tenantId);
        currency.setCurrencyCode(request.getCurrencyCode());
        currency.setCurrencyName(request.getCurrencyName());
        currency.setSymbol(request.getSymbol());
        currency.setExchangeRate(request.getExchangeRate());
        currency.setIsDefault(request.getIsDefault());
        currency.setStatus("0");
        currency.setCreatedBy("1");
        currency.setUpdatedBy("1");
        
        Currency saved = currencyRepository.save(currency);
        return convertToResponse(saved);
    }
    
    @Override
    public CurrencyResponse updateCurrency(Integer tenantId, String currencyCode, CurrencyRequest request) {
        Currency currency = currencyRepository.findByTenantIdAndCurrencyCodeAndStatus(tenantId, currencyCode, "0")
                .orElseThrow(() -> new RuntimeException("Currency not found"));
        
        if ("1".equals(request.getIsDefault()) && !"1".equals(currency.getIsDefault())) {
            currencyRepository.findDefaultCurrency(tenantId)
                    .ifPresent(defaultCurrency -> {
                        defaultCurrency.setIsDefault("0");
                        currencyRepository.save(defaultCurrency);
                    });
        }
        
        currency.setCurrencyName(request.getCurrencyName());
        currency.setSymbol(request.getSymbol());
        currency.setExchangeRate(request.getExchangeRate());
        currency.setIsDefault(request.getIsDefault());
        currency.setUpdatedBy("1");
        
        Currency updated = currencyRepository.save(currency);
        return convertToResponse(updated);
    }
    
    @Override
    public void deleteCurrency(Integer tenantId, String currencyCode) {
        Currency currency = currencyRepository.findByTenantIdAndCurrencyCodeAndStatus(tenantId, currencyCode, "0")
                .orElseThrow(() -> new RuntimeException("Currency not found"));
        
        if ("1".equals(currency.getIsDefault())) {
            throw new RuntimeException("Cannot delete default currency");
        }
        
        currency.setStatus("-1");
        currency.setUpdatedBy("1");
        currencyRepository.save(currency);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal convertAmount(BigDecimal amount, String fromCurrency, String toCurrency, Integer tenantId) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }
        
        Currency fromCur = currencyRepository.findByTenantIdAndCurrencyCodeAndStatus(tenantId, fromCurrency, "0")
                .orElseThrow(() -> new RuntimeException("From currency not found"));
        Currency toCur = currencyRepository.findByTenantIdAndCurrencyCodeAndStatus(tenantId, toCurrency, "0")
                .orElseThrow(() -> new RuntimeException("To currency not found"));
        
        BigDecimal usdAmount = amount.divide(fromCur.getExchangeRate(), 4, RoundingMode.HALF_UP);
        return usdAmount.multiply(toCur.getExchangeRate()).setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency, Integer tenantId) {
        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }
        
        Currency fromCur = currencyRepository.findByTenantIdAndCurrencyCodeAndStatus(tenantId, fromCurrency, "0")
                .orElseThrow(() -> new RuntimeException("From currency not found"));
        Currency toCur = currencyRepository.findByTenantIdAndCurrencyCodeAndStatus(tenantId, toCurrency, "0")
                .orElseThrow(() -> new RuntimeException("To currency not found"));
        
        return toCur.getExchangeRate().divide(fromCur.getExchangeRate(), 4, RoundingMode.HALF_UP);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CurrencyResponse getDefaultCurrency(Integer tenantId) {
        Currency currency = currencyRepository.findDefaultCurrency(tenantId)
                .orElseThrow(() -> new RuntimeException("Default currency not found"));
        return convertToResponse(currency);
    }
    
    private CurrencyResponse convertToResponse(Currency currency) {
        CurrencyResponse response = new CurrencyResponse();
        response.setCurrencyCode(currency.getCurrencyCode());
        response.setCurrencyName(currency.getCurrencyName());
        response.setSymbol(currency.getSymbol());
        response.setExchangeRate(currency.getExchangeRate());
        response.setIsDefault(currency.getIsDefault());
        response.setStatus(currency.getStatus());
        response.setCreated(currency.getCreated());
        response.setUpdated(currency.getUpdated());
        return response;
    }
} 