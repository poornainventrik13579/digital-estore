package com.inventrik.digitalestore.service.download;

import com.inventrik.digitalestore.domain.download.DigitalDownload;
import com.inventrik.digitalestore.domain.product.DigitalProductDetails;
import com.inventrik.digitalestore.dto.request.DigitalProductDetailsRequest;
import com.inventrik.digitalestore.dto.response.DigitalProductDetailsResponse;
import com.inventrik.digitalestore.dto.response.DownloadHistoryResponse;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.DigitalDownloadRepository;
import com.inventrik.digitalestore.repository.DigitalProductDetailsRepository;
import com.inventrik.digitalestore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DownloadServiceImpl implements DownloadService {
    
    private final DigitalDownloadRepository digitalDownloadRepository;
    private final DigitalProductDetailsRepository digitalProductDetailsRepository;
    private final ProductRepository productRepository;
    
    @Override
    @Transactional
    public void recordDownload(Integer tenantId, Long orderItemId, String ipAddress, String username) {
        // Generate a new download ID
        Long newDownloadId = System.currentTimeMillis();
        
        // Create download record
        DigitalDownload download = new DigitalDownload();
        download.setDownloadId(newDownloadId);
        download.setTenantId(tenantId);
        download.setOrderItemId(orderItemId);
        download.setDownloadDate(LocalDateTime.now());
        download.setIpAddress(ipAddress);
        download.setStatus("0"); // Active
        
        String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
        download.setCreatedBy(truncatedUsername);
        download.setUpdatedBy(truncatedUsername);
        download.setCreated(LocalDateTime.now());
        download.setUpdated(LocalDateTime.now());
        
        digitalDownloadRepository.save(download);
        
        log.info("Download recorded for order item {} by user {}", orderItemId, username);
    }
    
    @Override
    public List<DownloadHistoryResponse> getDownloadHistory(Integer tenantId, Long orderItemId) {
        List<DigitalDownload> downloads = digitalDownloadRepository.findByTenantIdAndOrderItemId(tenantId, orderItemId);
        return downloads.stream()
                .map(this::mapToDownloadHistoryResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DownloadHistoryResponse> getUserDownloadHistory(Integer tenantId, Long userId) {
        List<DigitalDownload> downloads = digitalDownloadRepository.findByTenantIdAndUserId(tenantId, userId);
        return downloads.stream()
                .map(this::mapToDownloadHistoryResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public DigitalProductDetailsResponse createDigitalProductDetails(Integer tenantId, String username, DigitalProductDetailsRequest request) {
        // Verify product exists
        productRepository.findByTenantIdAndProductId(tenantId, request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
        
        // Check if digital details already exist
        if (digitalProductDetailsRepository.existsByTenantIdAndProductId(tenantId, request.getProductId())) {
            throw new ResourceNotFoundException("Digital product details already exist for product: " + request.getProductId());
        }
        
        DigitalProductDetails digitalDetails = new DigitalProductDetails();
        digitalDetails.setProductId(request.getProductId());
        digitalDetails.setTenantId(tenantId);
        digitalDetails.setFileUrl(request.getFileUrl());
        digitalDetails.setFileSize(request.getFileSize());
        digitalDetails.setFileFormat(request.getFileFormat());
        digitalDetails.setLicenseInfo(request.getLicenseInfo());
        digitalDetails.setVersion(request.getVersion());
        digitalDetails.setStatus(request.getStatus() != null ? request.getStatus() : "0");
        
        String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
        digitalDetails.setCreatedBy(truncatedUsername);
        digitalDetails.setUpdatedBy(truncatedUsername);
        digitalDetails.setCreated(LocalDateTime.now());
        digitalDetails.setUpdated(LocalDateTime.now());
        
        DigitalProductDetails saved = digitalProductDetailsRepository.save(digitalDetails);
        return mapToDigitalProductDetailsResponse(saved);
    }
    
    @Override
    @Transactional
    public DigitalProductDetailsResponse updateDigitalProductDetails(Integer tenantId, Long productId, String username, DigitalProductDetailsRequest request) {
        DigitalProductDetails digitalDetails = digitalProductDetailsRepository.findByTenantIdAndProductId(tenantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Digital product details not found for product: " + productId));
        
        // Update fields
        if (request.getFileUrl() != null) digitalDetails.setFileUrl(request.getFileUrl());
        if (request.getFileSize() != null) digitalDetails.setFileSize(request.getFileSize());
        if (request.getFileFormat() != null) digitalDetails.setFileFormat(request.getFileFormat());
        if (request.getLicenseInfo() != null) digitalDetails.setLicenseInfo(request.getLicenseInfo());
        if (request.getVersion() != null) digitalDetails.setVersion(request.getVersion());
        if (request.getStatus() != null) digitalDetails.setStatus(request.getStatus());
        
        String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
        digitalDetails.setUpdatedBy(truncatedUsername);
        digitalDetails.setUpdated(LocalDateTime.now());
        
        DigitalProductDetails updated = digitalProductDetailsRepository.save(digitalDetails);
        return mapToDigitalProductDetailsResponse(updated);
    }
    
    @Override
    public DigitalProductDetailsResponse getDigitalProductDetails(Integer tenantId, Long productId) {
        DigitalProductDetails digitalDetails = digitalProductDetailsRepository.findByTenantIdAndProductId(tenantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Digital product details not found for product: " + productId));
        return mapToDigitalProductDetailsResponse(digitalDetails);
    }
    
    @Override
    public List<DigitalProductDetailsResponse> getAllDigitalProductDetails(Integer tenantId) {
        return digitalProductDetailsRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDigitalProductDetailsResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteDigitalProductDetails(Integer tenantId, Long productId) {
        if (!digitalProductDetailsRepository.existsByTenantIdAndProductId(tenantId, productId)) {
            throw new ResourceNotFoundException("Digital product details not found for product: " + productId);
        }
        digitalProductDetailsRepository.deleteByTenantIdAndProductId(tenantId, productId);
    }
    
    @Override
    public boolean hasDigitalDetails(Integer tenantId, Long productId) {
        return digitalProductDetailsRepository.existsByTenantIdAndProductId(tenantId, productId);
    }
    
    private DownloadHistoryResponse mapToDownloadHistoryResponse(DigitalDownload download) {
        return new DownloadHistoryResponse(
                download.getDownloadId(),
                download.getOrderItemId(),
                download.getDownloadDate(),
                download.getIpAddress(),
                download.getStatus()
        );
    }
    
    private DigitalProductDetailsResponse mapToDigitalProductDetailsResponse(DigitalProductDetails details) {
        return new DigitalProductDetailsResponse(
                details.getProductId(),
                details.getTenantId(),
                details.getFileUrl(),
                details.getFileSize(),
                details.getFileFormat(),
                details.getLicenseInfo(),
                details.getVersion(),
                details.getStatus(),
                details.getCreated(),
                details.getUpdated()
        );
    }
}