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
    
    // Find downloads by order item ID
    List<DigitalDownload> findByOrderItemId(Long orderItemId);
    
    // Find download by token
    Optional<DigitalDownload> findByDownloadToken(String downloadToken);
    
    // Count downloads by order item ID
    long countByOrderItemId(Long orderItemId);
    
    // Count completed downloads by order item ID
    long countByOrderItemIdAndDownloadStatus(Long orderItemId, String downloadStatus);
    
    // Find downloads by tenant and order item ID
    List<DigitalDownload> findByTenantIdAndOrderItemId(Integer tenantId, Long orderItemId);
    
    // Find downloads by tenant ID
    List<DigitalDownload> findByTenantId(Integer tenantId);
    
    // Find downloads by IP address
    List<DigitalDownload> findByIpAddress(String ipAddress);
    
    // Find downloads within date range
    List<DigitalDownload> findByDownloadDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find downloads by status
    List<DigitalDownload> findByTenantIdAndStatus(Integer tenantId, String status);
    
    // Find expired tokens
    List<DigitalDownload> findByTokenExpiryBefore(LocalDateTime dateTime);
    
    // Find downloads by user (through order item)
    @Query("SELECT dd FROM DigitalDownload dd JOIN dd.orderItem oi JOIN oi.order o WHERE o.userId = :userId AND dd.tenantId = :tenantId")
    List<DigitalDownload> findByTenantIdAndUserId(@Param("tenantId") Integer tenantId, @Param("userId") Long userId);
    
    // Get download history for a specific product
    @Query("SELECT dd FROM DigitalDownload dd JOIN dd.orderItem oi WHERE oi.productId = :productId AND dd.tenantId = :tenantId")
    List<DigitalDownload> findByTenantIdAndProductId(@Param("tenantId") Integer tenantId, @Param("productId") Long productId);
}