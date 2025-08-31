package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.response.StoreThemeResponse;
import com.inventrik.digitalestore.service.theme.StoreThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/themes")
@RequiredArgsConstructor
@Tag(name = "Global Store Theme Management", description = "Global APIs for managing store themes across all tenants")
@SecurityRequirement(name = "oauth2")
public class GlobalStoreThemeController {

    private final StoreThemeService storeThemeService;
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get all themes across all tenants")
    public ResponseEntity<List<StoreThemeResponse>> getAllThemes() {
        return ResponseEntity.ok(storeThemeService.getAllThemes());
    }
    
    @GetMapping("/name/{themeName}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get themes by name across all tenants")
    public ResponseEntity<List<StoreThemeResponse>> getThemesByName(
            @Parameter(description = "Theme name", required = true)
            @PathVariable String themeName) {
        return ResponseEntity.ok(storeThemeService.getThemesByName(themeName));
    }
}
