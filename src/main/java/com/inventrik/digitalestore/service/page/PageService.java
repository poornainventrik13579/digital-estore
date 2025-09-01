package com.inventrik.digitalestore.service.page;

import com.inventrik.digitalestore.domain.page.PageStatus;
import com.inventrik.digitalestore.domain.page.PageVisibility;
import com.inventrik.digitalestore.dto.request.PageRequest;
import com.inventrik.digitalestore.dto.request.PageUpdateRequest;
import com.inventrik.digitalestore.dto.response.PageResponse;

import java.util.List;

public interface PageService {
    
    List<PageResponse> getAllPages();
    PageResponse getPage(Long pageId);
    List<PageResponse> getPagesByTenant(Integer tenantId);
    List<PageResponse> getPagesByTenantAndStatus(Integer tenantId, PageStatus status);
    List<PageResponse> getPagesByTenantAndVisibility(Integer tenantId, PageVisibility visibility);
    List<PageResponse> getPagesByTenantAndLanguage(Integer tenantId, String language);
    PageResponse getPageBySlug(Integer tenantId, String slug);
    List<PageResponse> getDefaultPages(Integer tenantId);
    PageResponse createPage(String username, PageRequest pageRequest);
    PageResponse updatePage(Long pageId, String username, PageUpdateRequest updateRequest);
    void deletePage(Long pageId);
    PageResponse publishPage(Long pageId, String username);
    PageResponse archivePage(Long pageId, String username);
    List<PageResponse> getPublicPages(Integer tenantId);
    List<PageResponse> searchPages(Integer tenantId, String keyword);
    boolean existsByTenantAndSlug(Integer tenantId, String slug);
    boolean existsByTenantAndTitle(Integer tenantId, String title);
}
