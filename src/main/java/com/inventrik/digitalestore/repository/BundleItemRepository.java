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
    
    List<BundleItem> findByTenantIdAndBundleIdAndStatus(Integer tenantId, String bundleId, String status);

    List<BundleItem> findByTenantIdAndProductIdAndStatus(Integer tenantId, String productId, String status);

    Optional<BundleItem> findByTenantIdAndBundleItemIdAndStatus(Integer tenantId, String bundleItemId, String status);

    List<BundleItem> findByTenantIdAndBundleId(Integer tenantId, String bundleId);

    @Query("SELECT bi FROM BundleItem bi WHERE bi.tenantId = :tenantId AND bi.bundleId = :bundleId AND bi.status = '0'")
    List<BundleItem> findActiveBundleItems(@Param("tenantId") Integer tenantId, @Param("bundleId") String bundleId);

    @Query("SELECT COUNT(bi) FROM BundleItem bi WHERE bi.tenantId = :tenantId AND bi.bundleId = :bundleId AND bi.status = '0'")
    Integer countActiveBundleItems(@Param("tenantId") Integer tenantId, @Param("bundleId") String bundleId);

    @Query("SELECT bi FROM BundleItem bi WHERE bi.tenantId = :tenantId AND bi.productId = :productId AND bi.status = '0'")
    List<BundleItem> findBundlesContainingProduct(@Param("tenantId") Integer tenantId, @Param("productId") String productId);

    void deleteByTenantIdAndBundleIdAndStatus(Integer tenantId, String bundleId, String status);
} 