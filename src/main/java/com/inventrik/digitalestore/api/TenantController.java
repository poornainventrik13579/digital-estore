package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.TenantRequest;
import com.inventrik.digitalestore.dto.response.TenantResponse;
import com.inventrik.digitalestore.service.tenant.TenantService;
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
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant Management")
@SecurityRequirement(name = "oauth2")
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get all tenants")
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    @GetMapping("/{tenantId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get tenant by ID")
    public ResponseEntity<TenantResponse> getTenant(@PathVariable Integer tenantId) {
        return ResponseEntity.ok(tenantService.getTenant(tenantId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Create new tenant")
    public ResponseEntity<TenantResponse> createTenant(
            @Valid @RequestBody TenantRequest request,
            Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "system";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tenantService.createTenant(request, username));
    }

    @PutMapping("/{tenantId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Update tenant")
    public ResponseEntity<TenantResponse> updateTenant(
            @PathVariable Integer tenantId,
            @Valid @RequestBody TenantRequest request,
            Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "system";
        return ResponseEntity.ok(tenantService.updateTenant(tenantId, request, username));
    }

    @DeleteMapping("/{tenantId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Delete tenant")
    public ResponseEntity<Void> deleteTenant(@PathVariable Integer tenantId) {
        tenantService.deleteTenant(tenantId);
        return ResponseEntity.noContent().build();
    }
}
