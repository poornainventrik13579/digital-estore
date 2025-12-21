package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.StoreThemeRequest;
import com.inventrik.digitalestore.dto.response.StoreThemeResponse;
import com.inventrik.digitalestore.service.theme.StoreThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/themes")
@RequiredArgsConstructor
@Tag(name = "Store Theme Management")
@SecurityRequirement(name = "oauth2")
public class StoreThemeController {

    private final StoreThemeService storeThemeService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get all themes for tenant")
    public ResponseEntity<List<StoreThemeResponse>> getAllThemes(@PathVariable Integer tenantId) {
        return ResponseEntity.ok(storeThemeService.getAllThemes(tenantId));
    }

    @GetMapping("/{themeId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get theme by ID")
    public ResponseEntity<StoreThemeResponse> getTheme(
            @PathVariable Integer tenantId,
            @PathVariable Integer themeId) {
        return ResponseEntity.ok(storeThemeService.getTheme(tenantId, themeId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Create new theme")
    public ResponseEntity<StoreThemeResponse> createTheme(
            @PathVariable Integer tenantId,
            @Valid @RequestBody StoreThemeRequest request,
            Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "system";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(storeThemeService.createTheme(tenantId, request, username));
    }

    @PutMapping("/{themeId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Update theme")
    public ResponseEntity<StoreThemeResponse> updateTheme(
            @PathVariable Integer tenantId,
            @PathVariable Integer themeId,
            @Valid @RequestBody StoreThemeRequest request,
            Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "system";
        return ResponseEntity.ok(storeThemeService.updateTheme(tenantId, themeId, request, username));
    }

    @DeleteMapping("/{themeId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Delete theme")
    public ResponseEntity<Void> deleteTheme(
            @PathVariable Integer tenantId,
            @PathVariable Integer themeId) {
        storeThemeService.deleteTheme(tenantId, themeId);
        return ResponseEntity.noContent().build();
    }
}
