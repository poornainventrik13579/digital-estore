package com.inventrik.digitalestore.service.bundle;

import com.inventrik.digitalestore.dto.request.BundleRequest;
import com.inventrik.digitalestore.dto.response.BundleResponse;

import java.math.BigDecimal;
import java.util.List;

public interface BundleService {
    
    List<BundleResponse> getAllBundles(Integer tenantId);
    
    List<BundleResponse> getActiveBundles(Integer tenantId);
    
    BundleResponse getBundle(Integer tenantId, Long bundleId);
    
    BundleResponse createBundle(Integer tenantId, BundleRequest bundleRequest, String username);
    
    BundleResponse updateBundle(Integer tenantId, Long bundleId, BundleRequest bundleRequest, String username);
    
    void deleteBundle(Integer tenantId, Long bundleId, String username);
    
    List<BundleResponse> searchBundles(Integer tenantId, String name);
    
    BigDecimal calculateBundlePrice(Integer tenantId, List<BundleRequest.BundleItemRequest> bundleItems);
    
    boolean validateBundleComposition(Integer tenantId, List<BundleRequest.BundleItemRequest> bundleItems);
    
    List<BundleResponse> getBundlesContainingProduct(Integer tenantId, Long productId);
    
    BundleResponse addProductToBundle(Integer tenantId, Long bundleId, Long productId, Integer quantity, String username);
    
    BundleResponse removeProductFromBundle(Integer tenantId, Long bundleId, Long productId, String username);
    
    BundleResponse updateProductQuantityInBundle(Integer tenantId, Long bundleId, Long productId, Integer quantity, String username);
    
    Long getBundleCount(Integer tenantId);
} 