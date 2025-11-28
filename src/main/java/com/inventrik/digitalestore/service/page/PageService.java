package com.inventrik.digitalestore.service.page;

import com.inventrik.digitalestore.dto.request.PageRequest;
import com.inventrik.digitalestore.dto.response.PageResponse;

import java.util.List;

public interface PageService {
    List<PageResponse> getAllPages(Integer tenantId, String status, String visibility);
    PageResponse getPage(Integer tenantId, Long pageId);
    PageResponse getPageBySlug(Integer tenantId, String slug);
    PageResponse createPage(Integer tenantId, PageRequest request);
    PageResponse updatePage(Integer tenantId, Long pageId, PageRequest request);
    void deletePage(Integer tenantId, Long pageId);
}
