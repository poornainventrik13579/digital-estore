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
@RequestMapping("/api/v1/public/tenants/{tenantId}/themes")
@RequiredArgsConstructor
@Tag(name = "Store Theme Management")
// @SecurityRequirement(name = "oauth2")
public class StoreThemePublicController {

    private final StoreThemeService storeThemeService;

    @GetMapping
    // @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get all themes for tenant")
    public ResponseEntity<List<StoreThemeResponse>> getAllThemes(@PathVariable Integer tenantId) {
        return ResponseEntity.ok(storeThemeService.getAllThemes(tenantId));
    }

    @GetMapping("/{themeId}")
    // @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get theme by ID")
    public ResponseEntity<StoreThemeResponse> getTheme(
            @PathVariable Integer tenantId,
            @PathVariable String themeId) {
        return ResponseEntity.ok(storeThemeService.getTheme(tenantId, themeId));
    }

}
