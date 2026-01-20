package com.inventrik.digitalestore.service.download;

import com.inventrik.digitalestore.dto.request.DigitalProductDetailsRequest;
import com.inventrik.digitalestore.dto.response.DigitalProductDetailsResponse;
import com.inventrik.digitalestore.dto.response.DownloadHistoryResponse;

import java.util.List;

public interface DownloadService {
    
    // Record a download
    void recordDownload(Integer tenantId, String orderItemId, String ipAddress, String username);

    // Get download history for order item
    List<DownloadHistoryResponse> getDownloadHistory(Integer tenantId, String orderItemId);

    // Get download history for user (through order items)
    List<DownloadHistoryResponse> getUserDownloadHistory(Integer tenantId, String username, boolean canAccessAllDownloads, String userId);
    
    // Digital Product Details Management
    DigitalProductDetailsResponse createDigitalProductDetails(Integer tenantId, String username, DigitalProductDetailsRequest request);
    
    DigitalProductDetailsResponse updateDigitalProductDetails(Integer tenantId, String productId, String username, DigitalProductDetailsRequest request);

    DigitalProductDetailsResponse getDigitalProductDetails(Integer tenantId, String productId);

    List<DigitalProductDetailsResponse> getAllDigitalProductDetails(Integer tenantId);

    void deleteDigitalProductDetails(Integer tenantId, String productId);

    // Check if product has digital details
    boolean hasDigitalDetails(Integer tenantId, String productId);
}