package com.inventrik.digitalestore.service.page;

import com.inventrik.digitalestore.domain.page.Page;
import com.inventrik.digitalestore.domain.page.PageStatus;
import com.inventrik.digitalestore.domain.page.PageVisibility;
import com.inventrik.digitalestore.dto.request.PageRequest;
import com.inventrik.digitalestore.dto.request.PageUpdateRequest;
import com.inventrik.digitalestore.dto.response.PageResponse;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.exception.UnauthorizedException;
import com.inventrik.digitalestore.repository.PageRepository;
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
    
    private PageResponse mapToDTO(Page page) {
        return new PageResponse(
            page.getId(),
            page.getTenantId(),
            page.getTitle(),
            page.getSlug(),
            page.getContent(),
            page.getMetaTitle(),
            page.getMetaDescription(),
            page.getStatus(),
            page.getVisibility(),
            page.getCreatedAt(),
            page.getUpdatedAt(),
            page.getPublishedAt(),
            page.getIsDefault(),
            page.getLanguage(),
            page.getCreatedBy(),
            page.getUpdatedBy()
        );
    }
    
    private Page mapToEntity(PageRequest request, String username) {
        Page page = new Page();
        page.setTenantId(request.getTenantId());
        page.setTitle(request.getTitle());
        page.setSlug(request.getSlug());
        page.setContent(request.getContent());
        page.setMetaTitle(request.getMetaTitle());
        page.setMetaDescription(request.getMetaDescription());
        page.setStatus(request.getStatus());
        page.setVisibility(request.getVisibility());
        page.setIsDefault(request.getIsDefault());
        page.setLanguage(request.getLanguage());
        page.setCreatedBy(username);
        page.setUpdatedBy(username);
        return page;
    }
    
    private void updateEntityFromRequest(Page page, PageUpdateRequest request, String username) {
        if (request.getTitle() != null) {
            page.setTitle(request.getTitle());
        }
        if (request.getSlug() != null) {
            page.setSlug(request.getSlug());
        }
        if (request.getContent() != null) {
            page.setContent(request.getContent());
        }
        if (request.getMetaTitle() != null) {
            page.setMetaTitle(request.getMetaTitle());
        }
        if (request.getMetaDescription() != null) {
            page.setMetaDescription(request.getMetaDescription());
        }
        if (request.getStatus() != null) {
            page.setStatus(request.getStatus());
            if (request.getStatus() == PageStatus.PUBLISHED && page.getPublishedAt() == null) {
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
        page.setUpdatedBy(username);
        page.setUpdatedAt(LocalDateTime.now());
    }
    
    @Override
    public List<PageResponse> getAllPages() {
        return pageRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public PageResponse getPage(Integer tenantId, Long pageId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + pageId));

        if (!page.getTenantId().equals(tenantId)) {
            throw new UnauthorizedException("Page does not belong to the specified tenant");
        }

        return mapToDTO(page);
    }
    
    @Override
    public List<PageResponse> getPagesByTenant(Integer tenantId) {
        return pageRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PageResponse> getPagesByTenantAndStatus(Integer tenantId, PageStatus status) {
        return pageRepository.findByTenantIdAndStatus(tenantId, status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PageResponse> getPagesByTenantAndVisibility(Integer tenantId, PageVisibility visibility) {
        return pageRepository.findByTenantIdAndVisibility(tenantId, visibility).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PageResponse> getPagesByTenantAndLanguage(Integer tenantId, String language) {
        return pageRepository.findByTenantIdAndLanguage(tenantId, language).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public PageResponse getPageBySlug(Integer tenantId, String slug) {
        Page page = pageRepository.findByTenantIdAndSlug(tenantId, slug)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with slug: " + slug + " for tenant: " + tenantId));
        return mapToDTO(page);
    }
    
    @Override
    public List<PageResponse> getDefaultPages(Integer tenantId) {
        return pageRepository.findByTenantIdAndIsDefault(tenantId, true).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public PageResponse createPage(String username, PageRequest pageRequest) {
        Page page = mapToEntity(pageRequest, username);
        Page savedPage = pageRepository.save(page);
        return mapToDTO(savedPage);
    }
    
    @Override
    @Transactional
    public PageResponse updatePage(Integer tenantId, Long pageId, String username, PageUpdateRequest updateRequest) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + pageId));

        if (!page.getTenantId().equals(tenantId)) {
            throw new UnauthorizedException("Page does not belong to the specified tenant");
        }

        updateEntityFromRequest(page, updateRequest, username);
        Page updatedPage = pageRepository.save(page);
        return mapToDTO(updatedPage);
    }

    @Override
    @Transactional
    public void deletePage(Integer tenantId, Long pageId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + pageId));

        if (!page.getTenantId().equals(tenantId)) {
            throw new UnauthorizedException("Page does not belong to the specified tenant");
        }

        pageRepository.delete(page);
    }

    @Override
    @Transactional
    public PageResponse publishPage(Integer tenantId, Long pageId, String username) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + pageId));

        if (!page.getTenantId().equals(tenantId)) {
            throw new UnauthorizedException("Page does not belong to the specified tenant");
        }

        page.publish();
        page.setUpdatedBy(username);
        page.setUpdatedAt(LocalDateTime.now());

        Page updatedPage = pageRepository.save(page);
        return mapToDTO(updatedPage);
    }

    @Override
    @Transactional
    public PageResponse archivePage(Integer tenantId, Long pageId, String username) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + pageId));

        if (!page.getTenantId().equals(tenantId)) {
            throw new UnauthorizedException("Page does not belong to the specified tenant");
        }

        page.archive();
        page.setUpdatedBy(username);
        page.setUpdatedAt(LocalDateTime.now());

        Page updatedPage = pageRepository.save(page);
        return mapToDTO(updatedPage);
    }
    
    @Override
    public List<PageResponse> getPublicPages(Integer tenantId) {
        return pageRepository.findPublicAndPrivatePublishedPages(
                tenantId,
                PageStatus.PUBLISHED,
                PageVisibility.PUBLIC,
                PageVisibility.PRIVATE).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PageResponse> searchPages(Integer tenantId, String keyword) {
        return pageRepository.searchByTitleOrContent(tenantId, keyword).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByTenantAndSlug(Integer tenantId, String slug) {
        return pageRepository.existsByTenantIdAndSlug(tenantId, slug);
    }
    
    @Override
    public boolean existsByTenantAndTitle(Integer tenantId, String title) {
        return pageRepository.existsByTenantIdAndTitle(tenantId, title);
    }
}
