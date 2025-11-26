package com.inventrik.digitalestore.service.product;

import com.inventrik.digitalestore.domain.product.Product;
import com.inventrik.digitalestore.dto.request.ProductRequest;
import com.inventrik.digitalestore.dto.request.ProductUpdateRequest;
import com.inventrik.digitalestore.dto.response.PagedResponse;
import com.inventrik.digitalestore.dto.response.ProductResponse;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.CategoryRepository;
import com.inventrik.digitalestore.repository.ProductRepository;
import com.inventrik.digitalestore.repository.TenantRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import com.inventrik.digitalestore.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TenantRepository tenantRepository;
    private final IdGeneratorService idGeneratorService;
    private final UserService userService;
    
    // Utility method to convert Entity to DTO
    private ProductResponse mapToDTO(Product product) {
        return new ProductResponse(
            product.getProductId(),
            product.getTenantId(),
            product.getProductName(),
            product.getDescription(),
            product.getDefaultPrice(),
            product.getDefaultCurrency(),
            product.getImage1Url(),
            product.getImage2Url(),
            product.getImage3Url(),
            product.getImage4Url(),
            product.getImage5Url(),
            product.getBanner(),
            product.getThumbnail(),
            product.getMetadata(),
            product.getCategoryId(),
            product.getStatus(),
            product.getCreated(),
            product.getUpdated()
        );
    }
    
    @Override
    @Cacheable(value = "products", key = "#tenantId + ':all'")
    public List<ProductResponse> getAllProducts(Integer tenantId) {
        return productRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "products", key = "#tenantId + ':page:' + #page + ':' + #size")
    public PagedResponse<ProductResponse> getAllProductsPaginated(Integer tenantId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("created").descending());
        Page<Product> productPage = productRepository.findByTenantId(tenantId, pageable);
        
        List<ProductResponse> products = productPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
                
        return PagedResponse.of(products, page, size, productPage.getTotalElements());
    }
    
    @Override
    @Cacheable(value = "products", key = "#tenantId + ':product:' + #productId")
    public ProductResponse getProduct(Integer tenantId, Long productId) {
        Product product = productRepository.findByTenantIdAndProductId(tenantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return mapToDTO(product);
    }
    
    @Override
    @Transactional
    public ProductResponse createProduct(Integer tenantId, String username, ProductRequest productRequest) {
        tenantRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

        Long newProductId = idGeneratorService.generateId(tenantId, "PRODUCT");

        Product product = new Product();
        product.setTenantId(tenantId);
        product.setProductId(newProductId);
        product.setProductName(productRequest.getProductName());
        product.setDescription(productRequest.getDescription());
        product.setDefaultPrice(productRequest.getDefaultPrice());
        product.setDefaultCurrency(productRequest.getDefaultCurrency());
        product.setImage1Url(productRequest.getImage1Url());
        product.setImage2Url(productRequest.getImage2Url());
        product.setImage3Url(productRequest.getImage3Url());
        product.setImage4Url(productRequest.getImage4Url());
        product.setImage5Url(productRequest.getImage5Url());
        product.setBanner(productRequest.getBanner());
        product.setThumbnail(productRequest.getThumbnail());
        product.setMetadata(productRequest.getMetadata());
        
        if (productRequest.getCategoryId() != null) {
            categoryRepository.findByTenantIdAndCategoryId(tenantId, productRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productRequest.getCategoryId()));

            product.setCategoryId(productRequest.getCategoryId());
        }
        
        product.setStatus("0"); // Active status
        product.setCreatedBy(userService.getAuditCode(username));
        product.setUpdatedBy(userService.getAuditCode(username));
        product.setCreated(LocalDateTime.now());
        product.setUpdated(LocalDateTime.now());
        
        Product savedProduct = productRepository.save(product);
        
        return mapToDTO(savedProduct);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
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
        if (updateRequest.getImage1Url() != null) {
            product.setImage1Url(updateRequest.getImage1Url());
        }
        if (updateRequest.getImage2Url() != null) {
            product.setImage2Url(updateRequest.getImage2Url());
        }
        if (updateRequest.getImage3Url() != null) {
            product.setImage3Url(updateRequest.getImage3Url());
        }
        if (updateRequest.getImage4Url() != null) {
            product.setImage4Url(updateRequest.getImage4Url());
        }
        if (updateRequest.getImage5Url() != null) {
            product.setImage5Url(updateRequest.getImage5Url());
        }
        if (updateRequest.getBanner() != null) {
            product.setBanner(updateRequest.getBanner());
        }
        if (updateRequest.getThumbnail() != null) {
            product.setThumbnail(updateRequest.getThumbnail());
        }
        if (updateRequest.getMetadata() != null) {
            product.setMetadata(updateRequest.getMetadata());
        }
        if (updateRequest.getCategoryId() != null) {
            // First verify the category exists
            categoryRepository.findByTenantIdAndCategoryId(tenantId, updateRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + updateRequest.getCategoryId()));
            
            // Set the category ID directly
            product.setCategoryId(updateRequest.getCategoryId());
        }
        if (updateRequest.getStatus() != null) {
            product.setStatus(updateRequest.getStatus());
        }
        
        product.setUpdatedBy(username.length() > 2 ? username.substring(0, 2) : username);
        product.setUpdated(LocalDateTime.now());
        
        Product updatedProduct = productRepository.save(product);
        
        return mapToDTO(updatedProduct);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Integer tenantId, Long productId) {
        if (!productRepository.findByTenantIdAndProductId(tenantId, productId).isPresent()) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
        
        productRepository.deleteByTenantIdAndProductId(tenantId, productId);
    }
    
    @Override
    @Cacheable(value = "products", key = "#tenantId + ':category:' + #categoryId")
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
    
    @Override
    @Cacheable(value = "products", key = "#tenantId + ':search:' + #keyword + ':' + #page + ':' + #size")
    public PagedResponse<ProductResponse> searchProducts(Integer tenantId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("created").descending());
        Page<Product> productPage = productRepository.searchByKeyword(tenantId, keyword, pageable);
        
        List<ProductResponse> products = productPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
                
        return PagedResponse.of(products, page, size, productPage.getTotalElements());
    }
}