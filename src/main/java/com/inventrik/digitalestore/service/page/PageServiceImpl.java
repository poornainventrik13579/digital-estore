package com.inventrik.digitalestore.service.page;

import com.inventrik.digitalestore.domain.page.Page;
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
            page.getTemplate(),
            page.getStatus(),
            page.getVisibility(),
            page.getIsDefault(),
            page.getLanguage(),
            page.getCreated(),
            page.getUpdated(),
            page.getPublishedAt()
        );
    }

    @Override
    public List<PageResponse> getAllPages(Integer tenantId, String status, String visibility) {
        List<PageResponse> pages = pageRepository.findByTenantId(tenantId).stream()
                .filter(p -> !"DELETED".equals(p.getStatus()))
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
    public PageResponse getPage(Integer tenantId, String pageId) {
        Page page = pageRepository.findByTenantIdAndPageId(tenantId, pageId)
                .filter(p -> !"DELETED".equals(p.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));
        return mapToDTO(page);
    }

    @Override
    public PageResponse getPageBySlug(Integer tenantId, String slug) {
        Page page = pageRepository.findByTenantIdAndSlug(tenantId, slug)
                .filter(p -> !"DELETED".equals(p.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with slug: " + slug));
        return mapToDTO(page);
    }

    @Override
    @Transactional
    public PageResponse createPage(Integer tenantId, PageRequest request) {
        tenantRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

        if (pageRepository.findByTenantIdAndSlugAndStatusNot(tenantId, request.getSlug(), "DELETED").isPresent()) {
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
        page.setTemplate(request.getTemplate() != null ? request.getTemplate(): "default");
        page.setStatus(request.getStatus() != null ? request.getStatus() : "DRAFT");
        page.setVisibility(request.getVisibility() != null ? request.getVisibility() : "PUBLIC");
        page.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
        page.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en");

        if ("PUBLISHED".equals(page.getStatus()) && page.getPublishedAt() == null) {
            page.setPublishedAt(LocalDateTime.now());
        }

        Page saved = pageRepository.save(page);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public PageResponse updatePage(Integer tenantId, String pageId, PageRequest request) {
        tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

        Page page = pageRepository.findByTenantIdAndPageId(tenantId, pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));

        if (request.getSlug() != null && !page.getSlug().equals(request.getSlug()) &&
                pageRepository.existsByTenantIdAndSlugAndPageIdNot(tenantId, request.getSlug(), pageId)) {
            throw new BusinessException("Slug already in use by another page");
        }

        if (request.getTitle() != null) page.setTitle(request.getTitle());
        if (request.getSlug() != null) page.setSlug(request.getSlug());
        if (request.getContent() != null) page.setContent(request.getContent());
        if (request.getMetaTitle() != null) page.setMetaTitle(request.getMetaTitle());
        if (request.getMetaDescription() != null) page.setMetaDescription(request.getMetaDescription());

        if (request.getTemplate() != null) {
            page.setTemplate(request.getTemplate());
        }
        if (request.getStatus() != null) {
            String previousStatus = page.getStatus();
            page.setStatus(request.getStatus());
            if ("PUBLISHED".equals(request.getStatus()) && !"PUBLISHED".equals(previousStatus)) {
                page.setPublishedAt(LocalDateTime.now());
            }
        }
        if (request.getVisibility() != null) {
            page.setVisibility(request.getVisibility());
        }
        if (request.getIsDefault() != null) {
            page.setIsDefault(request.getIsDefault());
        }
        if (request.getLanguage() != null) {
            page.setLanguage(request.getLanguage());
        }

        Page updatedPage = pageRepository.save(page);
        return mapToDTO(updatedPage);
    }

    @Override
    @Transactional
    public void deletePage(Integer tenantId, String pageId) {
        Page page = pageRepository.findByTenantIdAndPageId(tenantId, pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));

        page.setStatus("DELETED");
        pageRepository.save(page);
    }
}
