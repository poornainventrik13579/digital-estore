package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.DiscountCodeRequest;
import com.inventrik.digitalestore.dto.request.ValidateDiscountRequest;
import com.inventrik.digitalestore.dto.response.DiscountCodeResponse;
import com.inventrik.digitalestore.dto.response.DiscountValidationResponse;
import com.inventrik.digitalestore.security.TenantAccessValidator;
import com.inventrik.digitalestore.service.discount.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/discounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Discount Management", description = "APIs for managing discount codes")
@SecurityRequirement(name = "bearerAuth")
public class DiscountController {

    private final DiscountService discountService;
    private final TenantAccessValidator tenantAccessValidator;
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Create a new discount code", description = "Create a new discount code for the tenant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Discount code created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or discount code already exists"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<DiscountCodeResponse> createDiscountCode(
            @PathVariable Integer tenantId,
            @Valid @RequestBody DiscountCodeRequest request,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            DiscountCodeResponse response = discountService.createDiscountCode(tenantId, request, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create discount code: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{discountId}")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Update a discount code", description = "Update an existing discount code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Discount code updated successfully"),
        @ApiResponse(responseCode = "404", description = "Discount code not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<DiscountCodeResponse> updateDiscountCode(
            @PathVariable Integer tenantId,
            @PathVariable Long discountId,
            @Valid @RequestBody DiscountCodeRequest request,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            DiscountCodeResponse response = discountService.updateDiscountCode(tenantId, discountId, request, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update discount code: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{discountId}")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Get discount code by ID", description = "Retrieve a specific discount code by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Discount code found"),
        @ApiResponse(responseCode = "404", description = "Discount code not found")
    })
    public ResponseEntity<DiscountCodeResponse> getDiscountCode(
            @PathVariable Integer tenantId,
            @PathVariable Long discountId,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        DiscountCodeResponse response = discountService.getDiscountCode(tenantId, discountId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get discount code by code", description = "Retrieve a specific discount code by its code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Discount code found"),
        @ApiResponse(responseCode = "404", description = "Discount code not found")
    })
    public ResponseEntity<DiscountCodeResponse> getDiscountCodeByCode(
            @PathVariable Integer tenantId,
            @PathVariable String code,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        DiscountCodeResponse response = discountService.getDiscountCodeByCode(tenantId, code);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Get all discount codes", description = "Retrieve all discount codes for the tenant")
    @ApiResponse(responseCode = "200", description = "List of discount codes")
    public ResponseEntity<List<DiscountCodeResponse>> getAllDiscountCodes(
            @PathVariable Integer tenantId,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<DiscountCodeResponse> response = discountService.getAllDiscountCodes(tenantId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get active discount codes", description = "Retrieve all currently active discount codes")
    @ApiResponse(responseCode = "200", description = "List of active discount codes")
    public ResponseEntity<List<DiscountCodeResponse>> getActiveDiscountCodes(
            @PathVariable Integer tenantId,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<DiscountCodeResponse> response = discountService.getActiveDiscountCodes(tenantId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{discountId}")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Delete a discount code", description = "Soft delete a discount code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Discount code deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Discount code not found")
    })
    public ResponseEntity<Void> deleteDiscountCode(
            @PathVariable Integer tenantId,
            @PathVariable Long discountId,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        discountService.deleteDiscountCode(tenantId, discountId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/validate")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Validate discount code", description = "Validate a discount code against order amount")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validation completed"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<DiscountValidationResponse> validateDiscountCode(
            @PathVariable Integer tenantId,
            @Valid @RequestBody ValidateDiscountRequest request,
            Authentication authentication) {
        
        if (!tenantAccessValidator.verifyTenantAccess(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        DiscountValidationResponse response = discountService.validateDiscountCode(tenantId, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{discountId}/usage")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Get discount usage statistics", description = "Get usage count and total amount for a discount code")
    @ApiResponse(responseCode = "200", description = "Usage statistics")
    public ResponseEntity<Map<String, Object>> getDiscountUsageStats(
            @PathVariable Integer tenantId,
            @PathVariable Long discountId,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        long usageCount = discountService.getDiscountUsageCount(tenantId, discountId);
        BigDecimal totalAmount = discountService.getTotalDiscountAmountUsed(tenantId, discountId);
        
        Map<String, Object> stats = Map.of(
            "usageCount", usageCount,
            "totalDiscountAmount", totalAmount,
            "discountId", discountId
        );
        
        return ResponseEntity.ok(stats);
    }
    
    @PostMapping("/cleanup-expired")
    @PreAuthorize("hasRole('ROLE_TENANT_ADMIN')")
    @Operation(summary = "Cleanup expired discounts", description = "Deactivate all expired discount codes")
    @ApiResponse(responseCode = "200", description = "Cleanup completed")
    public ResponseEntity<Map<String, String>> cleanupExpiredDiscounts(
            @PathVariable Integer tenantId,
            Authentication authentication) {
        
        if (!tenantAccessValidator.isTenantAdmin(authentication, tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        discountService.deactivateExpiredDiscounts(tenantId);
        return ResponseEntity.ok(Map.of("message", "Expired discount codes have been deactivated"));
    }
} 