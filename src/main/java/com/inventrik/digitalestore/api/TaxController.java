package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.TaxRequest;
import com.inventrik.digitalestore.dto.request.TaxUpdateRequest;
import com.inventrik.digitalestore.dto.response.TaxCalculationResponse;
import com.inventrik.digitalestore.dto.response.TaxResponse;
import com.inventrik.digitalestore.service.tax.TaxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/taxes")
@RequiredArgsConstructor
@Tag(name = "Tax Management", description = "APIs for managing tax rules and calculations")
@SecurityRequirement(name = "oauth2")
public class TaxController {

    private final TaxService taxService;
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get all taxes for a tenant")
    public ResponseEntity<List<TaxResponse>> getTaxesByTenant(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId) {
        return ResponseEntity.ok(taxService.getTaxesByTenant(tenantId));
    }
    
    @GetMapping("/{taxId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get a specific tax")
    public ResponseEntity<TaxResponse> getTax(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Tax ID", required = true)
            @PathVariable Integer taxId) {
        return ResponseEntity.ok(taxService.getTax(tenantId, taxId));
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create a new tax rule (JSON)")
    public ResponseEntity<TaxResponse> createTaxJson(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Valid @RequestBody TaxRequest taxRequest,
            Authentication authentication) {
        
        taxRequest.setTenantId(tenantId);
        String username = (authentication != null) ? authentication.getName() : "system";
        TaxResponse createdTax = taxService.createTax(username, taxRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTax);
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create a new tax rule (Form)")
    public ResponseEntity<TaxResponse> createTax(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Valid @ModelAttribute TaxRequest taxRequest,
            Authentication authentication) {
        
        taxRequest.setTenantId(tenantId);
        String username = (authentication != null) ? authentication.getName() : "system";
        TaxResponse createdTax = taxService.createTax(username, taxRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTax);
    }
    
    @PutMapping(path = "/{taxId}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update a tax rule (JSON)")
    public ResponseEntity<TaxResponse> updateTaxJson(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Tax ID", required = true)
            @PathVariable Integer taxId,
            @Valid @RequestBody TaxUpdateRequest updateRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        TaxResponse updatedTax = taxService.updateTax(tenantId, taxId, username, updateRequest);
        return ResponseEntity.ok(updatedTax);
    }
    
    @PutMapping(path = "/{taxId}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update a tax rule (Form)")
    public ResponseEntity<TaxResponse> updateTax(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Tax ID", required = true)
            @PathVariable Integer taxId,
            @Valid @ModelAttribute TaxUpdateRequest updateRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        TaxResponse updatedTax = taxService.updateTax(tenantId, taxId, username, updateRequest);
        return ResponseEntity.ok(updatedTax);
    }
    
    @DeleteMapping("/{taxId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete a tax rule")
    public ResponseEntity<Void> deleteTax(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Tax ID", required = true)
            @PathVariable Integer taxId) {
        taxService.deleteTax(tenantId, taxId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{taxId}/set-default")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Set tax as default for tenant")
    public ResponseEntity<TaxResponse> setAsDefaultTax(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Tax ID", required = true)
            @PathVariable Integer taxId,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        TaxResponse defaultTax = taxService.setAsDefaultTax(tenantId, taxId, username);
        return ResponseEntity.ok(defaultTax);
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get active taxes for tenant")
    public ResponseEntity<List<TaxResponse>> getActiveTaxes(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId) {
        return ResponseEntity.ok(taxService.getActiveTaxesByTenant(tenantId));
    }
    
    @GetMapping("/default")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get default tax for tenant")
    public ResponseEntity<TaxResponse> getDefaultTax(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId) {
        return ResponseEntity.ok(taxService.getDefaultTaxByTenant(tenantId));
    }
    
    @GetMapping("/valid")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get valid taxes for specific date")
    public ResponseEntity<List<TaxResponse>> getValidTaxesForDate(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(taxService.getValidTaxesForDate(tenantId, date));
    }
    
    @GetMapping("/valid/default")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get valid default tax for specific date")
    public ResponseEntity<TaxResponse> getValidDefaultTaxForDate(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(taxService.getValidDefaultTaxForDate(tenantId, date));
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Search taxes by keyword")
    public ResponseEntity<List<TaxResponse>> searchTaxes(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Search keyword", required = true)
            @RequestParam String keyword) {
        return ResponseEntity.ok(taxService.searchTaxes(tenantId, keyword));
    }
    
    @PostMapping("/calculate")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Calculate tax for base amount")
    public ResponseEntity<TaxCalculationResponse> calculateTax(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Base amount", required = true)
            @RequestParam BigDecimal baseAmount) {
        return ResponseEntity.ok(taxService.calculateTax(tenantId, baseAmount));
    }
    
    @PostMapping("/calculate/date")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Calculate tax for base amount on specific date")
    public ResponseEntity<TaxCalculationResponse> calculateTaxForDate(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Base amount", required = true)
            @RequestParam BigDecimal baseAmount,
            @Parameter(description = "Date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(taxService.calculateTaxForDate(tenantId, baseAmount, date));
    }
    
    @GetMapping("/calculate/default")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Calculate default tax amount only")
    public ResponseEntity<BigDecimal> calculateDefaultTaxAmount(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Base amount", required = true)
            @RequestParam BigDecimal baseAmount) {
        return ResponseEntity.ok(taxService.calculateDefaultTaxAmount(tenantId, baseAmount));
    }
    
    @GetMapping("/check/code/{code}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Check if tax code exists for tenant")
    public ResponseEntity<Boolean> checkTaxCodeExists(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Tax code", required = true)
            @PathVariable String code) {
        return ResponseEntity.ok(taxService.existsByTenantAndCode(tenantId, code));
    }
    
    @GetMapping("/count")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Count active taxes for tenant")
    public ResponseEntity<Long> countActiveTaxes(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId) {
        return ResponseEntity.ok(taxService.countActiveTaxesByTenant(tenantId));
    }
}
