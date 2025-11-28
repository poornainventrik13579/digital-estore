package com.inventrik.digitalestore.service.category;

import com.inventrik.digitalestore.dto.request.CategoryRequest;
import com.inventrik.digitalestore.dto.request.CategoryUpdateRequest;
import com.inventrik.digitalestore.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    // Get all categories for a tenant with optional status filter
    List<CategoryResponse> getAllCategories(Integer tenantId, String status);

    // Get a single category by ID
    CategoryResponse getCategory(Integer tenantId, Long categoryId);

    // Create a new category
    CategoryResponse createCategory(Integer tenantId, String username, CategoryRequest categoryRequest);

    // Update an existing category
    CategoryResponse updateCategory(Integer tenantId, Long categoryId, String username, CategoryUpdateRequest updateRequest);

    // Delete a category
    void deleteCategory(Integer tenantId, Long categoryId);
}