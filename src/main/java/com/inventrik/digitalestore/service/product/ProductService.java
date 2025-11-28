package com.inventrik.digitalestore.service.product;

import com.inventrik.digitalestore.dto.request.ProductRequest;
import com.inventrik.digitalestore.dto.request.ProductUpdateRequest;
import com.inventrik.digitalestore.dto.response.PagedResponse;
import com.inventrik.digitalestore.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    // Get all products for a tenant with optional filters (status, categoryId, keyword) and pagination
    PagedResponse<ProductResponse> getAllProductsPaginated(Integer tenantId, int page, int size, Long categoryId, String status, String keyword);

    // Get a single product by ID
    ProductResponse getProduct(Integer tenantId, Long productId);

    // Create a new product
    ProductResponse createProduct(Integer tenantId, String username, ProductRequest productRequest);

    // Update an existing product
    ProductResponse updateProduct(Integer tenantId, Long productId, String username, ProductUpdateRequest updateRequest);

    // Delete a product
    void deleteProduct(Integer tenantId, Long productId);
}
