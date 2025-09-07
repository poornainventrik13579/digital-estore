package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.CurrencyRequest;
import com.inventrik.digitalestore.dto.response.CurrencyResponse;
import com.inventrik.digitalestore.security.TenantAccessValidator;
import com.inventrik.digitalestore.service.currency.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/currencies")
@RequiredArgsConstructor
@Tag(name = "Currency Management", description = "APIs for managing currencies and exchange rates")
public class CurrencyController {
    
    private final CurrencyService currencyService;
    private final TenantAccessValidator tenantAccessValidator;
    
    @GetMapping
    @Operation(summary = "Get all currencies", description = "Retrieve all active currencies for a tenant")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<CurrencyResponse>> getAllCurrencies(
            @PathVariable Integer tenantId,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<CurrencyResponse> currencies = currencyService.getAllCurrencies(tenantId);
        return ResponseEntity.ok(currencies);
    }
    
    @GetMapping("/{currencyCode}")
    @Operation(summary = "Get currency by code", description = "Retrieve a specific currency by its code")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<CurrencyResponse> getCurrency(
            @PathVariable Integer tenantId,
            @PathVariable String currencyCode,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        CurrencyResponse currency = currencyService.getCurrency(tenantId, currencyCode);
        return ResponseEntity.ok(currency);
    }
    
    @PostMapping
    @Operation(summary = "Create currency", description = "Create a new currency")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    public ResponseEntity<CurrencyResponse> createCurrency(
            @PathVariable Integer tenantId,
            @Valid @RequestBody CurrencyRequest request,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        CurrencyResponse currency = currencyService.createCurrency(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(currency);
    }
    
    @PutMapping("/{currencyCode}")
    @Operation(summary = "Update currency", description = "Update an existing currency")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    public ResponseEntity<CurrencyResponse> updateCurrency(
            @PathVariable Integer tenantId,
            @PathVariable String currencyCode,
            @Valid @RequestBody CurrencyRequest request,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        CurrencyResponse currency = currencyService.updateCurrency(tenantId, currencyCode, request);
        return ResponseEntity.ok(currency);
    }
    
    @DeleteMapping("/{currencyCode}")
    @Operation(summary = "Delete currency", description = "Delete a currency (soft delete)")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    public ResponseEntity<Void> deleteCurrency(
            @PathVariable Integer tenantId,
            @PathVariable String currencyCode,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        currencyService.deleteCurrency(tenantId, currencyCode);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/default")
    @Operation(summary = "Get default currency", description = "Get the default currency for a tenant")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<CurrencyResponse> getDefaultCurrency(
            @PathVariable Integer tenantId,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        CurrencyResponse currency = currencyService.getDefaultCurrency(tenantId);
        return ResponseEntity.ok(currency);
    }
    
    @GetMapping("/convert")
    @Operation(summary = "Convert amount", description = "Convert amount between currencies")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Map<String, Object>> convertAmount(
            @PathVariable Integer tenantId,
            @RequestParam BigDecimal amount,
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        BigDecimal convertedAmount = currencyService.convertAmount(amount, fromCurrency, toCurrency, tenantId);
        BigDecimal exchangeRate = currencyService.getExchangeRate(fromCurrency, toCurrency, tenantId);
        
        Map<String, Object> result = Map.of(
                "originalAmount", amount,
                "fromCurrency", fromCurrency,
                "toCurrency", toCurrency,
                "convertedAmount", convertedAmount,
                "exchangeRate", exchangeRate
        );
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/exchange-rate")
    @Operation(summary = "Get exchange rate", description = "Get exchange rate between two currencies")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Map<String, Object>> getExchangeRate(
            @PathVariable Integer tenantId,
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        BigDecimal exchangeRate = currencyService.getExchangeRate(fromCurrency, toCurrency, tenantId);
        
        Map<String, Object> result = Map.of(
                "fromCurrency", fromCurrency,
                "toCurrency", toCurrency,
                "exchangeRate", exchangeRate
        );
        
        return ResponseEntity.ok(result);
    }
} 