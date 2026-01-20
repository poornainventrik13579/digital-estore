package com.inventrik.digitalestore.service.category;

import com.inventrik.digitalestore.dto.request.CategoryRequest;
import com.inventrik.digitalestore.dto.request.CategoryUpdateRequest;
import com.inventrik.digitalestore.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getAllCategories(Integer tenantId, String status);

    CategoryResponse getCategory(Integer tenantId, String categoryId);

    CategoryResponse createCategory(Integer tenantId, String username, CategoryRequest categoryRequest);

    CategoryResponse updateCategory(Integer tenantId, String categoryId, String username, CategoryUpdateRequest updateRequest);

    void deleteCategory(Integer tenantId, String categoryId);
}