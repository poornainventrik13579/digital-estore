package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.DiscountCodeRequest;
import com.inventrik.digitalestore.dto.request.ValidateDiscountRequest;
import com.inventrik.digitalestore.dto.response.DiscountCodeResponse;
import com.inventrik.digitalestore.dto.response.DiscountValidationResponse;
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
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
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
        
        try {
            DiscountCodeResponse response = discountService.createDiscountCode(tenantId, request, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create discount code: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{discountId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
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
        
        try {
            DiscountCodeResponse response = discountService.updateDiscountCode(tenantId, discountId, request, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update discount code: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{discountId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get discount code by ID", description = "Retrieve a specific discount code by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Discount code found"),
        @ApiResponse(responseCode = "404", description = "Discount code not found")
    })
    public ResponseEntity<DiscountCodeResponse> getDiscountCode(
            @PathVariable Integer tenantId,
            @PathVariable Long discountId) {
        
        DiscountCodeResponse response = discountService.getDiscountCode(tenantId, discountId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get discount codes with optional filters: ?code={code} or ?status=ACTIVE")
    @ApiResponse(responseCode = "200", description = "List of discount codes")
    public ResponseEntity<List<DiscountCodeResponse>> getAllDiscountCodes(
            @PathVariable Integer tenantId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(discountService.getAllDiscountCodes(tenantId, code, status));
    }
    
    @DeleteMapping("/{discountId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Delete a discount code", description = "Soft delete a discount code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Discount code deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Discount code not found")
    })
    public ResponseEntity<Void> deleteDiscountCode(
            @PathVariable Integer tenantId,
            @PathVariable Long discountId,
            Authentication authentication) {
        
        discountService.deleteDiscountCode(tenantId, discountId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/validate")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Validate discount code", description = "Validate a discount code against order amount")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validation completed"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<DiscountValidationResponse> validateDiscountCode(
            @PathVariable Integer tenantId,
            @Valid @RequestBody ValidateDiscountRequest request) {
        
        DiscountValidationResponse response = discountService.validateDiscountCode(tenantId, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{discountId}/usage")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get discount usage statistics", description = "Get usage count and total amount for a discount code")
    @ApiResponse(responseCode = "200", description = "Usage statistics")
    public ResponseEntity<Map<String, Object>> getDiscountUsageStats(
            @PathVariable Integer tenantId,
            @PathVariable Long discountId) {
        
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
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Cleanup expired discounts", description = "Deactivate all expired discount codes")
    @ApiResponse(responseCode = "200", description = "Cleanup completed")
    public ResponseEntity<Map<String, String>> cleanupExpiredDiscounts(@PathVariable Integer tenantId) {
        discountService.deactivateExpiredDiscounts(tenantId);
        return ResponseEntity.ok(Map.of("message", "Expired discount codes have been deactivated"));
    }
} 