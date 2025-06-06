package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.discount.DiscountUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountUsageRepository extends JpaRepository<DiscountUsage, DiscountUsage.DiscountUsagePK> {
    
    List<DiscountUsage> findByTenantIdAndDiscountIdAndStatus(Integer tenantId, Long discountId, String status);
    
    List<DiscountUsage> findByTenantIdAndUserIdAndStatus(Integer tenantId, Long userId, String status);
    
    List<DiscountUsage> findByTenantIdAndOrderIdAndStatus(Integer tenantId, Long orderId, String status);
    
    Optional<DiscountUsage> findByTenantIdAndOrderIdAndDiscountIdAndStatus(Integer tenantId, Long orderId, Long discountId, String status);
    
    @Query("SELECT COUNT(u) FROM DiscountUsage u WHERE u.tenantId = :tenantId AND u.discountId = :discountId AND u.status = :status")
    long countUsageByDiscountId(@Param("tenantId") Integer tenantId, @Param("discountId") Long discountId, @Param("status") String status);
    
    @Query("SELECT COUNT(u) FROM DiscountUsage u WHERE u.tenantId = :tenantId AND u.userId = :userId AND u.discountId = :discountId AND u.status = :status")
    long countUserUsageForDiscount(@Param("tenantId") Integer tenantId, @Param("userId") Long userId, @Param("discountId") Long discountId, @Param("status") String status);
    
    @Query("SELECT u FROM DiscountUsage u WHERE u.tenantId = :tenantId AND u.usedDate BETWEEN :startDate AND :endDate AND u.status = :status")
    List<DiscountUsage> findUsageInDateRange(@Param("tenantId") Integer tenantId, 
                                             @Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate, 
                                             @Param("status") String status);
    
    @Query("SELECT SUM(u.discountAmount) FROM DiscountUsage u WHERE u.tenantId = :tenantId AND u.discountId = :discountId AND u.status = :status")
    Double getTotalDiscountAmountUsed(@Param("tenantId") Integer tenantId, @Param("discountId") Long discountId, @Param("status") String status);
} 