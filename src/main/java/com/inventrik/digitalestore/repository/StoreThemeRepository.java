package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.theme.StoreTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreThemeRepository extends JpaRepository<StoreTheme, StoreTheme.StoreThemePK> {
    List<StoreTheme> findByTenantId(Integer tenantId);
    Optional<StoreTheme> findByTenantIdAndThemeId(Integer tenantId, Long themeId);
    void deleteByTenantIdAndThemeId(Integer tenantId, Long themeId);
}
