package com.inventrik.digitalestore.service.product;

import com.inventrik.digitalestore.dto.request.ProductRequest;
import com.inventrik.digitalestore.dto.request.ProductUpdateRequest;
import com.inventrik.digitalestore.dto.response.PagedResponse;
import com.inventrik.digitalestore.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    
    List<ProductResponse> getAllProducts(Integer tenantId);
    
    PagedResponse<ProductResponse> getAllProductsPaginated(Integer tenantId, int page, int size);
    
    ProductResponse getProduct(Integer tenantId, Long productId);
    
    ProductResponse createProduct(Integer tenantId, String username, ProductRequest productRequest);
    
    ProductResponse updateProduct(Integer tenantId, Long productId, String username, ProductUpdateRequest updateRequest);
    
    void deleteProduct(Integer tenantId, Long productId);
    
    List<ProductResponse> getProductsByCategory(Integer tenantId, Long categoryId);
    
    List<ProductResponse> getActiveProducts(Integer tenantId);
    
    PagedResponse<ProductResponse> searchProducts(Integer tenantId, String keyword, int page, int size);
}
