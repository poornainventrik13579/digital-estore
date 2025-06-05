package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.download.DigitalDownload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DigitalDownloadRepository extends JpaRepository<DigitalDownload, Long> {
    
    // Find downloads by tenant and order item
    List<DigitalDownload> findByTenantIdAndOrderItemId(Integer tenantId, Long orderItemId);
    
    // Find all downloads for a tenant
    List<DigitalDownload> findByTenantId(Integer tenantId);
    
    // Find downloads by IP address
    List<DigitalDownload> findByIpAddress(String ipAddress);
    
    // Find downloads by date range
    List<DigitalDownload> findByDownloadDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find downloads by tenant and status
    List<DigitalDownload> findByTenantIdAndStatus(Integer tenantId, String status);
    
    // Find downloads by user (through order items)
    @Query("SELECT dd FROM DigitalDownload dd JOIN dd.orderItem oi JOIN oi.order o WHERE o.tenantId = :tenantId AND o.userId = :userId")
    List<DigitalDownload> findByTenantIdAndUserId(@Param("tenantId") Integer tenantId, @Param("userId") Long userId);
}