package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.tax.Tax;
import com.inventrik.digitalestore.domain.tax.TaxStatus;
import com.inventrik.digitalestore.domain.tax.TaxDefaultFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaxRepository extends JpaRepository<Tax, Tax.TaxPK> {
    
    List<Tax> findByTenantId(Integer tenantId);
    List<Tax> findByTenantIdAndStatus(Integer tenantId, String status);
    Optional<Tax> findByTenantIdAndId(Integer tenantId, Integer id);
    Optional<Tax> findByTenantIdAndCode(Integer tenantId, String code);
    List<Tax> findByTenantIdAndDefaultFlag(Integer tenantId, String defaultFlag);
    boolean existsByTenantIdAndCode(Integer tenantId, String code);
    
    @Query("SELECT t FROM Tax t WHERE t.tenantId = :tenantId AND t.status = :activeStatus")
    List<Tax> findActiveTaxesByTenant(@Param("tenantId") Integer tenantId, @Param("activeStatus") String activeStatus);
    
    @Query("SELECT t FROM Tax t WHERE t.tenantId = :tenantId AND t.defaultFlag = :defaultFlag AND t.status = :activeStatus")
    Optional<Tax> findDefaultTaxByTenant(@Param("tenantId") Integer tenantId, @Param("defaultFlag") String defaultFlag, @Param("activeStatus") String activeStatus);
    
    @Query("SELECT t FROM Tax t WHERE t.tenantId = :tenantId AND t.status = :activeStatus " +
           "AND (t.startDate IS NULL OR t.startDate <= :date) " +
           "AND (t.endDate IS NULL OR t.endDate >= :date)")
    List<Tax> findValidTaxesForDate(@Param("tenantId") Integer tenantId, @Param("date") LocalDate date, @Param("activeStatus") String activeStatus);
    
    @Query("SELECT t FROM Tax t WHERE t.tenantId = :tenantId AND t.defaultFlag = :defaultFlag AND t.status = :activeStatus " +
           "AND (t.startDate IS NULL OR t.startDate <= :date) " +
           "AND (t.endDate IS NULL OR t.endDate >= :date)")
    Optional<Tax> findValidDefaultTaxForDate(@Param("tenantId") Integer tenantId, @Param("date") LocalDate date, @Param("defaultFlag") String defaultFlag, @Param("activeStatus") String activeStatus);
    
    @Query("SELECT t FROM Tax t WHERE t.tenantId = :tenantId AND t.code LIKE %:codePattern% AND t.status = :activeStatus")
    List<Tax> findByTenantAndCodePattern(@Param("tenantId") Integer tenantId, @Param("codePattern") String codePattern, @Param("activeStatus") String activeStatus);
    
    @Query("SELECT t FROM Tax t WHERE t.tenantId = :tenantId AND " +
           "(t.description LIKE %:keyword% OR t.code LIKE %:keyword%) AND t.status = :activeStatus")
    List<Tax> searchTaxes(@Param("tenantId") Integer tenantId, @Param("keyword") String keyword, @Param("activeStatus") String activeStatus);
    
    @Modifying
    @Query("UPDATE Tax t SET t.defaultFlag = :noFlag, t.modifiedBy = :modifiedBy, t.modified = CURRENT_TIMESTAMP " +
           "WHERE t.tenantId = :tenantId AND t.defaultFlag = :yesFlag")
    void clearDefaultFlags(@Param("tenantId") Integer tenantId, @Param("modifiedBy") String modifiedBy, @Param("noFlag") String noFlag, @Param("yesFlag") String yesFlag);
    
    @Query("SELECT COUNT(t) FROM Tax t WHERE t.tenantId = :tenantId AND t.status = :activeStatus")
    long countActiveTaxesByTenant(@Param("tenantId") Integer tenantId, @Param("activeStatus") String activeStatus);
}
