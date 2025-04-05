package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.ProductRequest;
import com.inventrik.digitalestore.dto.request.ProductUpdateRequest;
import com.inventrik.digitalestore.dto.response.ProductResponse;
import com.inventrik.digitalestore.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {

    private final ProductService productService;
    
    @GetMapping
    @Operation(summary = "Get all products")
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @PathVariable Integer tenantId) {
        return ResponseEntity.ok(productService.getAllProducts(tenantId));
    }
    
    @GetMapping("/{productId}")
    @Operation(summary = "Get a product by ID")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable Integer tenantId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProduct(tenantId, productId));
    }
    
    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> createProduct(
            @PathVariable Integer tenantId,
            @Valid @RequestBody ProductRequest productRequest,
            Authentication authentication) {
        
        // Get username from authentication or use a default
        String username = (authentication != null) ? authentication.getName() : "system";
        
        ProductResponse createdProduct = productService.createProduct(tenantId, username, productRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }
    
    @PutMapping("/{productId}")
    @Operation(summary = "Update a product")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Integer tenantId,
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest updateRequest,
            Authentication authentication) {
        
        // Get username from authentication or use a default
        String username = (authentication != null) ? authentication.getName() : "system";
        
        ProductResponse updatedProduct = productService.updateProduct(tenantId, productId, username, updateRequest);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @DeleteMapping("/{productId}")
    @Operation(summary = "Delete a product")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Integer tenantId,
            @PathVariable Long productId) {
        productService.deleteProduct(tenantId, productId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(
            @PathVariable Integer tenantId,
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(tenantId, categoryId));
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get active products")
    public ResponseEntity<List<ProductResponse>> getActiveProducts(
            @PathVariable Integer tenantId) {
        return ResponseEntity.ok(productService.getActiveProducts(tenantId));
    }
}
