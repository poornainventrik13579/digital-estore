package com.inventrik.digitalestore.service.download;

import com.inventrik.digitalestore.domain.download.DigitalDownload;
import com.inventrik.digitalestore.domain.order.OrderItem;
import com.inventrik.digitalestore.domain.product.DigitalProductDetails;
import com.inventrik.digitalestore.dto.request.DigitalProductDetailsRequest;
import com.inventrik.digitalestore.dto.response.DigitalProductDetailsResponse;
import com.inventrik.digitalestore.dto.response.DownloadTokenResponse;
import com.inventrik.digitalestore.dto.response.DownloadHistoryResponse;
import com.inventrik.digitalestore.exception.download.DownloadException;
import com.inventrik.digitalestore.exception.download.DownloadExpiredException;
import com.inventrik.digitalestore.exception.download.DownloadLimitExceededException;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.DigitalDownloadRepository;
import com.inventrik.digitalestore.repository.DigitalProductDetailsRepository;
import com.inventrik.digitalestore.repository.OrderRepository;
import com.inventrik.digitalestore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DownloadServiceImpl implements DownloadService {
    
    private final DigitalDownloadRepository digitalDownloadRepository;
    private final DigitalProductDetailsRepository digitalProductDetailsRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    
    @Value("${app.download.token-expiry-hours:24}")
    private int tokenExpiryHours;
    
    @Value("${app.download.base-path:/app/downloads}")
    private String downloadBasePath;
    
    @Override
    @Transactional
    public DownloadTokenResponse generateDownloadToken(Integer tenantId, Long orderId, Long orderItemId, String username, String ipAddress, String userAgent) {
        // FIXED: Find order item and validate using composite key
        OrderItem orderItem = findOrderItemWithValidation(tenantId, orderId, orderItemId);
        
        // Get digital product details
        DigitalProductDetails digitalDetails = digitalProductDetailsRepository
                .findByTenantIdAndProductId(tenantId, orderItem.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Digital product details not found for product: " + orderItem.getProductId()));
        
        // Check download limits
        validateDownloadLimits(tenantId, orderId, orderItemId, digitalDetails);
        
        // Check expiry
        validateDownloadExpiry(orderItem, digitalDetails);
        
        // Generate unique token
        String token = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
        
        // Create download record
        DigitalDownload download = new DigitalDownload();
        download.setDownloadId(System.currentTimeMillis());
        download.setTenantId(tenantId);
        download.setOrderId(orderId);  // FIXED: Set orderId
        download.setOrderItemId(orderItemId);
        download.setDownloadDate(LocalDateTime.now());
        download.setIpAddress(ipAddress);
        download.setUserAgent(userAgent);
        download.setDownloadToken(token);
        download.setTokenExpiry(LocalDateTime.now().plusHours(tokenExpiryHours));
        download.setDownloadStatus("INITIATED");
        download.setStatus("0");
        
        String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
        download.setCreatedBy(truncatedUsername);
        download.setUpdatedBy(truncatedUsername);
        
        DigitalDownload savedDownload = digitalDownloadRepository.save(download);
        
        // Calculate remaining downloads
        int remainingDownloads = getRemainingDownloads(tenantId, orderId, orderItemId);
        
        return new DownloadTokenResponse(
                savedDownload.getDownloadToken(),
                savedDownload.getTokenExpiry(),
                "/api/v1/downloads/" + savedDownload.getDownloadToken(),
                remainingDownloads
        );
    }
    
    @Override
    @Transactional
    public Resource validateDownloadAccess(String downloadToken, String ipAddress) {
        // Find download by token
        DigitalDownload download = digitalDownloadRepository.findByDownloadToken(downloadToken)
                .orElseThrow(() -> new DownloadException("Invalid download token"));
        
        // Check if token is expired
        if (download.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new DownloadExpiredException("Download token has expired");
        }
        
        // Validate IP address (optional security check)
        if (!download.getIpAddress().equals(ipAddress)) {
            log.warn("Download attempted from different IP. Original: {}, Current: {}", 
                    download.getIpAddress(), ipAddress);
        }
        
        // Get digital product details
        OrderItem orderItem = download.getOrderItem();
        DigitalProductDetails digitalDetails = digitalProductDetailsRepository
                .findByTenantIdAndProductId(download.getTenantId(), orderItem.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Digital product details not found"));
        
        // Update download status
        download.setDownloadStatus("IN_PROGRESS");
        download.setUpdated(LocalDateTime.now());
        digitalDownloadRepository.save(download);
        
        // Return file resource
        try {
            Path filePath = Paths.get(digitalDetails.getFileUrl());
            Resource resource;
            
            if (filePath.isAbsolute()) {
                resource = new FileSystemResource(filePath);
            } else {
                // Assume relative path from base download directory
                Path fullPath = Paths.get(downloadBasePath).resolve(filePath);
                resource = new FileSystemResource(fullPath);
            }
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new DownloadException("File not found or not readable: " + digitalDetails.getFileUrl());
            }
        } catch (Exception e) {
            throw new DownloadException("Error accessing file: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public void recordDownloadCompletion(String downloadToken, Long fileSizeDownloaded) {
        DigitalDownload download = digitalDownloadRepository.findByDownloadToken(downloadToken)
                .orElseThrow(() -> new DownloadException("Invalid download token"));
        
        download.setDownloadStatus("COMPLETED");
        download.setFileSizeDownloaded(fileSizeDownloaded);
        download.setUpdated(LocalDateTime.now());
        
        digitalDownloadRepository.save(download);
        
        log.info("Download completed for token: {}, size: {} bytes", downloadToken, fileSizeDownloaded);
    }
    
    @Override
    public List<DownloadHistoryResponse> getDownloadHistory(Integer tenantId, Long orderId, Long orderItemId) {
        // FIXED: Use composite key
        List<DigitalDownload> downloads = digitalDownloadRepository.findByTenantIdAndOrderIdAndOrderItemId(tenantId, orderId, orderItemId);
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
    public List<DownloadHistoryResponse> getProductDownloadHistory(Integer tenantId, Long productId) {
        List<DigitalDownload> downloads = digitalDownloadRepository.findByTenantIdAndProductId(tenantId, productId);
        return downloads.stream()
                .map(this::mapToDownloadHistoryResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public int getRemainingDownloads(Integer tenantId, Long orderId, Long orderItemId) {
        // FIXED: Use composite key to get digital details
        OrderItem orderItem = findOrderItemWithValidation(tenantId, orderId, orderItemId);
        DigitalProductDetails digitalDetails = digitalProductDetailsRepository
                .findByTenantIdAndProductId(tenantId, orderItem.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Digital product details not found"));
        
        if (digitalDetails.getDownloadLimit() == null) {
            return -1; // Unlimited downloads
        }
        
        // FIXED: Use composite key for counting
        long completedDownloads = digitalDownloadRepository.countByTenantIdAndOrderIdAndOrderItemIdAndDownloadStatus(
                tenantId, orderId, orderItemId, "COMPLETED");
        return Math.max(0, digitalDetails.getDownloadLimit() - (int) completedDownloads);
    }
    
    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        List<DigitalDownload> expiredTokens = digitalDownloadRepository.findByTokenExpiryBefore(LocalDateTime.now());
        
        for (DigitalDownload download : expiredTokens) {
            if ("INITIATED".equals(download.getDownloadStatus()) || "IN_PROGRESS".equals(download.getDownloadStatus())) {
                download.setDownloadStatus("EXPIRED");
                download.setStatus("-1"); // Mark as inactive
                download.setUpdated(LocalDateTime.now());
            }
        }
        
        if (!expiredTokens.isEmpty()) {
            digitalDownloadRepository.saveAll(expiredTokens);
            log.info("Cleaned up {} expired download tokens", expiredTokens.size());
        }
    }
    
    @Override
    @Transactional
    public DigitalProductDetailsResponse createDigitalProductDetails(Integer tenantId, String username, DigitalProductDetailsRequest request) {
        // Verify product exists
        productRepository.findByTenantIdAndProductId(tenantId, request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
        
        // Check if digital details already exist
        if (digitalProductDetailsRepository.existsByTenantIdAndProductId(tenantId, request.getProductId())) {
            throw new DownloadException("Digital product details already exist for product: " + request.getProductId());
        }
        
        DigitalProductDetails digitalDetails = new DigitalProductDetails();
        digitalDetails.setProductId(request.getProductId());
        digitalDetails.setTenantId(tenantId);
        digitalDetails.setFileUrl(request.getFileUrl());
        digitalDetails.setFileSize(request.getFileSize());
        digitalDetails.setFileFormat(request.getFileFormat());
        digitalDetails.setLicenseInfo(request.getLicenseInfo());
        digitalDetails.setVersion(request.getVersion());
        digitalDetails.setDownloadLimit(request.getDownloadLimit());
        digitalDetails.setExpiryDays(request.getExpiryDays());
        digitalDetails.setFileHash(request.getFileHash());
        digitalDetails.setStatus("0");
        
        String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
        digitalDetails.setCreatedBy(truncatedUsername);
        digitalDetails.setUpdatedBy(truncatedUsername);
        
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
        if (request.getDownloadLimit() != null) digitalDetails.setDownloadLimit(request.getDownloadLimit());
        if (request.getExpiryDays() != null) digitalDetails.setExpiryDays(request.getExpiryDays());
        if (request.getFileHash() != null) digitalDetails.setFileHash(request.getFileHash());
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
    
    // FIXED: Helper methods with composite key support
    private OrderItem findOrderItemWithValidation(Integer tenantId, Long orderId, Long orderItemId) {
        return orderRepository.findOrderItemByTenantIdAndOrderIdAndOrderItemId(tenantId, orderId, orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found: " + orderItemId));
    }
    
    private void validateDownloadLimits(Integer tenantId, Long orderId, Long orderItemId, DigitalProductDetails digitalDetails) {
        if (digitalDetails.getDownloadLimit() != null) {
            // FIXED: Use composite key for counting
            long completedDownloads = digitalDownloadRepository.countByTenantIdAndOrderIdAndOrderItemIdAndDownloadStatus(
                    tenantId, orderId, orderItemId, "COMPLETED");
            if (completedDownloads >= digitalDetails.getDownloadLimit()) {
                throw new DownloadLimitExceededException("Download limit exceeded for this product");
            }
        }
    }
    
    private void validateDownloadExpiry(OrderItem orderItem, DigitalProductDetails digitalDetails) {
        if (digitalDetails.getExpiryDays() != null) {
            LocalDateTime expiryDate = orderItem.getCreated().plusDays(digitalDetails.getExpiryDays());
            if (LocalDateTime.now().isAfter(expiryDate)) {
                throw new DownloadExpiredException("Download period has expired");
            }
        }
    }
    
    private DownloadHistoryResponse mapToDownloadHistoryResponse(DigitalDownload download) {
        return new DownloadHistoryResponse(
                download.getDownloadId(),
                download.getOrderItemId(),
                download.getOrderItem() != null ? download.getOrderItem().getProductId() : null,
                download.getDownloadDate(),
                download.getIpAddress(),
                download.getDownloadStatus(),
                download.getFileSizeDownloaded(),
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
                details.getDownloadLimit(),
                details.getExpiryDays(),
                details.getFileHash(),
                details.getStatus(),
                details.getCreated(),
                details.getUpdated()
        );
    }
}