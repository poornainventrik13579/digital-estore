package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.domain.page.PageStatus;
import com.inventrik.digitalestore.domain.page.PageVisibility;
import com.inventrik.digitalestore.dto.request.PageRequest;
import com.inventrik.digitalestore.dto.request.PageUpdateRequest;
import com.inventrik.digitalestore.dto.response.PageResponse;
import com.inventrik.digitalestore.security.TenantAccessValidator;
import com.inventrik.digitalestore.service.page.PageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/pages")
@RequiredArgsConstructor
@Validated
@Tag(name = "Page Management", description = "APIs for managing CMS pages and content")
@SecurityRequirement(name = "oauth2")
public class PageController {

    private final PageService pageService;
    private final TenantAccessValidator tenantAccessValidator;
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get all pages for a tenant")
    public ResponseEntity<List<PageResponse>> getPagesByTenant(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(pageService.getPagesByTenant(tenantId));
    }
    
    @GetMapping("/{pageId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get a specific page")
    public ResponseEntity<PageResponse> getPage(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Page ID", required = true)
            @PathVariable Long pageId,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(pageService.getPage(tenantId, pageId));
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Create a new page (JSON)")
    public ResponseEntity<PageResponse> createPageJson(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Valid @RequestBody PageRequest pageRequest,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        pageRequest.setTenantId(tenantId);
        String username = (authentication != null) ? authentication.getName() : "system";
        PageResponse createdPage = pageService.createPage(username, pageRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPage);
    }
    
    
    @PutMapping(path = "/{pageId}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Update a page (JSON)")
    public ResponseEntity<PageResponse> updatePageJson(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Page ID", required = true)
            @PathVariable Long pageId,
            @Valid @RequestBody PageUpdateRequest updateRequest,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String username = (authentication != null) ? authentication.getName() : "system";
        PageResponse updatedPage = pageService.updatePage(tenantId, pageId, username, updateRequest);
        return ResponseEntity.ok(updatedPage);
    }
    
    
    @DeleteMapping("/{pageId}")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Delete a page")
    public ResponseEntity<Void> deletePage(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Page ID", required = true)
            @PathVariable Long pageId,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        pageService.deletePage(tenantId, pageId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{pageId}/publish")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Publish a page")
    public ResponseEntity<PageResponse> publishPage(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Page ID", required = true)
            @PathVariable Long pageId,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String username = (authentication != null) ? authentication.getName() : "system";
        PageResponse publishedPage = pageService.publishPage(tenantId, pageId, username);
        return ResponseEntity.ok(publishedPage);
    }
    
    @PostMapping("/{pageId}/archive")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Archive a page")
    public ResponseEntity<PageResponse> archivePage(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Page ID", required = true)
            @PathVariable Long pageId,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String username = (authentication != null) ? authentication.getName() : "system";
        PageResponse archivedPage = pageService.archivePage(tenantId, pageId, username);
        return ResponseEntity.ok(archivedPage);
    }
    
    @GetMapping("/slug/{slug}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get page by slug")
    public ResponseEntity<PageResponse> getPageBySlug(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Page slug", required = true)
            @PathVariable String slug,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(pageService.getPageBySlug(tenantId, slug));
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get pages by status")
    public ResponseEntity<List<PageResponse>> getPagesByStatus(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Page status", required = true)
            @PathVariable PageStatus status,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(pageService.getPagesByTenantAndStatus(tenantId, status));
    }
    
    @GetMapping("/visibility/{visibility}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get pages by visibility")
    public ResponseEntity<List<PageResponse>> getPagesByVisibility(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Page visibility", required = true)
            @PathVariable PageVisibility visibility,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(pageService.getPagesByTenantAndVisibility(tenantId, visibility));
    }
    
    @GetMapping("/language/{language}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get pages by language")
    public ResponseEntity<List<PageResponse>> getPagesByLanguage(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Language code", required = true)
            @PathVariable String language,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(pageService.getPagesByTenantAndLanguage(tenantId, language));
    }
    
    @GetMapping("/default")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get default template pages")
    public ResponseEntity<List<PageResponse>> getDefaultPages(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(pageService.getDefaultPages(tenantId));
    }
    
    @GetMapping("/public")
    @Operation(summary = "Get public published pages")
    public ResponseEntity<List<PageResponse>> getPublicPages(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId) {
        return ResponseEntity.ok(pageService.getPublicPages(tenantId));
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Search pages by keyword")
    public ResponseEntity<List<PageResponse>> searchPages(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Search keyword", required = true)
            @RequestParam @Size(max = 100) String keyword,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(pageService.searchPages(tenantId, keyword));
    }
    
    @GetMapping("/check/slug/{slug}")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Check if slug exists for tenant")
    public ResponseEntity<Boolean> checkSlugExists(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Page slug", required = true)
            @PathVariable String slug,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(pageService.existsByTenantAndSlug(tenantId, slug));
    }
    
    @GetMapping("/check/title/{title}")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Check if title exists for tenant")
    public ResponseEntity<Boolean> checkTitleExists(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Page title", required = true)
            @PathVariable String title,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(pageService.existsByTenantAndTitle(tenantId, title));
    }
}
