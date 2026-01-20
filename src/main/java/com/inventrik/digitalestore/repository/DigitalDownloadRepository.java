package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.download.DigitalDownload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DigitalDownloadRepository extends JpaRepository<DigitalDownload, DigitalDownload.DigitalDownloadPK> {

    @Query("SELECT dd FROM DigitalDownload dd WHERE dd.tenantId = :tenantId AND dd.orderItemId = :orderItemId ORDER BY dd.downloadDate DESC")
    List<DigitalDownload> findByTenantIdAndOrderItemId(@Param("tenantId") Integer tenantId, @Param("orderItemId") String orderItemId);

    @Query("SELECT dd FROM DigitalDownload dd WHERE dd.tenantId = :tenantId ORDER BY dd.downloadDate DESC")
    List<DigitalDownload> findByTenantId(@Param("tenantId") Integer tenantId);

    @Query("SELECT dd FROM DigitalDownload dd WHERE dd.ipAddress = :ipAddress ORDER BY dd.downloadDate DESC")
    List<DigitalDownload> findByIpAddress(@Param("ipAddress") String ipAddress);

    @Query("SELECT dd FROM DigitalDownload dd WHERE dd.downloadDate BETWEEN :startDate AND :endDate ORDER BY dd.downloadDate DESC")
    List<DigitalDownload> findByDownloadDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT dd FROM DigitalDownload dd WHERE dd.tenantId = :tenantId AND dd.status = :status ORDER BY dd.downloadDate DESC")
    List<DigitalDownload> findByTenantIdAndStatus(@Param("tenantId") Integer tenantId, @Param("status") String status);

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
        ORDER BY dd.downloadDate DESC
        """)
    List<DigitalDownload> findByTenantIdAndUserId(@Param("tenantId") Integer tenantId, @Param("userId") String userId);
}
