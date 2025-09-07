package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.StoreThemeRequest;
import com.inventrik.digitalestore.dto.request.StoreThemeUpdateRequest;
import com.inventrik.digitalestore.dto.response.StoreThemeResponse;
import com.inventrik.digitalestore.service.theme.StoreThemeService;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/themes")
@RequiredArgsConstructor
@Tag(name = "Store Theme Management", description = "APIs for managing store themes and visual customization")
@SecurityRequirement(name = "oauth2")
public class StoreThemeController {

    private final StoreThemeService storeThemeService;
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get all themes for a tenant")
    public ResponseEntity<List<StoreThemeResponse>> getThemesByTenant(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId) {
        return ResponseEntity.ok(storeThemeService.getThemesByTenant(tenantId));
    }
    
    @GetMapping("/{themeId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get a specific theme")
    public ResponseEntity<StoreThemeResponse> getTheme(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Theme ID", required = true)
            @PathVariable Integer themeId) {
        return ResponseEntity.ok(storeThemeService.getTheme(tenantId, themeId));
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Create a new theme (JSON)")
    public ResponseEntity<StoreThemeResponse> createThemeJson(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Valid @RequestBody StoreThemeRequest themeRequest,
            Authentication authentication) {
        
        themeRequest.setTenantId(tenantId);
        String username = (authentication != null) ? authentication.getName() : "system";
        StoreThemeResponse createdTheme = storeThemeService.createTheme(username, themeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTheme);
    }
    
    
    @PutMapping(path = "/{themeId}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Update a theme (JSON)")
    public ResponseEntity<StoreThemeResponse> updateThemeJson(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Theme ID", required = true)
            @PathVariable Integer themeId,
            @Valid @RequestBody StoreThemeUpdateRequest updateRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        StoreThemeResponse updatedTheme = storeThemeService.updateTheme(tenantId, themeId, username, updateRequest);
        return ResponseEntity.ok(updatedTheme);
    }
    
    
    @DeleteMapping("/{themeId}")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Delete a theme")
    public ResponseEntity<Void> deleteTheme(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Theme ID", required = true)
            @PathVariable Integer themeId) {
        storeThemeService.deleteTheme(tenantId, themeId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get themes by status for a tenant")
    public ResponseEntity<List<StoreThemeResponse>> getThemesByStatus(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Status", required = true)
            @PathVariable String status) {
        return ResponseEntity.ok(storeThemeService.getThemesByTenantAndStatus(tenantId, status));
    }
    
    @GetMapping("/check/name/{themeName}")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Check if theme name exists for tenant")
    public ResponseEntity<Boolean> checkThemeNameExists(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Parameter(description = "Theme name", required = true)
            @PathVariable String themeName) {
        return ResponseEntity.ok(storeThemeService.existsByTenantAndName(tenantId, themeName));
    }
}
