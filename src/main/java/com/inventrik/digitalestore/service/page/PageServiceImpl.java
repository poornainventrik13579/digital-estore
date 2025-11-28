package com.inventrik.digitalestore.service.page;

import com.inventrik.digitalestore.domain.page.Page;
import com.inventrik.digitalestore.domain.page.PageStatus;
import com.inventrik.digitalestore.domain.page.PageVisibility;
import com.inventrik.digitalestore.dto.request.PageRequest;
import com.inventrik.digitalestore.dto.response.PageResponse;
import com.inventrik.digitalestore.exception.BusinessException;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.PageRepository;
import com.inventrik.digitalestore.repository.TenantRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {

    private final PageRepository pageRepository;
    private final TenantRepository tenantRepository;
    private final IdGeneratorService idGeneratorService;

    private PageResponse mapToDTO(Page page) {
        return new PageResponse(
            page.getTenantId(),
            page.getPageId(),
            page.getTitle(),
            page.getSlug(),
            page.getContent(),
            page.getMetaTitle(),
            page.getMetaDescription(),
            page.getStatus(),
            page.getVisibility(),
            page.getIsDefault(),
            page.getLanguage(),
            page.getCreatedAt(),
            page.getUpdatedAt(),
            page.getPublishedAt()
        );
    }

    @Override
    public List<PageResponse> getAllPages(Integer tenantId, String status, String visibility) {
        List<PageResponse> pages = pageRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        if (status != null) {
            pages = pages.stream()
                    .filter(page -> status.equals(page.getStatus()))
                    .collect(Collectors.toList());
        }
        if (visibility != null) {
            pages = pages.stream()
                    .filter(page -> visibility.equals(page.getVisibility()))
                    .collect(Collectors.toList());
        }

        return pages;
    }

    @Override
    public PageResponse getPage(Integer tenantId, Long pageId) {
        Page page = pageRepository.findByTenantIdAndPageId(tenantId, pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));
        return mapToDTO(page);
    }

    @Override
    public PageResponse getPageBySlug(Integer tenantId, String slug) {
        Page page = pageRepository.findByTenantIdAndSlug(tenantId, slug)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with slug: " + slug));
        return mapToDTO(page);
    }

    @Override
    @Transactional
    public PageResponse createPage(Integer tenantId, PageRequest request) {
        tenantRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

        if (pageRepository.findByTenantIdAndSlug(tenantId, request.getSlug()).isPresent()) {
            throw new BusinessException("Slug already exists");
        }

        Page page = new Page();
        page.setTenantId(tenantId);
        page.setPageId(idGeneratorService.generateId(tenantId, "PAGE"));
        page.setTitle(request.getTitle());
        page.setSlug(request.getSlug());
        page.setContent(request.getContent());
        page.setMetaTitle(request.getMetaTitle());
        page.setMetaDescription(request.getMetaDescription());
        page.setStatus(request.getStatus() != null ? request.getStatus() : PageStatus.DRAFT);
        page.setVisibility(request.getVisibility() != null ? request.getVisibility() : PageVisibility.PUBLIC);
        page.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
        page.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en");

        if (page.getStatus() == PageStatus.PUBLISHED && page.getPublishedAt() == null) {
            page.setPublishedAt(LocalDateTime.now());
        }

        Page saved = pageRepository.save(page);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public PageResponse updatePage(Integer tenantId, Long pageId, PageRequest request) {
        Page page = pageRepository.findByTenantIdAndPageId(tenantId, pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));

        page.setTitle(request.getTitle());
        page.setContent(request.getContent());
        page.setMetaTitle(request.getMetaTitle());
        page.setMetaDescription(request.getMetaDescription());

        if (request.getStatus() != null) {
            PageStatus oldStatus = page.getStatus();
            page.setStatus(request.getStatus());
            if (request.getStatus() == PageStatus.PUBLISHED && oldStatus != PageStatus.PUBLISHED) {
                page.setPublishedAt(LocalDateTime.now());
            }
        }

        if (request.getVisibility() != null) {
            page.setVisibility(request.getVisibility());
        }

        Page updated = pageRepository.save(page);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deletePage(Integer tenantId, Long pageId) {
        if (!pageRepository.findByTenantIdAndPageId(tenantId, pageId).isPresent()) {
            throw new ResourceNotFoundException("Page not found");
        }
        pageRepository.deleteByTenantIdAndPageId(tenantId, pageId);
    }
}
