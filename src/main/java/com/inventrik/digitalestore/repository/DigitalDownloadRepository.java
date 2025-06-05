package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.download.DigitalDownload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DigitalDownloadRepository extends JpaRepository<DigitalDownload, Long> {
    
    // FIXED: Use composite key for OrderItem lookup
    List<DigitalDownload> findByTenantIdAndOrderIdAndOrderItemId(Integer tenantId, Long orderId, Long orderItemId);
    
    Optional<DigitalDownload> findByDownloadToken(String downloadToken);
    
    // FIXED: Count using composite key
    long countByTenantIdAndOrderIdAndOrderItemId(Integer tenantId, Long orderId, Long orderItemId);
    
    long countByTenantIdAndOrderIdAndOrderItemIdAndDownloadStatus(Integer tenantId, Long orderId, Long orderItemId, String downloadStatus);
    
    List<DigitalDownload> findByTenantId(Integer tenantId);
    
    List<DigitalDownload> findByIpAddress(String ipAddress);
    
    List<DigitalDownload> findByDownloadDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<DigitalDownload> findByTenantIdAndStatus(Integer tenantId, String status);
    
    List<DigitalDownload> findByTokenExpiryBefore(LocalDateTime dateTime);
    
    // FIXED: Use proper join for user lookup
    @Query("SELECT dd FROM DigitalDownload dd JOIN dd.orderItem oi JOIN oi.order o WHERE o.tenantId = :tenantId AND o.userId = :userId")
    List<DigitalDownload> findByTenantIdAndUserId(@Param("tenantId") Integer tenantId, @Param("userId") Long userId);
    
    // FIXED: Use proper join for product lookup
    @Query("SELECT dd FROM DigitalDownload dd JOIN dd.orderItem oi WHERE oi.tenantId = :tenantId AND oi.productId = :productId")
    List<DigitalDownload> findByTenantIdAndProductId(@Param("tenantId") Integer tenantId, @Param("productId") Long productId);
}