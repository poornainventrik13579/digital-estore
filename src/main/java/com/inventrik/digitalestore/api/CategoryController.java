package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.CategoryRequest;
import com.inventrik.digitalestore.dto.request.CategoryUpdateRequest;
import com.inventrik.digitalestore.dto.response.CategoryResponse;
import com.inventrik.digitalestore.service.category.CategoryService;
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
@RequestMapping("/api/v1/tenants/{tenantId}/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "APIs for managing categories")
@SecurityRequirement(name = "oauth2")
public class CategoryController {

    private final CategoryService categoryService;
    
    /**
     * Get all categories with optional filtering
     *
     * Query parameters:
     * - status: Filter by status - "ACTIVE" or "INACTIVE" (optional)
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get all categories with optional status filter")
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @PathVariable Integer tenantId,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(categoryService.getAllCategories(tenantId, status));
    }
    
    @GetMapping("/{categoryId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get a category by ID")
    public ResponseEntity<CategoryResponse> getCategory(
            @PathVariable Integer tenantId,
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.getCategory(tenantId, categoryId));
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create a new category (JSON)")
    public ResponseEntity<CategoryResponse> createCategoryJson(
            @PathVariable Integer tenantId,
            @Valid @RequestBody CategoryRequest categoryRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        CategoryResponse createdCategory = categoryService.createCategory(tenantId, username, categoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create a new category (Form)")
    public ResponseEntity<CategoryResponse> createCategory(
            @PathVariable Integer tenantId,
            @Valid @ModelAttribute CategoryRequest categoryRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        CategoryResponse createdCategory = categoryService.createCategory(tenantId, username, categoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }
    
    @PutMapping(path = "/{categoryId}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update a category (JSON)")
    public ResponseEntity<CategoryResponse> updateCategoryJson(
            @PathVariable Integer tenantId,
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryUpdateRequest updateRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        CategoryResponse updatedCategory = categoryService.updateCategory(tenantId, categoryId, username, updateRequest);
        return ResponseEntity.ok(updatedCategory);
    }
    
    @PutMapping(path = "/{categoryId}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update a category (Form)")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Integer tenantId,
            @PathVariable Long categoryId,
            @Valid @ModelAttribute CategoryUpdateRequest updateRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        CategoryResponse updatedCategory = categoryService.updateCategory(tenantId, categoryId, username, updateRequest);
        return ResponseEntity.ok(updatedCategory);
    }
    
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete a category")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Integer tenantId,
            @PathVariable Long categoryId) {
        categoryService.deleteCategory(tenantId, categoryId);
        return ResponseEntity.noContent().build();
    }
}