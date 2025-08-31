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
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant Management", description = "APIs for managing tenants and store configurations")
@SecurityRequirement(name = "oauth2")
public class TenantController {

    private final TenantService tenantService;
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get all tenants")
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }
    
    @GetMapping("/{tenantId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get a tenant by ID")
    public ResponseEntity<TenantResponse> getTenant(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId) {
        return ResponseEntity.ok(tenantService.getTenant(tenantId));
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create a new tenant (JSON)")
    public ResponseEntity<TenantResponse> createTenantJson(
            @Valid @RequestBody TenantRequest tenantRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        TenantResponse createdTenant = tenantService.createTenant(username, tenantRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTenant);
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create a new tenant (Form)")
    public ResponseEntity<TenantResponse> createTenant(
            @Valid @ModelAttribute TenantRequest tenantRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        TenantResponse createdTenant = tenantService.createTenant(username, tenantRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTenant);
    }
    
    @PutMapping(path = "/{tenantId}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update a tenant (JSON)")
    public ResponseEntity<TenantResponse> updateTenantJson(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Valid @RequestBody TenantUpdateRequest updateRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        TenantResponse updatedTenant = tenantService.updateTenant(tenantId, username, updateRequest);
        return ResponseEntity.ok(updatedTenant);
    }
    
    @PutMapping(path = "/{tenantId}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update a tenant (Form)")
    public ResponseEntity<TenantResponse> updateTenant(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Valid @ModelAttribute TenantUpdateRequest updateRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        TenantResponse updatedTenant = tenantService.updateTenant(tenantId, username, updateRequest);
        return ResponseEntity.ok(updatedTenant);
    }
    
    @DeleteMapping("/{tenantId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete a tenant")
    public ResponseEntity<Void> deleteTenant(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId) {
        tenantService.deleteTenant(tenantId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/domain/{domainName}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get tenant by domain name")
    public ResponseEntity<TenantResponse> getTenantByDomain(
            @Parameter(description = "Domain name", required = true)
            @PathVariable String domainName) {
        return ResponseEntity.ok(tenantService.getTenantByDomain(domainName));
    }
    
    @GetMapping("/subdomain/{subdomain}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get tenant by subdomain")
    public ResponseEntity<TenantResponse> getTenantBySubdomain(
            @Parameter(description = "Subdomain", required = true)
            @PathVariable String subdomain) {
        return ResponseEntity.ok(tenantService.getTenantBySubdomain(subdomain));
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get tenants by status")
    public ResponseEntity<List<TenantResponse>> getTenantsByStatus(
            @Parameter(description = "Status", required = true)
            @PathVariable String status) {
        return ResponseEntity.ok(tenantService.getTenantsByStatus(status));
    }
    
    @GetMapping("/country/{countryRegion}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get tenants by country/region")
    public ResponseEntity<List<TenantResponse>> getTenantsByCountry(
            @Parameter(description = "Country/Region", required = true)
            @PathVariable String countryRegion) {
        return ResponseEntity.ok(tenantService.getTenantsByCountry(countryRegion));
    }
    
    @GetMapping("/check/email/{email}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Check if email exists")
    public ResponseEntity<Boolean> checkEmailExists(
            @Parameter(description = "Email address", required = true)
            @PathVariable String email) {
        return ResponseEntity.ok(tenantService.existsByEmail(email));
    }
    
    @GetMapping("/check/domain/{domainName}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Check if domain exists")
    public ResponseEntity<Boolean> checkDomainExists(
            @Parameter(description = "Domain name", required = true)
            @PathVariable String domainName) {
        return ResponseEntity.ok(tenantService.existsByDomain(domainName));
    }
    
    @GetMapping("/check/subdomain/{subdomain}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Check if subdomain exists")
    public ResponseEntity<Boolean> checkSubdomainExists(
            @Parameter(description = "Subdomain", required = true)
            @PathVariable String subdomain) {
        return ResponseEntity.ok(tenantService.existsBySubdomain(subdomain));
    }
}
