package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.bundle.ProductBundle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductBundleRepository extends JpaRepository<ProductBundle, ProductBundle.ProductBundlePK> {
    
    List<ProductBundle> findByTenantIdAndStatus(Integer tenantId, String status);
    
    Optional<ProductBundle> findByTenantIdAndBundleIdAndStatus(Integer tenantId, Long bundleId, String status);
    
    List<ProductBundle> findByTenantId(Integer tenantId);
    
    Optional<ProductBundle> findByTenantIdAndBundleId(Integer tenantId, Long bundleId);
    
    @Query("SELECT pb FROM ProductBundle pb WHERE pb.tenantId = :tenantId AND pb.status = '0' ORDER BY pb.created DESC")
    List<ProductBundle> findActiveBundles(@Param("tenantId") Integer tenantId);
    
    @Query("SELECT pb FROM ProductBundle pb WHERE pb.tenantId = :tenantId AND pb.bundleName LIKE %:name% AND pb.status = '0'")
    List<ProductBundle> findByBundleNameContaining(@Param("tenantId") Integer tenantId, @Param("name") String name);
    
    @Query("SELECT COUNT(pb) FROM ProductBundle pb WHERE pb.tenantId = :tenantId AND pb.status = '0'")
    Long countActiveBundles(@Param("tenantId") Integer tenantId);
} 