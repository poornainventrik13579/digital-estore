package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByTenantIdAndCategoryId(Integer tenantId, Long categoryId);
    
    List<Category> findByTenantId(Integer tenantId);
    
    List<Category> findByTenantIdAndStatus(Integer tenantId, String status);
    
    void deleteByTenantIdAndCategoryId(Integer tenantId, Long categoryId);
    
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.products WHERE c.tenantId = :tenantId")
    List<Category> findByTenantIdWithProducts(@Param("tenantId") Integer tenantId);
}