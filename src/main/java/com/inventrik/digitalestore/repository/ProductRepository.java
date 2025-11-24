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
    
    Optional<Product> findByTenantIdAndProductId(Integer tenantId, Long productId);
    
    Optional<Product> findByTenantIdAndProductIdAndStatus(Integer tenantId, Long productId, String status);
    
    List<Product> findByTenantId(Integer tenantId);
    
    Page<Product> findByTenantId(Integer tenantId, Pageable pageable);
    
    List<Product> findByTenantIdAndStatus(Integer tenantId, String status);
    
    List<Product> findByTenantIdAndCategoryId(Integer tenantId, Long categoryId);
    
    void deleteByTenantIdAndProductId(Integer tenantId, Long productId);
    
    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId " +
           "AND (LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeyword(@Param("tenantId") Integer tenantId, 
                                 @Param("keyword") String keyword, 
                                 Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId " +
           "AND p.status = '0' " +
           "AND (LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchActiveByKeyword(@Param("tenantId") Integer tenantId, 
                                       @Param("keyword") String keyword, 
                                       Pageable pageable);
}
