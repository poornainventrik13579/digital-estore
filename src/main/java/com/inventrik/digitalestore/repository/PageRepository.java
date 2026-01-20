package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.page.Page;
import com.inventrik.digitalestore.domain.page.PageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Page.PagePK> {
    List<Page> findByTenantId(Integer tenantId);
    Optional<Page> findByTenantIdAndPageId(Integer tenantId, String pageId);
    Optional<Page> findByTenantIdAndSlug(Integer tenantId, String slug);
    List<Page> findByTenantIdAndStatus(Integer tenantId, PageStatus status);
    void deleteByTenantIdAndPageId(Integer tenantId, String pageId);
}
