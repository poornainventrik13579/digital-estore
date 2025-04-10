package com.inventrik.digitalestore.service.product;

import com.inventrik.digitalestore.domain.product.Product;
import com.inventrik.digitalestore.dto.request.ProductRequest;
import com.inventrik.digitalestore.dto.request.ProductUpdateRequest;
import com.inventrik.digitalestore.dto.response.ProductResponse;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    
    // Utility method to convert Entity to DTO
    private ProductResponse mapToDTO(Product product) {
        return new ProductResponse(
            product.getProductId(),
            product.getTenantId(),
            product.getProductName(),
            product.getDescription(),
            product.getDefaultPrice(),
            product.getDefaultCurrency(),
            product.getCategoryId(),
            product.getStatus(),
            product.getCreated(),
            product.getUpdated()
        );
    }
    
    @Override
    public List<ProductResponse> getAllProducts(Integer tenantId) {
        return productRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public ProductResponse getProduct(Integer tenantId, Long productId) {
        Product product = productRepository.findByTenantIdAndProductId(tenantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return mapToDTO(product);
    }
    
    @Override
    @Transactional
    public ProductResponse createProduct(Integer tenantId, String username, ProductRequest productRequest) {
        // Generate a new product ID (in production, use a better ID generation strategy)
        Long newProductId = System.currentTimeMillis();
        
        Product product = new Product();
        product.setTenantId(tenantId);
        product.setProductId(newProductId);
        product.setProductName(productRequest.getProductName());
        product.setDescription(productRequest.getDescription());
        product.setDefaultPrice(productRequest.getDefaultPrice());
        product.setDefaultCurrency(productRequest.getDefaultCurrency());
        product.setCategoryId(productRequest.getCategoryId());
        product.setStatus("0"); // Active status
        product.setCreatedBy(username);
        product.setUpdatedBy(username);
        product.setCreated(LocalDateTime.now());
        product.setUpdated(LocalDateTime.now());
        
        Product savedProduct = productRepository.save(product);
        
        return mapToDTO(savedProduct);
    }
    
    @Override
    @Transactional
    public ProductResponse updateProduct(Integer tenantId, Long productId, String username, ProductUpdateRequest updateRequest) {
        Product product = productRepository.findByTenantIdAndProductId(tenantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        if (updateRequest.getProductName() != null) {
            product.setProductName(updateRequest.getProductName());
        }
        if (updateRequest.getDescription() != null) {
            product.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getDefaultPrice() != null) {
            product.setDefaultPrice(updateRequest.getDefaultPrice());
        }
        if (updateRequest.getDefaultCurrency() != null) {
            product.setDefaultCurrency(updateRequest.getDefaultCurrency());
        }
        if (updateRequest.getCategoryId() != null) {
            product.setCategoryId(updateRequest.getCategoryId());
        }
        if (updateRequest.getStatus() != null) {
            product.setStatus(updateRequest.getStatus());
        }
        
        product.setUpdatedBy(username);
        product.setUpdated(LocalDateTime.now());
        
        Product updatedProduct = productRepository.save(product);
        
        return mapToDTO(updatedProduct);
    }
    
    @Override
    @Transactional
    public void deleteProduct(Integer tenantId, Long productId) {
        // Check if product exists
        if (!productRepository.findByTenantIdAndProductId(tenantId, productId).isPresent()) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
        
        productRepository.deleteByTenantIdAndProductId(tenantId, productId);
    }
    
    @Override
    public List<ProductResponse> getProductsByCategory(Integer tenantId, Long categoryId) {
        return productRepository.findByTenantIdAndCategoryId(tenantId, categoryId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProductResponse> getActiveProducts(Integer tenantId) {
        return productRepository.findByTenantIdAndStatus(tenantId, "0").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}
