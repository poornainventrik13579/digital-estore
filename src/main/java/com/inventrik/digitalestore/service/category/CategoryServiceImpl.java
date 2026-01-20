package com.inventrik.digitalestore.service.category;

import com.inventrik.digitalestore.domain.category.Category;
import com.inventrik.digitalestore.dto.request.CategoryRequest;
import com.inventrik.digitalestore.dto.request.CategoryUpdateRequest;
import com.inventrik.digitalestore.dto.response.CategoryResponse;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.exception.ValidationException;
import com.inventrik.digitalestore.repository.CategoryRepository;
import com.inventrik.digitalestore.repository.TenantRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import com.inventrik.digitalestore.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final IdGeneratorService idGeneratorService;
    private final UserService userService;
    private final TenantRepository tenantRepository;
    
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
    public List<CategoryResponse> getAllCategories(Integer tenantId, String status) {
        if (status != null && !status.trim().isEmpty()) {
            String statusCode = "ACTIVE".equalsIgnoreCase(status) ? "0" : "1";
            return categoryRepository.findByTenantIdAndStatus(tenantId, statusCode).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        return categoryRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public CategoryResponse getCategory(Integer tenantId, String categoryId) {
        Category category = categoryRepository.findByTenantIdAndCategoryId(tenantId, categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        return mapToDTO(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(Integer tenantId, String username, CategoryRequest categoryRequest) {
        tenantRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

        String newCategoryId = idGeneratorService.generateId(tenantId, "CATEGORY");

        Category category = new Category();
        category.setTenantId(tenantId);
        category.setCategoryId(newCategoryId);
        category.setCategoryName(categoryRequest.getCategoryName());
        category.setDescription(categoryRequest.getDescription());
        category.setStatus("0"); // Active status
        category.setProducts(new ArrayList<>()); // Initialize empty products list
        
        // Ensure username is truncated to 2 characters as per DB schema
        category.setCreatedBy(userService.getAuditCode(username));
        category.setUpdatedBy(userService.getAuditCode(username));
        category.setCreated(LocalDateTime.now());
        category.setUpdated(LocalDateTime.now());
        
        Category savedCategory = categoryRepository.save(category);
        
        return mapToDTO(savedCategory);
    }
    
    @Override
    @Transactional
    public CategoryResponse updateCategory(Integer tenantId, String categoryId, String username, CategoryUpdateRequest updateRequest) {
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
        category.setUpdatedBy(username);
        category.setUpdated(LocalDateTime.now());
        
        Category updatedCategory = categoryRepository.save(category);
        
        return mapToDTO(updatedCategory);
    }
    
    @Override
    @Transactional
    public void deleteCategory(Integer tenantId, String categoryId) {
        Category category = categoryRepository.findByTenantIdAndCategoryId(tenantId, categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        // Prevent deletion if category has products
        if (!category.getProducts().isEmpty()) {
            throw new ValidationException("Cannot delete category with existing products. Please reassign or delete products first.");
        }

        categoryRepository.deleteByTenantIdAndCategoryId(tenantId, categoryId);
    }
}