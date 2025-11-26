package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.CurrencyRequest;
import com.inventrik.digitalestore.dto.response.CurrencyResponse;
import com.inventrik.digitalestore.service.currency.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/currencies")
@RequiredArgsConstructor
@Tag(name = "Currency Management")
public class CurrencyController {
    
    private final CurrencyService currencyService;
    
    @GetMapping
    @Operation(summary = "Get all currencies")
    public ResponseEntity<List<CurrencyResponse>> getAllCurrencies(@PathVariable Integer tenantId) {
        return ResponseEntity.ok(currencyService.getAllCurrencies(tenantId));
    }

    @GetMapping("/{currencyCode}")
    @Operation(summary = "Get currency by code")
    public ResponseEntity<CurrencyResponse> getCurrency(
            @PathVariable Integer tenantId,
            @PathVariable String currencyCode) {
        return ResponseEntity.ok(currencyService.getCurrency(tenantId, currencyCode));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create currency (System Admin only)")
    public ResponseEntity<CurrencyResponse> createCurrency(
            @PathVariable Integer tenantId,
            @Valid @RequestBody CurrencyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(currencyService.createCurrency(tenantId, request));
    }

    @PutMapping("/{currencyCode}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update currency (System Admin only)")
    public ResponseEntity<CurrencyResponse> updateCurrency(
            @PathVariable Integer tenantId,
            @PathVariable String currencyCode,
            @Valid @RequestBody CurrencyRequest request) {
        return ResponseEntity.ok(currencyService.updateCurrency(tenantId, currencyCode, request));
    }

    @DeleteMapping("/{currencyCode}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete currency (System Admin only)")
    public ResponseEntity<Void> deleteCurrency(
            @PathVariable Integer tenantId,
            @PathVariable String currencyCode) {
        currencyService.deleteCurrency(tenantId, currencyCode);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/default")
    @Operation(summary = "Get default currency")
    public ResponseEntity<CurrencyResponse> getDefaultCurrency(@PathVariable Integer tenantId) {
        return ResponseEntity.ok(currencyService.getDefaultCurrency(tenantId));
    }
    
    @GetMapping("/convert")
    @Operation(summary = "Convert amount")
    public ResponseEntity<Map<String, Object>> convertAmount(
            @PathVariable Integer tenantId,
            @RequestParam BigDecimal amount,
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency) {
        BigDecimal convertedAmount = currencyService.convertAmount(amount, fromCurrency, toCurrency, tenantId);
        BigDecimal exchangeRate = currencyService.getExchangeRate(fromCurrency, toCurrency, tenantId);

        return ResponseEntity.ok(Map.of(
                "originalAmount", amount,
                "fromCurrency", fromCurrency,
                "toCurrency", toCurrency,
                "convertedAmount", convertedAmount,
                "exchangeRate", exchangeRate
        ));
    }

    @GetMapping("/exchange-rate")
    @Operation(summary = "Get exchange rate")
    public ResponseEntity<Map<String, Object>> getExchangeRate(
            @PathVariable Integer tenantId,
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency) {
        BigDecimal exchangeRate = currencyService.getExchangeRate(fromCurrency, toCurrency, tenantId);

        return ResponseEntity.ok(Map.of(
                "fromCurrency", fromCurrency,
                "toCurrency", toCurrency,
                "exchangeRate", exchangeRate
        ));
    }
} 