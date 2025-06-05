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
    
    @GetMapping
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories(@PathVariable Integer tenantId) {
        return ResponseEntity.ok(categoryService.getAllCategories(tenantId));
    }
    
    @GetMapping("/{categoryId}")
    @Operation(summary = "Get a category by ID")
    public ResponseEntity<CategoryResponse> getCategory(
            @PathVariable Integer tenantId,
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.getCategory(tenantId, categoryId));
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Create a new category")
    public ResponseEntity<CategoryResponse> createCategory(
            @PathVariable Integer tenantId,
            @Valid @ModelAttribute CategoryRequest categoryRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        CategoryResponse createdCategory = categoryService.createCategory(tenantId, username, categoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }
    
    @PutMapping(path = "/{categoryId}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Update a category")
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
    @Operation(summary = "Delete a category")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Integer tenantId,
            @PathVariable Long categoryId) {
        categoryService.deleteCategory(tenantId, categoryId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get active categories")
    public ResponseEntity<List<CategoryResponse>> getActiveCategories(@PathVariable Integer tenantId) {
        return ResponseEntity.ok(categoryService.getActiveCategories(tenantId));
    }
}