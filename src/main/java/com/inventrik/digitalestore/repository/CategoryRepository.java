package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Find category by tenant and category ID
    Optional<Category> findByTenantIdAndCategoryId(Integer tenantId, Long categoryId);
    
    // Find all categories for a tenant
    List<Category> findByTenantId(Integer tenantId);
    
    // Find active categories for a tenant
    List<Category> findByTenantIdAndStatus(Integer tenantId, String status);
    
    // Delete category by tenant and category ID
    void deleteByTenantIdAndCategoryId(Integer tenantId, Long categoryId);
}