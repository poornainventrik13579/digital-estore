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
import java.util.List;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant Management", description = "APIs for managing tenants and store configurations")
@SecurityRequirement(name = "bearer-jwt")
public class TenantController {

    private final TenantService tenantService;
    
    /**
     * Extract tenant ID from JWT token
     */
    private Integer extractTenantIdFromJwt(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return null;
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("tenant_id");
    }
    
    /**
     * Verify that the tenant ID in the JWT matches the requested tenant ID
     */
    private boolean verifyTenantAccess(Authentication authentication, Integer requestedTenantId) {
        Integer tokenTenantId = extractTenantIdFromJwt(authentication);
        return tokenTenantId != null && tokenTenantId.equals(requestedTenantId);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Only admins can see all tenants
    @Operation(summary = "Get all tenants (Admin only)")
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }
    
    @GetMapping("/{tenantId}")
    @Operation(summary = "Get tenant details", description = "Get tenant details - tenant can only access their own data")
    public ResponseEntity<TenantResponse> getTenant(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            Authentication authentication) {
        
        // Verify tenant can only access their own data
        if (!verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(tenantService.getTenant(tenantId));
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Only admins can create tenants via this endpoint
    @Operation(summary = "Create a new tenant (Admin only)", 
               description = "Create tenant via admin API - for tenant self-registration use /api/v1/tenant-auth/signup")
    public ResponseEntity<TenantResponse> createTenantJson(
            @Valid @RequestBody TenantRequest tenantRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "admin";
        TenantResponse createdTenant = tenantService.createTenant(username, tenantRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTenant);
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Only admins can create tenants via this endpoint
    @Operation(summary = "Create a new tenant (Admin Form)", 
               description = "Create tenant via admin form API - for tenant self-registration use /api/v1/tenant-auth/signup")
    public ResponseEntity<TenantResponse> createTenant(
            @Valid @ModelAttribute TenantRequest tenantRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "admin";
        TenantResponse createdTenant = tenantService.createTenant(username, tenantRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTenant);
    }
    
    @PutMapping(path = "/{tenantId}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Update tenant details", description = "Update tenant details - tenant can only update their own data")
    public ResponseEntity<TenantResponse> updateTenantJson(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Valid @RequestBody TenantUpdateRequest updateRequest,
            Authentication authentication) {
        
        // Verify tenant can only update their own data
        if (!verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String username = (authentication != null) ? authentication.getName() : "tenant";
        TenantResponse updatedTenant = tenantService.updateTenant(tenantId, username, updateRequest);
        return ResponseEntity.ok(updatedTenant);
    }
    
    @PutMapping(path = "/{tenantId}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Update tenant details (Form)", description = "Update tenant details via form - tenant can only update their own data")
    public ResponseEntity<TenantResponse> updateTenant(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            @Valid @ModelAttribute TenantUpdateRequest updateRequest,
            Authentication authentication) {
        
        // Verify tenant can only update their own data
        if (!verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String username = (authentication != null) ? authentication.getName() : "tenant";
        TenantResponse updatedTenant = tenantService.updateTenant(tenantId, username, updateRequest);
        return ResponseEntity.ok(updatedTenant);
    }
    
    @DeleteMapping("/{tenantId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Only admins can delete tenants
    @Operation(summary = "Delete a tenant (Admin only)")
    public ResponseEntity<Void> deleteTenant(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId) {
        tenantService.deleteTenant(tenantId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/domain/{domainName}")
    @Operation(summary = "Get tenant by domain name", description = "Public endpoint for domain-based tenant lookup")
    public ResponseEntity<TenantResponse> getTenantByDomain(
            @Parameter(description = "Domain name", required = true)
            @PathVariable String domainName) {
        return ResponseEntity.ok(tenantService.getTenantByDomain(domainName));
    }
    
    @GetMapping("/subdomain/{subdomain}")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get tenant by subdomain")
    public ResponseEntity<TenantResponse> getTenantBySubdomain(
            @Parameter(description = "Subdomain", required = true)
            @PathVariable String subdomain) {
        return ResponseEntity.ok(tenantService.getTenantBySubdomain(subdomain));
    }
    
    @GetMapping("/status/{status}")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get tenants by status")
    public ResponseEntity<List<TenantResponse>> getTenantsByStatus(
            @Parameter(description = "Status", required = true)
            @PathVariable String status) {
        return ResponseEntity.ok(tenantService.getTenantsByStatus(status));
    }
    
    @GetMapping("/country/{countryRegion}")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get tenants by country/region")
    public ResponseEntity<List<TenantResponse>> getTenantsByCountry(
            @Parameter(description = "Country/Region", required = true)
            @PathVariable String countryRegion) {
        return ResponseEntity.ok(tenantService.getTenantsByCountry(countryRegion));
    }
    
    @GetMapping("/check/email/{email}")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Check if email exists")
    public ResponseEntity<Boolean> checkEmailExists(
            @Parameter(description = "Email address", required = true)
            @PathVariable String email) {
        return ResponseEntity.ok(tenantService.existsByEmail(email));
    }
    
    @GetMapping("/check/domain/{domainName}")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Check if domain exists")
    public ResponseEntity<Boolean> checkDomainExists(
            @Parameter(description = "Domain name", required = true)
            @PathVariable String domainName) {
        return ResponseEntity.ok(tenantService.existsByDomain(domainName));
    }
    
    @GetMapping("/check/subdomain/{subdomain}")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Check if subdomain exists")
    public ResponseEntity<Boolean> checkSubdomainExists(
            @Parameter(description = "Subdomain", required = true)
            @PathVariable String subdomain) {
        return ResponseEntity.ok(tenantService.existsBySubdomain(subdomain));
    }
}
