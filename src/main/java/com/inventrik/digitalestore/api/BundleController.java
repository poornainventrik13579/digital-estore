package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.BundleRequest;
import com.inventrik.digitalestore.dto.response.BundleResponse;
import com.inventrik.digitalestore.security.TenantAccessValidator;
import com.inventrik.digitalestore.service.bundle.BundleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/bundles")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Bundle Management", description = "APIs for managing product bundles")
public class BundleController {
    
    private final BundleService bundleService;
    private final TenantAccessValidator tenantAccessValidator;
    
    @GetMapping
    @Operation(summary = "Get all bundles", description = "Retrieve all product bundles for a tenant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved bundles"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<BundleResponse>> getAllBundles(
            @PathVariable Integer tenantId,
            Authentication authentication) {

        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Getting all bundles for tenant: {}", tenantId);
        List<BundleResponse> bundles = bundleService.getAllBundles(tenantId);
        return ResponseEntity.ok(bundles);
    }
    
    @GetMapping("/{bundleId}")
    @Operation(summary = "Get bundle by ID", description = "Retrieve a specific product bundle by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved bundle"),
        @ApiResponse(responseCode = "404", description = "Bundle not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<BundleResponse> getBundle(
            @Parameter(description = "Tenant ID") @PathVariable Integer tenantId,
            @Parameter(description = "Bundle ID") @PathVariable Long bundleId,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        log.info("Getting bundle {} for tenant: {}", bundleId, tenantId);
        BundleResponse bundle = bundleService.getBundle(tenantId, bundleId);
        return ResponseEntity.ok(bundle);
    }
    
    @PostMapping
    @Operation(summary = "Create a new bundle", description = "Create a new product bundle")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Bundle created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    public ResponseEntity<BundleResponse> createBundle(
            @Parameter(description = "Tenant ID") @PathVariable Integer tenantId,
            @Valid @RequestBody BundleRequest bundleRequest,
            Authentication authentication) {

        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String createdBy = authentication.getName();
        log.info("Creating bundle for tenant: {} by user: {}", tenantId, createdBy);
        BundleResponse bundle = bundleService.createBundle(tenantId, bundleRequest, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(bundle);
    }
    
    @PutMapping("/{bundleId}")
    @Operation(summary = "Update a bundle", description = "Update an existing product bundle")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bundle updated successfully"),
        @ApiResponse(responseCode = "404", description = "Bundle not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    public ResponseEntity<BundleResponse> updateBundle(
            @Parameter(description = "Tenant ID") @PathVariable Integer tenantId,
            @Parameter(description = "Bundle ID") @PathVariable Long bundleId,
            @Valid @RequestBody BundleRequest bundleRequest,
            Authentication authentication) {

        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String updatedBy = authentication.getName();
        log.info("Updating bundle {} for tenant: {} by user: {}", bundleId, tenantId, updatedBy);
        BundleResponse bundle = bundleService.updateBundle(tenantId, bundleId, bundleRequest, updatedBy);
        return ResponseEntity.ok(bundle);
    }
    
    @DeleteMapping("/{bundleId}")
    @Operation(summary = "Delete a bundle", description = "Delete a product bundle")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Bundle deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Bundle not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    public ResponseEntity<Void> deleteBundle(
            @Parameter(description = "Tenant ID") @PathVariable Integer tenantId,
            @Parameter(description = "Bundle ID") @PathVariable Long bundleId,
            Authentication authentication) {

        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String deletedBy = authentication.getName();
        log.info("Deleting bundle {} for tenant: {} by user: {}", bundleId, tenantId, deletedBy);
        bundleService.deleteBundle(tenantId, bundleId, deletedBy);
        return ResponseEntity.noContent().build();
    }
} 