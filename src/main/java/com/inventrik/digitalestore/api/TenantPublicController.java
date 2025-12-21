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
@RequestMapping("/api/v1/public/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant Management")
@SecurityRequirement(name = "oauth2")
public class TenantPublicController {

    private final TenantService tenantService;

    @GetMapping
//    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get all tenants")
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    @GetMapping("/{tenantId}")
//    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get tenant by ID")
    public ResponseEntity<TenantResponse> getTenant(@PathVariable Integer tenantId) {
        return ResponseEntity.ok(tenantService.getTenant(tenantId));
    }
}