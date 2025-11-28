package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.response.PagedResponse;
import com.inventrik.digitalestore.dto.response.ProductResponse;
import com.inventrik.digitalestore.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/tenants/{tenantId}/products")
@RequiredArgsConstructor
@Tag(name = "Public Product Access", description = "Public APIs for browsing products without authentication")
public class PublicProductController {

    private final ProductService productService;
    
    @GetMapping
    @Operation(summary = "Get all products with pagination")
    public ResponseEntity<PagedResponse<ProductResponse>> getAllProducts(
            @PathVariable Integer tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.getAllProductsPaginated(tenantId, page, size, null, null, null));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get a product by ID")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable Integer tenantId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProduct(tenantId, productId));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<PagedResponse<ProductResponse>> getProductsByCategory(
            @PathVariable Integer tenantId,
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getAllProductsPaginated(tenantId, 0, Integer.MAX_VALUE, categoryId, null, null));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active products")
    public ResponseEntity<PagedResponse<ProductResponse>> getActiveProducts(@PathVariable Integer tenantId) {
        return ResponseEntity.ok(productService.getAllProductsPaginated(tenantId, 0, Integer.MAX_VALUE, null, "ACTIVE", null));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by keyword")
    public ResponseEntity<PagedResponse<ProductResponse>> searchProducts(
            @PathVariable Integer tenantId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.getAllProductsPaginated(tenantId, page, size, null, null, keyword));
    }
} 