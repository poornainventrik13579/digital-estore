package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.CurrencyRequest;
import com.inventrik.digitalestore.dto.response.CurrencyResponse;
import com.inventrik.digitalestore.service.currency.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
@Tag(name = "Currency Management", description = "APIs for managing currencies and exchange rates")
@SecurityRequirement(name = "oauth2")
public class CurrencyController {
    
    private final CurrencyService currencyService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get all currencies", description = "Retrieve all active currencies for a tenant")
    public ResponseEntity<List<CurrencyResponse>> getAllCurrencies(@RequestHeader("X-Tenant-ID") Integer tenantId) {
        List<CurrencyResponse> currencies = currencyService.getAllCurrencies(tenantId);
        return ResponseEntity.ok(currencies);
    }

    @GetMapping("/{currencyCode}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get currency by code", description = "Retrieve a specific currency by its code")
    public ResponseEntity<CurrencyResponse> getCurrency(
            @RequestHeader("X-Tenant-ID") Integer tenantId,
            @PathVariable String currencyCode) {
        CurrencyResponse currency = currencyService.getCurrency(tenantId, currencyCode);
        return ResponseEntity.ok(currency);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Create currency", description = "Create a new currency")
    public ResponseEntity<CurrencyResponse> createCurrency(
            @RequestHeader("X-Tenant-ID") Integer tenantId,
            @Valid @RequestBody CurrencyRequest request) {
        CurrencyResponse currency = currencyService.createCurrency(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(currency);
    }

    @PutMapping("/{currencyCode}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Update currency", description = "Update an existing currency")
    public ResponseEntity<CurrencyResponse> updateCurrency(
            @RequestHeader("X-Tenant-ID") Integer tenantId,
            @PathVariable String currencyCode,
            @Valid @RequestBody CurrencyRequest request) {
        CurrencyResponse currency = currencyService.updateCurrency(tenantId, currencyCode, request);
        return ResponseEntity.ok(currency);
    }

    @DeleteMapping("/{currencyCode}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Delete currency", description = "Delete a currency (soft delete)")
    public ResponseEntity<Void> deleteCurrency(
            @RequestHeader("X-Tenant-ID") Integer tenantId,
            @PathVariable String currencyCode) {
        currencyService.deleteCurrency(tenantId, currencyCode);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/default")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get default currency", description = "Get the default currency for a tenant")
    public ResponseEntity<CurrencyResponse> getDefaultCurrency(@RequestHeader("X-Tenant-ID") Integer tenantId) {
        CurrencyResponse currency = currencyService.getDefaultCurrency(tenantId);
        return ResponseEntity.ok(currency);
    }

    @GetMapping("/convert")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Convert amount", description = "Convert amount between currencies")
    public ResponseEntity<Map<String, Object>> convertAmount(
            @RequestHeader("X-Tenant-ID") Integer tenantId,
            @RequestParam BigDecimal amount,
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency) {
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
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get exchange rate", description = "Get exchange rate between two currencies")
    public ResponseEntity<Map<String, Object>> getExchangeRate(
            @RequestHeader("X-Tenant-ID") Integer tenantId,
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency) {
        BigDecimal exchangeRate = currencyService.getExchangeRate(fromCurrency, toCurrency, tenantId);

        Map<String, Object> result = Map.of(
                "fromCurrency", fromCurrency,
                "toCurrency", toCurrency,
                "exchangeRate", exchangeRate
        );

        return ResponseEntity.ok(result);
    }
} 