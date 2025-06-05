package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.DigitalProductDetailsRequest;
import com.inventrik.digitalestore.dto.response.DigitalProductDetailsResponse;
import com.inventrik.digitalestore.dto.response.DownloadTokenResponse;
import com.inventrik.digitalestore.dto.response.DownloadHistoryResponse;
import com.inventrik.digitalestore.service.download.DownloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Download Management", description = "APIs for managing digital product downloads")
@Slf4j
public class DownloadController {
    
    private final DownloadService downloadService;
    
    @PostMapping("/tenants/{tenantId}/order-items/{orderItemId}/download-token")
    @Operation(summary = "Generate download token for order item")
    public ResponseEntity<DownloadTokenResponse> generateDownloadToken(
            @PathVariable Integer tenantId,
            @PathVariable Long orderItemId,
            HttpServletRequest request,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        DownloadTokenResponse response = downloadService.generateDownloadToken(
                tenantId, orderItemId, username, ipAddress, userAgent);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/downloads/{token}")
    @Operation(summary = "Download file using token")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String token,
            HttpServletRequest request) {
        
        String ipAddress = getClientIpAddress(request);
        Resource file = downloadService.validateDownloadAccess(token, ipAddress);
        
        // Record download completion (this could be done via a separate endpoint after successful download)
        try {
            downloadService.recordDownloadCompletion(token, file.contentLength());
        } catch (Exception e) {
            log.warn("Could not record download completion: {}", e.getMessage());
        }
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }
    
    @PostMapping("/downloads/{token}/complete")
    @Operation(summary = "Record download completion")
    public ResponseEntity<Void> recordDownloadCompletion(
            @PathVariable String token,
            @RequestParam Long fileSize) {
        
        downloadService.recordDownloadCompletion(token, fileSize);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/tenants/{tenantId}/order-items/{orderItemId}/download-history")
    @Operation(summary = "Get download history for order item")
    public ResponseEntity<List<DownloadHistoryResponse>> getDownloadHistory(
            @PathVariable Integer tenantId,
            @PathVariable Long orderItemId) {
        
        List<DownloadHistoryResponse> history = downloadService.getDownloadHistory(tenantId, orderItemId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/tenants/{tenantId}/users/{userId}/download-history")
    @Operation(summary = "Get download history for user")
    public ResponseEntity<List<DownloadHistoryResponse>> getUserDownloadHistory(
            @PathVariable Integer tenantId,
            @PathVariable Long userId) {
        
        List<DownloadHistoryResponse> history = downloadService.getUserDownloadHistory(tenantId, userId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/tenants/{tenantId}/products/{productId}/download-history")
    @Operation(summary = "Get download history for product")
    public ResponseEntity<List<DownloadHistoryResponse>> getProductDownloadHistory(
            @PathVariable Integer tenantId,
            @PathVariable Long productId) {
        
        List<DownloadHistoryResponse> history = downloadService.getProductDownloadHistory(tenantId, productId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/tenants/{tenantId}/order-items/{orderItemId}/remaining-downloads")
    @Operation(summary = "Get remaining downloads for order item")
    public ResponseEntity<Integer> getRemainingDownloads(
            @PathVariable Integer tenantId,
            @PathVariable Long orderItemId) {
        
        int remaining = downloadService.getRemainingDownloads(tenantId, orderItemId);
        return ResponseEntity.ok(remaining);
    }
    
    // Digital Product Details Management
    @PostMapping("/tenants/{tenantId}/digital-product-details")
    @Operation(summary = "Create digital product details")
    public ResponseEntity<DigitalProductDetailsResponse> createDigitalProductDetails(
            @PathVariable Integer tenantId,
            @Valid @RequestBody DigitalProductDetailsRequest request,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        DigitalProductDetailsResponse response = downloadService.createDigitalProductDetails(tenantId, username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/tenants/{tenantId}/digital-product-details/{productId}")
    @Operation(summary = "Update digital product details")
    public ResponseEntity<DigitalProductDetailsResponse> updateDigitalProductDetails(
            @PathVariable Integer tenantId,
            @PathVariable Long productId,
            @Valid @RequestBody DigitalProductDetailsRequest request,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        DigitalProductDetailsResponse response = downloadService.updateDigitalProductDetails(tenantId, productId, username, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/tenants/{tenantId}/digital-product-details/{productId}")
    @Operation(summary = "Get digital product details")
    public ResponseEntity<DigitalProductDetailsResponse> getDigitalProductDetails(
            @PathVariable Integer tenantId,
            @PathVariable Long productId) {
        
        DigitalProductDetailsResponse response = downloadService.getDigitalProductDetails(tenantId, productId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/tenants/{tenantId}/digital-product-details")
    @Operation(summary = "Get all digital product details")
    public ResponseEntity<List<DigitalProductDetailsResponse>> getAllDigitalProductDetails(
            @PathVariable Integer tenantId) {
        
        List<DigitalProductDetailsResponse> response = downloadService.getAllDigitalProductDetails(tenantId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/tenants/{tenantId}/digital-product-details/{productId}")
    @Operation(summary = "Delete digital product details")
    public ResponseEntity<Void> deleteDigitalProductDetails(
            @PathVariable Integer tenantId,
            @PathVariable Long productId) {
        
        downloadService.deleteDigitalProductDetails(tenantId, productId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/admin/cleanup-expired-tokens")
    @Operation(summary = "Cleanup expired download tokens")
    public ResponseEntity<Void> cleanupExpiredTokens() {
        downloadService.cleanupExpiredTokens();
        return ResponseEntity.ok().build();
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}