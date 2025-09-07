package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.response.TenantResponse;
import com.inventrik.digitalestore.service.tenant.SubdomainResolverService;
import com.inventrik.digitalestore.service.tenant.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Store Information", description = "Public APIs for store information and subdomain availability")
@Slf4j
public class StoreController {

    private final TenantService tenantService;
    private final SubdomainResolverService subdomainResolver;
    
    @GetMapping("/{subdomain}/store/info")
    @Operation(summary = "Get store information")
    public ResponseEntity<TenantResponse> getStoreInfo(@PathVariable String subdomain) {
        Integer tenantId = subdomainResolver.resolveTenantId(subdomain);
        TenantResponse storeInfo = tenantService.getTenant(tenantId);
        return ResponseEntity.ok(storeInfo);
    }
    
    @GetMapping("/check/subdomain/{subdomain}")
    @Operation(summary = "Check subdomain availability")
    public ResponseEntity<Map<String, Object>> checkSubdomainAvailability(@PathVariable String subdomain) {
        boolean isValidFormat = subdomainResolver.isValidSubdomainFormat(subdomain);
        boolean isAvailable = isValidFormat && subdomainResolver.isSubdomainAvailable(subdomain);
        
        return ResponseEntity.ok(Map.of(
            "subdomain", subdomain,
            "available", isAvailable,
            "validFormat", isValidFormat,
            "message", isAvailable ? "Subdomain is available" : 
                      !isValidFormat ? "Invalid subdomain format" : "Subdomain is already taken"
        ));
    }
    
    @GetMapping("/{subdomain}/store/status")
    @Operation(summary = "Get store status")
    public ResponseEntity<Map<String, Object>> getStoreStatus(@PathVariable String subdomain) {
        try {
            Integer tenantId = subdomainResolver.resolveTenantId(subdomain);
            TenantResponse store = tenantService.getTenant(tenantId);
            
            return ResponseEntity.ok(Map.of(
                "subdomain", subdomain,
                "status", "active",
                "storeName", store.getShopName(),
                "online", true
            ));
            
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "subdomain", subdomain,
                "status", "inactive",
                "online", false,
                "message", "Store not found or inactive"
            ));
        }
    }
}
