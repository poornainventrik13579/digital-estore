package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.discount.DiscountCode;
import com.inventrik.digitalestore.domain.discount.DiscountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountCodeRepository extends JpaRepository<DiscountCode, DiscountCode.DiscountCodePK> {
    
    List<DiscountCode> findByTenantIdAndStatus(Integer tenantId, String status);
    
    Optional<DiscountCode> findByTenantIdAndCodeAndStatus(Integer tenantId, String code, String status);
    
    List<DiscountCode> findByTenantIdAndDiscountTypeAndStatus(Integer tenantId, DiscountType discountType, String status);
    
    @Query("SELECT d FROM DiscountCode d WHERE d.tenantId = :tenantId AND d.status = :status " +
           "AND (d.validFrom IS NULL OR d.validFrom <= :now) " +
           "AND (d.validTo IS NULL OR d.validTo >= :now)")
    List<DiscountCode> findActiveDiscountCodes(@Param("tenantId") Integer tenantId, 
                                               @Param("status") String status, 
                                               @Param("now") LocalDateTime now);
    
    @Query("SELECT d FROM DiscountCode d WHERE d.tenantId = :tenantId AND d.code = :code " +
           "AND d.status = :status " +
           "AND (d.validFrom IS NULL OR d.validFrom <= :now) " +
           "AND (d.validTo IS NULL OR d.validTo >= :now) " +
           "AND (d.maxUses = 0 OR d.usedCount < d.maxUses)")
    Optional<DiscountCode> findValidDiscountCode(@Param("tenantId") Integer tenantId, 
                                                 @Param("code") String code, 
                                                 @Param("status") String status, 
                                                 @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(d) FROM DiscountCode d WHERE d.tenantId = :tenantId AND d.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") Integer tenantId, @Param("status") String status);
    
    @Query("SELECT d FROM DiscountCode d WHERE d.tenantId = :tenantId AND d.validTo < :now AND d.status = :status")
    List<DiscountCode> findExpiredDiscountCodes(@Param("tenantId") Integer tenantId, 
                                                @Param("now") LocalDateTime now, 
                                                @Param("status") String status);
    
    @Modifying
    @Query("UPDATE DiscountCode d SET d.usedCount = d.usedCount + 1, d.updatedBy = :updatedBy, d.updated = CURRENT_TIMESTAMP " +
           "WHERE d.tenantId = :tenantId AND d.discountId = :discountId AND (d.maxUses = 0 OR d.usedCount < d.maxUses)")
    int incrementUsedCount(@Param("tenantId") Integer tenantId,
                          @Param("discountId") String discountId,
                          @Param("updatedBy") String updatedBy);
} 