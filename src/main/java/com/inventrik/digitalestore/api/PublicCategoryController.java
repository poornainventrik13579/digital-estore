package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.response.CategoryResponse;
import com.inventrik.digitalestore.service.category.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/tenants/{tenantId}/categories")
@RequiredArgsConstructor
@Tag(name = "Public Category Access", description = "Public APIs for browsing categories without authentication")
public class PublicCategoryController {

    private final CategoryService categoryService;
    
    @GetMapping
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories(@PathVariable Integer tenantId) {
        return ResponseEntity.ok(categoryService.getAllCategories(tenantId, null));
    }
    
    @GetMapping("/{categoryId}")
    @Operation(summary = "Get a category by ID")
    public ResponseEntity<CategoryResponse> getCategory(
            @PathVariable Integer tenantId,
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.getCategory(tenantId, categoryId));
    }
} 