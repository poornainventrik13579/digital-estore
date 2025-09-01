package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.page.Page;
import com.inventrik.digitalestore.domain.page.PageStatus;
import com.inventrik.digitalestore.domain.page.PageVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    
    List<Page> findByTenantId(Integer tenantId);
    List<Page> findByTenantIdAndStatus(Integer tenantId, PageStatus status);
    List<Page> findByTenantIdAndVisibility(Integer tenantId, PageVisibility visibility);
    List<Page> findByTenantIdAndLanguage(Integer tenantId, String language);
    Optional<Page> findByTenantIdAndSlug(Integer tenantId, String slug);
    List<Page> findByTenantIdAndIsDefault(Integer tenantId, Boolean isDefault);
    boolean existsByTenantIdAndSlug(Integer tenantId, String slug);
    boolean existsByTenantIdAndTitle(Integer tenantId, String title);
    
    @Query("SELECT p FROM Page p WHERE p.tenantId = :tenantId AND p.status = :status AND p.visibility = :visibility")
    List<Page> findByTenantIdAndStatusAndVisibility(
        @Param("tenantId") Integer tenantId, 
        @Param("status") PageStatus status, 
        @Param("visibility") PageVisibility visibility
    );
    
    @Query("SELECT p FROM Page p WHERE p.tenantId = :tenantId AND p.status = 'PUBLISHED' AND p.visibility IN ('PUBLIC', 'PRIVATE')")
    List<Page> findPublicAndPrivatePublishedPages(@Param("tenantId") Integer tenantId);
    
    @Query("SELECT p FROM Page p WHERE p.tenantId = :tenantId AND p.content LIKE %:keyword%")
    List<Page> searchByContent(@Param("tenantId") Integer tenantId, @Param("keyword") String keyword);
    
    @Query("SELECT p FROM Page p WHERE p.tenantId = :tenantId AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    List<Page> searchByTitleOrContent(@Param("tenantId") Integer tenantId, @Param("keyword") String keyword);
}
