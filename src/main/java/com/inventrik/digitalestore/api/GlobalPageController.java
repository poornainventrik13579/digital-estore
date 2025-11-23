package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.response.PageResponse;
import com.inventrik.digitalestore.service.page.PageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pages")
@RequiredArgsConstructor
@Tag(name = "Global Page Management", description = "Global APIs for managing pages across all tenants")
@SecurityRequirement(name = "oauth2")
public class GlobalPageController {

    private final PageService pageService;
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Get all pages across all tenants")
    public ResponseEntity<List<PageResponse>> getAllPages() {
        return ResponseEntity.ok(pageService.getAllPages());
    }
}
