package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.TaxRequest;
import com.inventrik.digitalestore.dto.request.TaxUpdateRequest;
import com.inventrik.digitalestore.dto.response.TaxCalculationResponse;
import com.inventrik.digitalestore.dto.response.TaxResponse;
import com.inventrik.digitalestore.security.TenantAccessValidator;
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
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/taxes")
@RequiredArgsConstructor
@Validated
@Tag(name = "Tax Management", description = "APIs for managing tax rules and calculations")
@SecurityRequirement(name = "oauth2")
public class TaxController {

    private final TaxService taxService;
    private final TenantAccessValidator tenantAccessValidator;
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get all taxes for a tenant")
    public ResponseEntity<List<TaxResponse>> getTaxesByTenant(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            Authentication authentication) {

        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(taxService.getTaxesByTenant(tenantId));
    }
    
    @GetMapping("/{taxId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get a specific tax")
    public ResponseEntity<TaxResponse> getTax(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Tax ID", required = true)
            @PathVariable Integer taxId,
            Authentication authentication) {

        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(taxService.getTax(tenantId, taxId));
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Create a new tax rule (JSON)")
    public ResponseEntity<TaxResponse> createTaxJson(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Valid @RequestBody TaxRequest taxRequest,
            Authentication authentication) {
        
        taxRequest.setTenantId(tenantId);
        String username = authentication.getName();
        TaxResponse createdTax = taxService.createTax(username, taxRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTax);
    }


    @PutMapping(path = "/{taxId}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Update a tax rule (JSON)")
    public ResponseEntity<TaxResponse> updateTaxJson(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Tax ID", required = true)
            @PathVariable Integer taxId,
            @Valid @RequestBody TaxUpdateRequest updateRequest,
            Authentication authentication) {

        String username = authentication.getName();
        TaxResponse updatedTax = taxService.updateTax(tenantId, taxId, username, updateRequest);
        return ResponseEntity.ok(updatedTax);
    }


    @DeleteMapping("/{taxId}")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Delete a tax rule")
    public ResponseEntity<Void> deleteTax(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Tax ID", required = true)
            @PathVariable Integer taxId) {
        taxService.deleteTax(tenantId, taxId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/default")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get default tax for tenant")
    public ResponseEntity<TaxResponse> getDefaultTax(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            Authentication authentication) {

        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(taxService.getDefaultTaxByTenant(tenantId));
    }

    @PostMapping("/calculate")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Calculate tax for base amount (optionally for a specific date)")
    public ResponseEntity<TaxCalculationResponse> calculateTax(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Base amount", required = true)
            @RequestParam @Positive BigDecimal baseAmount,
            @Parameter(description = "Date (YYYY-MM-DD) - optional", required = false)
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {

        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (date != null) {
            return ResponseEntity.ok(taxService.calculateTaxForDate(tenantId, baseAmount, date));
        }
        return ResponseEntity.ok(taxService.calculateTax(tenantId, baseAmount));
    }
}
