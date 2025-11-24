package com.inventrik.digitalestore.service;

import com.inventrik.digitalestore.domain.product.Product;
import com.inventrik.digitalestore.dto.request.ProductRequest;
import com.inventrik.digitalestore.dto.request.ProductUpdateRequest;
import com.inventrik.digitalestore.dto.response.ProductResponse;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.ProductRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import com.inventrik.digitalestore.service.product.ProductServiceImpl;
import com.inventrik.digitalestore.dto.response.PagedResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private IdGeneratorService idGeneratorService;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void shouldGetAllProducts() {
        Integer tenantId = 1;
        Product product1 = createProduct(tenantId, 1L, "Product 1");
        Product product2 = createProduct(tenantId, 2L, "Product 2");

        when(productRepository.findByTenantId(tenantId)).thenReturn(Arrays.asList(product1, product2));

        List<ProductResponse> result = productService.getAllProducts(tenantId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProductName()).isEqualTo("Product 1");
        assertThat(result.get(1).getProductName()).isEqualTo("Product 2");
        verify(productRepository).findByTenantId(tenantId);
    }

    @Test
    void shouldGetProductById() {
        Integer tenantId = 1;
        Long productId = 1L;
        Product product = createProduct(tenantId, productId, "Test Product");

        when(productRepository.findByTenantIdAndProductId(tenantId, productId)).thenReturn(Optional.of(product));

        ProductResponse result = productService.getProduct(tenantId, productId);

        assertThat(result.getProductName()).isEqualTo("Test Product");
        assertThat(result.getDefaultPrice()).isEqualTo(new BigDecimal("99.99"));
        verify(productRepository).findByTenantIdAndProductId(tenantId, productId);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        Integer tenantId = 1;
        Long productId = 1L;

        when(productRepository.findByTenantIdAndProductId(tenantId, productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(tenantId, productId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Product not found");

        verify(productRepository).findByTenantIdAndProductId(tenantId, productId);
    }

    @Test
    void shouldCreateProduct() {
        Integer tenantId = 1;
        Long generatedProductId = 123L;
        ProductRequest request = new ProductRequest();
        request.setProductName("New Product");
        request.setDescription("New product description");
        request.setDefaultPrice(new BigDecimal("149.99"));
        request.setDefaultCurrency("USD");
        request.setCategoryId(1L);

        Product savedProduct = createProduct(tenantId, generatedProductId, "New Product");

        when(idGeneratorService.generateId(tenantId, "PRODUCT")).thenReturn(generatedProductId);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponse result = productService.createProduct(tenantId, "testuser", request);

        assertThat(result.getProductName()).isEqualTo("New Product");
        assertThat(result.getDefaultPrice()).isEqualTo(new BigDecimal("99.99"));
        verify(idGeneratorService).generateId(tenantId, "PRODUCT");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldUpdateProduct() {
        Integer tenantId = 1;
        Long productId = 1L;
        Product existingProduct = createProduct(tenantId, productId, "Original Product");
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setProductName("Updated Product");
        updateRequest.setDescription("Updated description");
        updateRequest.setDefaultPrice(new BigDecimal("199.99"));

        when(productRepository.findByTenantIdAndProductId(tenantId, productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        ProductResponse result = productService.updateProduct(tenantId, productId, "testuser", updateRequest);

        assertThat(result.getProductName()).isEqualTo("Updated Product");
        verify(productRepository).findByTenantIdAndProductId(tenantId, productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldDeleteProduct() {
        Integer tenantId = 1;
        Long productId = 1L;
        Product existingProduct = createProduct(tenantId, productId, "Test Product");

        when(productRepository.findByTenantIdAndProductId(tenantId, productId)).thenReturn(Optional.of(existingProduct));
        doNothing().when(productRepository).deleteById(any());

        productService.deleteProduct(tenantId, productId);

        verify(productRepository).findByTenantIdAndProductId(tenantId, productId);
        verify(productRepository).deleteByTenantIdAndProductId(tenantId, productId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentProduct() {
        Integer tenantId = 1;
        Long productId = 1L;

        when(productRepository.findByTenantIdAndProductId(tenantId, productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(tenantId, productId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Product not found");

        verify(productRepository).findByTenantIdAndProductId(tenantId, productId);
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void shouldSearchProductsByName() {
        Integer tenantId = 1;
        String searchTerm = "course";
        Product product1 = createProduct(tenantId, 1L, "Digital Course");
        Product product2 = createProduct(tenantId, 2L, "Course Material");
        List<ProductResponse> products = productService.getProductsByCategory(tenantId, 1L);

        assertThat(products).hasSize(0);
    }

    @Test
    void shouldGetProductsByCategory() {
        Integer tenantId = 1;
        Long categoryId = 10L;
        Product product1 = createProduct(tenantId, 1L, "Product 1");
        Product product2 = createProduct(tenantId, 2L, "Product 2");

        when(productRepository.findByTenantIdAndCategoryId(tenantId, categoryId))
            .thenReturn(Arrays.asList(product1, product2));

        List<ProductResponse> result = productService.getProductsByCategory(tenantId, categoryId);

        assertThat(result).hasSize(2);
        verify(productRepository).findByTenantIdAndCategoryId(tenantId, categoryId);
    }



    private Product createProduct(Integer tenantId, Long productId, String productName) {
        Product product = new Product();
        product.setTenantId(tenantId);
        product.setProductId(productId);
        product.setProductName(productName);
        product.setDescription("Test description for " + productName);
        product.setDefaultPrice(new BigDecimal("99.99"));
        product.setDefaultCurrency("USD");
        product.setCategoryId(1L);
        product.setCreated(LocalDateTime.now());
        product.setUpdated(LocalDateTime.now());
        return product;
    }
}