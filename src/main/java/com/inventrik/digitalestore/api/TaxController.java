package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.TaxRequest;
import com.inventrik.digitalestore.dto.response.TaxResponse;
import com.inventrik.digitalestore.service.tax.TaxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/taxes")
@RequiredArgsConstructor
@Tag(name = "Tax Management")
@SecurityRequirement(name = "oauth2")
public class TaxController {

    private final TaxService taxService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get taxes for tenant with optional filters")
    public ResponseEntity<List<TaxResponse>> getAllTaxes(
            @PathVariable Integer tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String defaultFlag) {
        return ResponseEntity.ok(taxService.getAllTaxes(tenantId, status, defaultFlag));
    }

    @GetMapping("/{taxId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get tax by ID")
    public ResponseEntity<TaxResponse> getTax(
            @PathVariable Integer tenantId,
            @PathVariable String taxId) {
        return ResponseEntity.ok(taxService.getTax(tenantId, taxId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Create new tax")
    public ResponseEntity<TaxResponse> createTax(
            @PathVariable Integer tenantId,
            @Valid @RequestBody TaxRequest request,
            Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "system";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taxService.createTax(tenantId, request, username));
    }

    @PutMapping("/{taxId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Update tax")
    public ResponseEntity<TaxResponse> updateTax(
            @PathVariable Integer tenantId,
            @PathVariable String taxId,
            @Valid @RequestBody TaxRequest request,
            Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "system";
        return ResponseEntity.ok(taxService.updateTax(tenantId, taxId, request, username));
    }

    @DeleteMapping("/{taxId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Delete tax")
    public ResponseEntity<Void> deleteTax(
            @PathVariable Integer tenantId,
            @PathVariable String taxId) {
        taxService.deleteTax(tenantId, taxId);
        return ResponseEntity.noContent().build();
    }
}
