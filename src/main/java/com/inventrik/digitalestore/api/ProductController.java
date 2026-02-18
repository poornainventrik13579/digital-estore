package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.ProductRequest;
import com.inventrik.digitalestore.dto.request.ProductUpdateRequest;
import com.inventrik.digitalestore.dto.response.PagedResponse;
import com.inventrik.digitalestore.dto.response.ProductResponse;
import com.inventrik.digitalestore.security.TenantSecurity;
import com.inventrik.digitalestore.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "APIs for managing products")
@SecurityRequirement(name = "oauth2")
public class ProductController {

    private final ProductService productService;
    private final TenantSecurity tenantSecurity;
    
    /**
     * Get all products with optional filtering
     *
     * Query parameters:
     * - page: Page number (default: 0)
     * - size: Page size (default: 20)
     * - categoryId: Filter by category ID (optional)
     * - status: Filter by status - "ACTIVE" or "INACTIVE" (optional)
     * - keyword: Search by keyword (optional)
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get all products with optional filters (category, status, keyword)")
    public ResponseEntity<PagedResponse<ProductResponse>> getAllProducts(
            @PathVariable Integer tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            Authentication authentication) {

        // TODO: Uncomment when roles are properly configured in JWT
        // tenantSecurity.validateTenantAccess(authentication, tenantId);
        return ResponseEntity.ok(productService.getAllProductsPaginated(tenantId, page, size, categoryId, status, keyword));
    }
    
    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get a product by ID")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable Integer tenantId,
            @PathVariable String productId,
            Authentication authentication) {

        // TODO: Uncomment when roles are properly configured in JWT
        // tenantSecurity.validateTenantAccess(authentication, tenantId);
        return ResponseEntity.ok(productService.getProduct(tenantId, productId));
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Create a new product (JSON)")
    public ResponseEntity<ProductResponse> createProductJson(
            @PathVariable Integer tenantId,
            @Valid @RequestBody ProductRequest productRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        ProductResponse createdProduct = productService.createProduct(tenantId, username, productRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }
    
    @PutMapping(path = "/{productId}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Update a product (JSON)")
    public ResponseEntity<ProductResponse> updateProductJson(
            @PathVariable Integer tenantId,
            @PathVariable String productId,
            @Valid @RequestBody ProductUpdateRequest updateRequest,
            Authentication authentication) {

        String username = (authentication != null) ? authentication.getName() : "system";
        ProductResponse updatedProduct = productService.updateProduct(tenantId, productId, username, updateRequest);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Delete a product")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Integer tenantId,
            @PathVariable String productId,
            Authentication authentication) {
        productService.deleteProduct(tenantId, productId);
        return ResponseEntity.noContent().build();
    }
}