package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.response.CategoryResponse;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.service.category.CategoryService;
import com.inventrik.digitalestore.service.tenant.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/tenants/{tenantId}/categories")
@RequiredArgsConstructor
@Tag(name = "Public Category Access", description = "Public APIs for browsing categories without authentication")
public class PublicCategoryController {

    private final CategoryService categoryService;
    private final TenantService tenantService;
    
    private boolean validateTenant(Integer tenantId) {
        try {
            var tenant = tenantService.getTenant(tenantId);
            return "A".equals(tenant.getStatus());
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }
    
    @GetMapping
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories(@PathVariable Integer tenantId) {
        if (!validateTenant(tenantId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(categoryService.getAllCategories(tenantId));
    }
    
    @GetMapping("/{categoryId}")
    @Operation(summary = "Get a category by ID")
    public ResponseEntity<CategoryResponse> getCategory(
            @PathVariable Integer tenantId,
            @PathVariable Long categoryId) {
        if (!validateTenant(tenantId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(categoryService.getCategory(tenantId, categoryId));
    }
} 