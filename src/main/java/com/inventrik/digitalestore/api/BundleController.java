package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.BundleRequest;
import com.inventrik.digitalestore.dto.response.BundleResponse;
import com.inventrik.digitalestore.service.bundle.BundleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/bundles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bundle Management", description = "APIs for managing product bundles")
public class BundleController {
    
    private final BundleService bundleService;
    
    @GetMapping
    @Operation(summary = "Get bundles with optional filters: ?status=ACTIVE or ?name={name} or ?productId={id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'TENANT')")
    public ResponseEntity<List<BundleResponse>> getAllBundles(
            @PathVariable Integer tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long productId) {

        log.info("Getting bundles for tenant {} with filters - status: {}, name: {}, productId: {}",
                 tenantId, status, name, productId);
        return ResponseEntity.ok(bundleService.getAllBundles(tenantId, status, name, productId));
    }
    
    @GetMapping("/{bundleId}")
    @Operation(summary = "Get bundle by ID", description = "Retrieve a specific product bundle by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved bundle"),
        @ApiResponse(responseCode = "404", description = "Bundle not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'TENANT')")
    public ResponseEntity<BundleResponse> getBundle(
            @Parameter(description = "Tenant ID") @PathVariable Integer tenantId,
            @Parameter(description = "Bundle ID") @PathVariable Long bundleId) {
        
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
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'TENANT')")
    public ResponseEntity<BundleResponse> createBundle(
            @Parameter(description = "Tenant ID") @PathVariable Integer tenantId,
            @Valid @RequestBody BundleRequest bundleRequest,
            Authentication authentication) {
        
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
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'TENANT')")
    public ResponseEntity<BundleResponse> updateBundle(
            @Parameter(description = "Tenant ID") @PathVariable Integer tenantId,
            @Parameter(description = "Bundle ID") @PathVariable Long bundleId,
            @Valid @RequestBody BundleRequest bundleRequest,
            Authentication authentication) {
        
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
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'TENANT')")
    public ResponseEntity<Void> deleteBundle(
            @Parameter(description = "Tenant ID") @PathVariable Integer tenantId,
            @Parameter(description = "Bundle ID") @PathVariable Long bundleId,
            Authentication authentication) {
        
        String deletedBy = authentication.getName();
        log.info("Deleting bundle {} for tenant: {} by user: {}", bundleId, tenantId, deletedBy);
        bundleService.deleteBundle(tenantId, bundleId, deletedBy);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/calculate-price")
    @Operation(summary = "Calculate bundle price", description = "Calculate total price for bundle items")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully calculated price"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'TENANT')")
    public ResponseEntity<BigDecimal> calculateBundlePrice(
            @Parameter(description = "Tenant ID") @PathVariable Integer tenantId,
            @Valid @RequestBody List<BundleRequest.BundleItemRequest> bundleItems) {
        
        log.info("Calculating bundle price: tenantId={}", tenantId);
        BigDecimal price = bundleService.calculateBundlePrice(tenantId, bundleItems);
        return ResponseEntity.ok(price);
    }
    
    @PostMapping("/{bundleId}/products/{productId}")
    @Operation(summary = "Add product to bundle", description = "Add a product to an existing bundle")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product added to bundle successfully"),
        @ApiResponse(responseCode = "404", description = "Bundle or product not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'TENANT')")
    public ResponseEntity<BundleResponse> addProductToBundle(
            @Parameter(description = "Tenant ID") @PathVariable Integer tenantId,
            @Parameter(description = "Bundle ID") @PathVariable Long bundleId,
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Quantity") @RequestParam(defaultValue = "1") Integer quantity,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "system";
        log.info("Adding product to bundle: bundleId={}, productId={}, quantity={}, tenantId={}, username={}", 
                bundleId, productId, quantity, tenantId, username);
        
        BundleResponse bundle = bundleService.addProductToBundle(tenantId, bundleId, productId, quantity, username);
        return ResponseEntity.ok(bundle);
    }
    
    @DeleteMapping("/{bundleId}/products/{productId}")
    @Operation(summary = "Remove product from bundle", description = "Remove a product from a bundle")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product removed from bundle successfully"),
        @ApiResponse(responseCode = "404", description = "Bundle or product not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'TENANT')")
    public ResponseEntity<BundleResponse> removeProductFromBundle(
            @Parameter(description = "Tenant ID") @PathVariable Integer tenantId,
            @Parameter(description = "Bundle ID") @PathVariable Long bundleId,
            @Parameter(description = "Product ID") @PathVariable Long productId,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "system";
        log.info("Removing product from bundle: bundleId={}, productId={}, tenantId={}, username={}", 
                bundleId, productId, tenantId, username);
        
        BundleResponse bundle = bundleService.removeProductFromBundle(tenantId, bundleId, productId, username);
        return ResponseEntity.ok(bundle);
    }
    
    @PutMapping("/{bundleId}/products/{productId}/quantity")
    @Operation(summary = "Update product quantity in bundle", description = "Update the quantity of a product in a bundle")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product quantity updated successfully"),
        @ApiResponse(responseCode = "404", description = "Bundle or product not found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'TENANT')")
    public ResponseEntity<BundleResponse> updateProductQuantityInBundle(
            @Parameter(description = "Tenant ID") @PathVariable Integer tenantId,
            @Parameter(description = "Bundle ID") @PathVariable Long bundleId,
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "New quantity") @RequestParam Integer quantity,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "system";
        log.info("Updating product quantity in bundle: bundleId={}, productId={}, quantity={}, tenantId={}, username={}", 
                bundleId, productId, quantity, tenantId, username);
        
        BundleResponse bundle = bundleService.updateProductQuantityInBundle(tenantId, bundleId, productId, quantity, username);
        return ResponseEntity.ok(bundle);
    }
    
    @GetMapping("/count")
    @Operation(summary = "Get bundle count", description = "Get the count of active bundles for a tenant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved bundle count"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'TENANT')")
    public ResponseEntity<Long> getBundleCount(
            @Parameter(description = "Tenant ID") @PathVariable Integer tenantId) {
        
        log.info("Getting bundle count for tenant: {}", tenantId);
        Long count = bundleService.getBundleCount(tenantId);
        return ResponseEntity.ok(count);
    }
} 