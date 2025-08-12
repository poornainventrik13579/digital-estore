package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find product by tenant and product ID
    Optional<Product> findByTenantIdAndProductId(Integer tenantId, Long productId);
    
    // Find product by tenant, product ID and status
    Optional<Product> findByTenantIdAndProductIdAndStatus(Integer tenantId, Long productId, String status);
    
    // Find all products for a tenant
    List<Product> findByTenantId(Integer tenantId);
    
    // Find all products for a tenant with pagination
    Page<Product> findByTenantId(Integer tenantId, Pageable pageable);
    
    // Find active products for a tenant
    List<Product> findByTenantIdAndStatus(Integer tenantId, String status);
    
    // Find products by category for a tenant
    List<Product> findByTenantIdAndCategoryId(Integer tenantId, Long categoryId);
    
    // Delete product by tenant and product ID
    void deleteByTenantIdAndProductId(Integer tenantId, Long productId);
    
    // Search products by keyword (name or description)
    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId " +
           "AND (LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeyword(@Param("tenantId") Integer tenantId, 
                                 @Param("keyword") String keyword, 
                                 Pageable pageable);
    
    // Search active products by keyword
    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId " +
           "AND p.status = '0' " +
           "AND (LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchActiveByKeyword(@Param("tenantId") Integer tenantId, 
                                       @Param("keyword") String keyword, 
                                       Pageable pageable);
}
