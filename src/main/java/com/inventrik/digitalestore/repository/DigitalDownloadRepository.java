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
    
    List<DigitalDownload> findByTenantIdAndOrderItemId(Integer tenantId, Long orderItemId);
    
    List<DigitalDownload> findByTenantId(Integer tenantId);

    List<DigitalDownload> findByTenantIdAndIpAddress(Integer tenantId, String ipAddress);

    List<DigitalDownload> findByTenantIdAndDownloadDateBetween(Integer tenantId, LocalDateTime startDate, LocalDateTime endDate);

    List<DigitalDownload> findByTenantIdAndStatus(Integer tenantId, String status);
    
    @Query("""
        SELECT dd FROM DigitalDownload dd 
        WHERE dd.tenantId = :tenantId 
        AND dd.orderItemId IN (
            SELECT oi.orderItemId FROM OrderItem oi 
            WHERE oi.tenantId = :tenantId 
            AND oi.orderId IN (
                SELECT o.orderId FROM Order o 
                WHERE o.tenantId = :tenantId AND o.userId = :userId
            )
        )
        """)
    List<DigitalDownload> findByTenantIdAndUserId(@Param("tenantId") Integer tenantId, @Param("userId") Long userId);
}