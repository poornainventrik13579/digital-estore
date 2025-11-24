package com.inventrik.digitalestore.service.category;

import com.inventrik.digitalestore.dto.request.CategoryRequest;
import com.inventrik.digitalestore.dto.request.CategoryUpdateRequest;
import com.inventrik.digitalestore.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    
    List<CategoryResponse> getAllCategories(Integer tenantId);
    
    CategoryResponse getCategory(Integer tenantId, Long categoryId);
    
    CategoryResponse createCategory(Integer tenantId, String username, CategoryRequest categoryRequest);
    
    CategoryResponse updateCategory(Integer tenantId, Long categoryId, String username, CategoryUpdateRequest updateRequest);
    
    void deleteCategory(Integer tenantId, Long categoryId);
    
    List<CategoryResponse> getActiveCategories(Integer tenantId);
}