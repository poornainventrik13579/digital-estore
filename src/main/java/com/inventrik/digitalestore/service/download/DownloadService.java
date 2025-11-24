package com.inventrik.digitalestore.service.download;

import com.inventrik.digitalestore.dto.request.DigitalProductDetailsRequest;
import com.inventrik.digitalestore.dto.response.DigitalProductDetailsResponse;
import com.inventrik.digitalestore.dto.response.DownloadHistoryResponse;

import java.util.List;

public interface DownloadService {
    
    void recordDownload(Integer tenantId, Long orderItemId, String ipAddress, String username);
    
    List<DownloadHistoryResponse> getDownloadHistory(Integer tenantId, Long orderItemId);
    
    List<DownloadHistoryResponse> getUserDownloadHistory(Integer tenantId, Long userId);
    
    DigitalProductDetailsResponse createDigitalProductDetails(Integer tenantId, String username, DigitalProductDetailsRequest request);
    
    DigitalProductDetailsResponse updateDigitalProductDetails(Integer tenantId, Long productId, String username, DigitalProductDetailsRequest request);
    
    DigitalProductDetailsResponse getDigitalProductDetails(Integer tenantId, Long productId);
    
    List<DigitalProductDetailsResponse> getAllDigitalProductDetails(Integer tenantId);
    
    void deleteDigitalProductDetails(Integer tenantId, Long productId);
    
    boolean hasDigitalDetails(Integer tenantId, Long productId);
}