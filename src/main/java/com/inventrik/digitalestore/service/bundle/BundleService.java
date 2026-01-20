package com.inventrik.digitalestore.service.bundle;

import com.inventrik.digitalestore.dto.request.BundleRequest;
import com.inventrik.digitalestore.dto.response.BundleResponse;

import java.math.BigDecimal;
import java.util.List;

public interface BundleService {

    List<BundleResponse> getAllBundles(Integer tenantId, String status, String name, String productId);

    BundleResponse getBundle(Integer tenantId, String bundleId);

    BundleResponse createBundle(Integer tenantId, BundleRequest bundleRequest, String username);

    BundleResponse updateBundle(Integer tenantId, String bundleId, BundleRequest bundleRequest, String username);

    void deleteBundle(Integer tenantId, String bundleId, String username);

    BigDecimal calculateBundlePrice(Integer tenantId, List<BundleRequest.BundleItemRequest> bundleItems);

    boolean validateBundleComposition(Integer tenantId, List<BundleRequest.BundleItemRequest> bundleItems);

    BundleResponse addProductToBundle(Integer tenantId, String bundleId, String productId, Integer quantity, String username);

    BundleResponse removeProductFromBundle(Integer tenantId, String bundleId, String productId, String username);

    BundleResponse updateProductQuantityInBundle(Integer tenantId, String bundleId, String productId, Integer quantity, String username);

    Integer getBundleCount(Integer tenantId);
} 