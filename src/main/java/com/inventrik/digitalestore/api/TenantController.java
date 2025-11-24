package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.TenantRequest;
import com.inventrik.digitalestore.dto.request.TenantUpdateRequest;
import com.inventrik.digitalestore.dto.response.TenantResponse;
import com.inventrik.digitalestore.service.tenant.TenantService;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Tenant Management", description = "Admin APIs for managing tenants across the platform")
@SecurityRequirement(name = "oauth2")
public class TenantController {

    private final TenantService tenantService;
    
    private Integer extractTenantIdFromJwt(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return null;
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("tenant_id");
    }
    
    private boolean verifyTenantAccess(Authentication authentication, Integer requestedTenantId) {
        Integer tokenTenantId = extractTenantIdFromJwt(authentication);
        return tokenTenantId != null && tokenTenantId.equals(requestedTenantId);
    }
    
    @GetMapping("/tenants")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Get all tenants")
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }
    
    @GetMapping("/tenants/{tenantId}")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN') or hasRole('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Get tenant details")
    public ResponseEntity<TenantResponse> getTenant(
            @PathVariable Integer tenantId,
            Authentication authentication) {

        if (!verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(tenantService.getTenant(tenantId));
    }

    @PostMapping("/tenants")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Create a new tenant")
    public ResponseEntity<TenantResponse> createTenantJson(
            @Valid @RequestBody TenantRequest tenantRequest,
            Authentication authentication) {

        String username = (authentication != null) ? authentication.getName() : "admin";
        TenantResponse createdTenant = tenantService.createTenant(username, tenantRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTenant);
    }


    @PutMapping("/tenants/{tenantId}")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Update tenant details")
    public ResponseEntity<TenantResponse> updateTenantJson(
            @PathVariable Integer tenantId,
            @Valid @RequestBody TenantUpdateRequest updateRequest,
            Authentication authentication) {

        if (!verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String username = (authentication != null) ? authentication.getName() : "tenant";
        TenantResponse updatedTenant = tenantService.updateTenant(tenantId, username, updateRequest);
        return ResponseEntity.ok(updatedTenant);
    }


    @DeleteMapping("/tenants/{tenantId}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Delete a tenant")
    public ResponseEntity<Void> deleteTenant(@PathVariable Integer tenantId) {
        tenantService.deleteTenant(tenantId);
        return ResponseEntity.noContent().build();
    }
}
