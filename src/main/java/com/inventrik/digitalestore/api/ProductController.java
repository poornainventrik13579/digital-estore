package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.ProductRequest;
import com.inventrik.digitalestore.dto.request.ProductUpdateRequest;
import com.inventrik.digitalestore.dto.response.PagedResponse;
import com.inventrik.digitalestore.dto.response.ProductResponse;
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
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get all products with pagination")
    public ResponseEntity<PagedResponse<ProductResponse>> getAllProducts(
            @PathVariable Integer tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<ProductResponse> products = productService.getAllProductsPaginated(tenantId, page, size);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{productId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get a product by ID")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable Integer tenantId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProduct(tenantId, productId));
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create a new product (JSON)")
    public ResponseEntity<ProductResponse> createProductJson(
            @PathVariable Integer tenantId,
            @Valid @RequestBody ProductRequest productRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        ProductResponse createdProduct = productService.createProduct(tenantId, username, productRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create a new product (Form)")
    public ResponseEntity<ProductResponse> createProduct(
            @PathVariable Integer tenantId,
            @Valid @ModelAttribute ProductRequest productRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        ProductResponse createdProduct = productService.createProduct(tenantId, username, productRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }
    
    @PutMapping(path = "/{productId}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update a product (JSON)")
    public ResponseEntity<ProductResponse> updateProductJson(
            @PathVariable Integer tenantId,
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest updateRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        ProductResponse updatedProduct = productService.updateProduct(tenantId, productId, username, updateRequest);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @PutMapping(path = "/{productId}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update a product (Form)")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Integer tenantId,
            @PathVariable Long productId,
            @Valid @ModelAttribute ProductUpdateRequest updateRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        ProductResponse updatedProduct = productService.updateProduct(tenantId, productId, username, updateRequest);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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
    public ResponseEntity<List<ProductResponse>> getActiveProducts(@PathVariable Integer tenantId) {
        return ResponseEntity.ok(productService.getActiveProducts(tenantId));
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Search products by keyword")
    public ResponseEntity<PagedResponse<ProductResponse>> searchProducts(
            @PathVariable Integer tenantId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.searchProducts(tenantId, keyword, page, size));
    }
}