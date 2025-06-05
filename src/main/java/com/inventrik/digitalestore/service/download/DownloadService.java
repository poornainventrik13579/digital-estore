package com.inventrik.digitalestore.service.download;

import com.inventrik.digitalestore.dto.request.DigitalProductDetailsRequest;
import com.inventrik.digitalestore.dto.response.DigitalProductDetailsResponse;
import com.inventrik.digitalestore.dto.response.DownloadTokenResponse;
import com.inventrik.digitalestore.dto.response.DownloadHistoryResponse;
import org.springframework.core.io.Resource;

import java.util.List;

public interface DownloadService {
    
    // FIXED: Generate secure download token for order item (now includes orderId)
    DownloadTokenResponse generateDownloadToken(Integer tenantId, Long orderId, Long orderItemId, String username, String ipAddress, String userAgent);
    
    // Validate download access and return file resource
    Resource validateDownloadAccess(String downloadToken, String ipAddress);
    
    // Record download completion
    void recordDownloadCompletion(String downloadToken, Long fileSizeDownloaded);
    
    // FIXED: Get download history for order item (now includes orderId)
    List<DownloadHistoryResponse> getDownloadHistory(Integer tenantId, Long orderId, Long orderItemId);
    
    // Get download history for user
    List<DownloadHistoryResponse> getUserDownloadHistory(Integer tenantId, Long userId);
    
    // Get download history for product
    List<DownloadHistoryResponse> getProductDownloadHistory(Integer tenantId, Long productId);
    
    // FIXED: Check remaining downloads for order item (now includes orderId)
    int getRemainingDownloads(Integer tenantId, Long orderId, Long orderItemId);
    
    // Clean up expired tokens
    void cleanupExpiredTokens();
    
    // Digital Product Details Management
    DigitalProductDetailsResponse createDigitalProductDetails(Integer tenantId, String username, DigitalProductDetailsRequest request);
    
    DigitalProductDetailsResponse updateDigitalProductDetails(Integer tenantId, Long productId, String username, DigitalProductDetailsRequest request);
    
    DigitalProductDetailsResponse getDigitalProductDetails(Integer tenantId, Long productId);
    
    List<DigitalProductDetailsResponse> getAllDigitalProductDetails(Integer tenantId);
    
    void deleteDigitalProductDetails(Integer tenantId, Long productId);
    
    // Check if product has digital details
    boolean hasDigitalDetails(Integer tenantId, Long productId);
}