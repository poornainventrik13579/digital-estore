package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.bundle.BundleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BundleItemRepository extends JpaRepository<BundleItem, BundleItem.BundleItemPK> {
    
    List<BundleItem> findByTenantIdAndBundleIdAndStatus(Integer tenantId, Long bundleId, String status);
    
    List<BundleItem> findByTenantIdAndProductIdAndStatus(Integer tenantId, Long productId, String status);
    
    Optional<BundleItem> findByTenantIdAndBundleItemIdAndStatus(Integer tenantId, Long bundleItemId, String status);
    
    List<BundleItem> findByTenantIdAndBundleId(Integer tenantId, Long bundleId);
    
    @Query("SELECT bi FROM BundleItem bi WHERE bi.tenantId = :tenantId AND bi.bundleId = :bundleId AND bi.status = '0'")
    List<BundleItem> findActiveBundleItems(@Param("tenantId") Integer tenantId, @Param("bundleId") Long bundleId);
    
    @Query("SELECT COUNT(bi) FROM BundleItem bi WHERE bi.tenantId = :tenantId AND bi.bundleId = :bundleId AND bi.status = '0'")
    Long countActiveBundleItems(@Param("tenantId") Integer tenantId, @Param("bundleId") Long bundleId);
    
    @Query("SELECT bi FROM BundleItem bi WHERE bi.tenantId = :tenantId AND bi.productId = :productId AND bi.status = '0'")
    List<BundleItem> findBundlesContainingProduct(@Param("tenantId") Integer tenantId, @Param("productId") Long productId);
    
    void deleteByTenantIdAndBundleIdAndStatus(Integer tenantId, Long bundleId, String status);
} 