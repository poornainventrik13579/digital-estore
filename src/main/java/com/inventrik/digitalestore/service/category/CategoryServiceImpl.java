package com.inventrik.digitalestore.service.category;

import com.inventrik.digitalestore.domain.category.Category;
import com.inventrik.digitalestore.dto.request.CategoryRequest;
import com.inventrik.digitalestore.dto.request.CategoryUpdateRequest;
import com.inventrik.digitalestore.dto.response.CategoryResponse;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    // Utility method to convert Entity to DTO
    private CategoryResponse mapToDTO(Category category) {
        return new CategoryResponse(
            category.getCategoryId(),
            category.getTenantId(),
            category.getCategoryName(),
            category.getDescription(),
            category.getStatus(),
            category.getCreated(),
            category.getUpdated()
        );
    }
    
    @Override
    public List<CategoryResponse> getAllCategories(Integer tenantId) {
        return categoryRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public CategoryResponse getCategory(Integer tenantId, Long categoryId) {
        Category category = categoryRepository.findByTenantIdAndCategoryId(tenantId, categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        return mapToDTO(category);
    }
    
    @Override
    @Transactional
    public CategoryResponse createCategory(Integer tenantId, String username, CategoryRequest categoryRequest) {
        // Generate a new category ID (in production, use a better ID generation strategy)
        Long newCategoryId = System.currentTimeMillis();
        
        Category category = new Category();
        category.setTenantId(tenantId);
        category.setCategoryId(newCategoryId);
        category.setCategoryName(categoryRequest.getCategoryName());
        category.setDescription(categoryRequest.getDescription());
        category.setStatus("0"); // Active status
        
        // Ensure username is truncated to 2 characters as per DB schema
        category.setCreatedBy(username.length() > 2 ? username.substring(0, 2) : username);
        category.setUpdatedBy(username.length() > 2 ? username.substring(0, 2) : username);
        category.setCreated(LocalDateTime.now());
        category.setUpdated(LocalDateTime.now());
        
        Category savedCategory = categoryRepository.save(category);
        
        return mapToDTO(savedCategory);
    }
    
    @Override
    @Transactional
    public CategoryResponse updateCategory(Integer tenantId, Long categoryId, String username, CategoryUpdateRequest updateRequest) {
        Category category = categoryRepository.findByTenantIdAndCategoryId(tenantId, categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        
        if (updateRequest.getCategoryName() != null) {
            category.setCategoryName(updateRequest.getCategoryName());
        }
        if (updateRequest.getDescription() != null) {
            category.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getStatus() != null) {
            category.setStatus(updateRequest.getStatus());
        }
        
        // Ensure username is truncated to 2 characters as per DB schema
        category.setUpdatedBy(username.length() > 2 ? username.substring(0, 2) : username);
        category.setUpdated(LocalDateTime.now());
        
        Category updatedCategory = categoryRepository.save(category);
        
        return mapToDTO(updatedCategory);
    }
    
    @Override
    @Transactional
    public void deleteCategory(Integer tenantId, Long categoryId) {
        // Check if category exists
        if (!categoryRepository.findByTenantIdAndCategoryId(tenantId, categoryId).isPresent()) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        
        categoryRepository.deleteByTenantIdAndCategoryId(tenantId, categoryId);
    }
    
    @Override
    public List<CategoryResponse> getActiveCategories(Integer tenantId) {
        return categoryRepository.findByTenantIdAndStatus(tenantId, "0").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}