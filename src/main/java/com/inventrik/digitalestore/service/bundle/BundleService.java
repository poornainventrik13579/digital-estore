package com.inventrik.digitalestore.service.bundle;

import com.inventrik.digitalestore.dto.request.BundleRequest;
import com.inventrik.digitalestore.dto.response.BundleResponse;

import java.math.BigDecimal;
import java.util.List;

public interface BundleService {

    // Get all bundles for a tenant with optional filters (status, name, productId)
    List<BundleResponse> getAllBundles(Integer tenantId, String status, String name, Long productId);

    // Get a single bundle by ID
    BundleResponse getBundle(Integer tenantId, Long bundleId);

    // Create a new bundle
    BundleResponse createBundle(Integer tenantId, BundleRequest bundleRequest, String username);

    // Update an existing bundle
    BundleResponse updateBundle(Integer tenantId, Long bundleId, BundleRequest bundleRequest, String username);

    // Delete/deactivate a bundle
    void deleteBundle(Integer tenantId, Long bundleId, String username);

    // Calculate bundle pricing
    BigDecimal calculateBundlePrice(Integer tenantId, List<BundleRequest.BundleItemRequest> bundleItems);

    // Validate bundle composition
    boolean validateBundleComposition(Integer tenantId, List<BundleRequest.BundleItemRequest> bundleItems);

    // Add product to existing bundle
    BundleResponse addProductToBundle(Integer tenantId, Long bundleId, Long productId, Integer quantity, String username);

    // Remove product from bundle
    BundleResponse removeProductFromBundle(Integer tenantId, Long bundleId, Long productId, String username);

    // Update product quantity in bundle
    BundleResponse updateProductQuantityInBundle(Integer tenantId, Long bundleId, Long productId, Integer quantity, String username);

    // Get bundle statistics
    Long getBundleCount(Integer tenantId);
} 