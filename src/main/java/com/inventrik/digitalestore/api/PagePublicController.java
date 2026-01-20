package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.PageRequest;
import com.inventrik.digitalestore.dto.response.PageResponse;
import com.inventrik.digitalestore.service.page.PageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public/tenants/{tenantId}/pages")
@RequiredArgsConstructor
@Tag(name = "Page Management")
// @SecurityRequirement(name = "oauth2")
public class PagePublicController {

    private final PageService pageService;

    @GetMapping
    // @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get pages for tenant with optional filters (public)")
    public ResponseEntity<List<PageResponse>> getAllPages(
            @PathVariable Integer tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String visibility) {
        return ResponseEntity.ok(pageService.getAllPages(tenantId, status, visibility));
    }

    @GetMapping("/{pageId}")
    // @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get page by ID (public)")
    public ResponseEntity<PageResponse> getPage(
            @PathVariable Integer tenantId,
            @PathVariable String pageId) {
        return ResponseEntity.ok(pageService.getPage(tenantId, pageId));
    }

    @GetMapping("/slug/{slug}")
    // @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get page by slug (public)")
    public ResponseEntity<PageResponse> getPageBySlug(
            @PathVariable Integer tenantId,
            @PathVariable String slug) {
        return ResponseEntity.ok(pageService.getPageBySlug(tenantId, slug));
    }
}
