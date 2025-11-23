package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.theme.StoreTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreThemeRepository extends JpaRepository<StoreTheme, StoreTheme.StoreThemePK> {
    
    Optional<StoreTheme> findByTenantIdAndThemeId(Integer tenantId, Integer themeId);
    List<StoreTheme> findByTenantId(Integer tenantId);
    List<StoreTheme> findByTenantIdAndStatus(Integer tenantId, String status);
    List<StoreTheme> findByTenantIdAndThemeName(Integer tenantId, String themeName);
    boolean existsByTenantIdAndThemeName(Integer tenantId, String themeName);
    @Modifying
    @Query("DELETE FROM StoreTheme s WHERE s.tenantId = :tenantId AND s.themeId = :themeId")
    void deleteByTenantIdAndThemeId(@Param("tenantId") Integer tenantId, @Param("themeId") Integer themeId);
}
