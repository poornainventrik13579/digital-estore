package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.DigitalProductDetailsRequest;
import com.inventrik.digitalestore.dto.response.DigitalProductDetailsResponse;
import com.inventrik.digitalestore.dto.response.DownloadHistoryResponse;
import com.inventrik.digitalestore.service.download.DownloadService;
import com.inventrik.digitalestore.util.HttpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Download Management", description = "APIs for managing digital product downloads")
@SecurityRequirement(name = "oauth2")
@Slf4j
public class DownloadController {

    private final DownloadService downloadService;

    @PostMapping("/tenants/{tenantId}/order-items/{orderItemId}/record-download")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Record a download for order item")
    public ResponseEntity<Void> recordDownload(
            @PathVariable Integer tenantId,
            @PathVariable String orderItemId,
            HttpServletRequest request,
            Authentication authentication) {

        String username = (authentication != null) ? authentication.getName() : "system";
        String ipAddress = HttpUtils.getClientIpAddress(request);

        downloadService.recordDownload(tenantId, orderItemId, ipAddress, username);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/tenants/{tenantId}/order-items/{orderItemId}/download-history")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get download history for order item")
    public ResponseEntity<List<DownloadHistoryResponse>> getDownloadHistory(
            @PathVariable Integer tenantId,
            @PathVariable String orderItemId) {

        List<DownloadHistoryResponse> history = downloadService.getDownloadHistory(tenantId, orderItemId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/tenants/{tenantId}/users/{userId}/download-history")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get download history for user. Regular users can only see their own history.")
    public ResponseEntity<List<DownloadHistoryResponse>> getUserDownloadHistory(
            @PathVariable Integer tenantId,
            @PathVariable String userId,
            Authentication authentication) {

        String username = authentication.getName();

        List<DownloadHistoryResponse> history = downloadService.getUserDownloadHistory(
                tenantId, username, true, userId);

        return ResponseEntity.ok(history);
    }

    @PostMapping("/tenants/{tenantId}/digital-product-details")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
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
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Update digital product details")
    public ResponseEntity<DigitalProductDetailsResponse> updateDigitalProductDetails(
            @PathVariable Integer tenantId,
            @PathVariable String productId,
            @Valid @RequestBody DigitalProductDetailsRequest request,
            Authentication authentication) {

        String username = (authentication != null) ? authentication.getName() : "system";
        DigitalProductDetailsResponse response = downloadService.updateDigitalProductDetails(tenantId, productId, username, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tenants/{tenantId}/digital-product-details/{productId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get digital product details")
    public ResponseEntity<DigitalProductDetailsResponse> getDigitalProductDetails(
            @PathVariable Integer tenantId,
            @PathVariable String productId) {

        DigitalProductDetailsResponse response = downloadService.getDigitalProductDetails(tenantId, productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tenants/{tenantId}/digital-product-details")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get all digital product details")
    public ResponseEntity<List<DigitalProductDetailsResponse>> getAllDigitalProductDetails(
            @PathVariable Integer tenantId) {

        List<DigitalProductDetailsResponse> response = downloadService.getAllDigitalProductDetails(tenantId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/tenants/{tenantId}/digital-product-details/{productId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Delete digital product details")
    public ResponseEntity<Void> deleteDigitalProductDetails(
            @PathVariable Integer tenantId,
            @PathVariable String productId) {

        downloadService.deleteDigitalProductDetails(tenantId, productId);
        return ResponseEntity.noContent().build();
    }
}
