package com.example.digitalestore.repository;

import com.example.digitalestore.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find product by tenant and product ID
    Optional<Product> findByTenantIdAndProductId(Integer tenantId, Long productId);
    
    // Find all products for a tenant
    List<Product> findByTenantId(Integer tenantId);
    
    // Find active products for a tenant
    List<Product> findByTenantIdAndStatus(Integer tenantId, String status);
    
    // Find products by category for a tenant
    List<Product> findByTenantIdAndCategoryId(Integer tenantId, Long categoryId);
    
    // Delete product by tenant and product ID
    void deleteByTenantIdAndProductId(Integer tenantId, Long productId);
}