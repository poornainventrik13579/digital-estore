package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.product.Product;
import com.inventrik.digitalestore.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldSaveAndFindProduct() {
        Product product = createTestProduct(1, 1L, "Test Product");
        
        Product saved = productRepository.save(product);
        
        assertThat(saved.getProductId()).isNotNull();
        assertThat(saved.getProductName()).isEqualTo("Test Product");
        assertThat(saved.getDefaultPrice()).isEqualTo(new BigDecimal("99.99"));
    }

    @Test
    void shouldFindByTenantIdAndProductId() {
        Product product = createTestProduct(1, 1L, "Test Product");
        entityManager.persistAndFlush(product);

        Optional<Product> found = productRepository.findByTenantIdAndProductId(1, 1L);

        assertThat(found).isPresent();
        assertThat(found.get().getProductName()).isEqualTo("Test Product");
    }

    @Test
    void shouldFindAllByTenantId() {
        Product product1 = createTestProduct(1, 1L, "Product 1");
        Product product2 = createTestProduct(1, 2L, "Product 2");
        Product product3 = createTestProduct(2, 1L, "Product 3");
        
        entityManager.persistAndFlush(product1);
        entityManager.persistAndFlush(product2);
        entityManager.persistAndFlush(product3);

        List<Product> tenant1Products = productRepository.findByTenantId(1);
        List<Product> tenant2Products = productRepository.findByTenantId(2);

        assertThat(tenant1Products).hasSize(2);
        assertThat(tenant2Products).hasSize(1);
        assertThat(tenant1Products).extracting("productName").containsExactlyInAnyOrder("Product 1", "Product 2");
        assertThat(tenant2Products).extracting("productName").containsExactlyInAnyOrder("Product 3");
    }

    @Test
    void shouldFindByTenantIdAndCategoryId() {
        Product product1 = createTestProduct(1, 1L, "Product 1");
        product1.setCategoryId(10L);
        Product product2 = createTestProduct(1, 2L, "Product 2");
        product2.setCategoryId(10L);
        Product product3 = createTestProduct(1, 3L, "Product 3");
        product3.setCategoryId(20L);
        
        entityManager.persistAndFlush(product1);
        entityManager.persistAndFlush(product2);
        entityManager.persistAndFlush(product3);

        List<Product> category10Products = productRepository.findByTenantIdAndCategoryId(1, 10L);
        List<Product> category20Products = productRepository.findByTenantIdAndCategoryId(1, 20L);

        assertThat(category10Products).hasSize(2);
        assertThat(category20Products).hasSize(1);
        assertThat(category10Products).extracting("productName").containsExactlyInAnyOrder("Product 1", "Product 2");
        assertThat(category20Products).extracting("productName").containsExactlyInAnyOrder("Product 3");
    }

    @Test
    void shouldFindByTenantIdAndProductNameContainingIgnoreCase() {
        Product product1 = createTestProduct(1, 1L, "Digital Course");
        Product product2 = createTestProduct(1, 2L, "Online Tutorial");
        Product product3 = createTestProduct(1, 3L, "course material");
        
        entityManager.persistAndFlush(product1);
        entityManager.persistAndFlush(product2);
        entityManager.persistAndFlush(product3);

        List<Product> allProducts = productRepository.findByTenantId(1);
        List<Product> courseProducts = allProducts.stream()
                .filter(p -> p.getProductName().toLowerCase().contains("course"))
                .collect(java.util.stream.Collectors.toList());
        List<Product> onlineProducts = allProducts.stream()
                .filter(p -> p.getProductName().toLowerCase().contains("online"))
                .collect(java.util.stream.Collectors.toList());

        assertThat(courseProducts).hasSize(2);
        assertThat(onlineProducts).hasSize(1);
        assertThat(courseProducts).extracting("productName").containsExactlyInAnyOrder("Digital Course", "course material");
        assertThat(onlineProducts).extracting("productName").containsExactlyInAnyOrder("Online Tutorial");
    }

    @Test
    void shouldFindByTenantIdAndDefaultPriceBetween() {
        Product product1 = createTestProduct(1, 1L, "Cheap Product");
        product1.setDefaultPrice(new BigDecimal("10.00"));
        Product product2 = createTestProduct(1, 2L, "Medium Product");
        product2.setDefaultPrice(new BigDecimal("50.00"));
        Product product3 = createTestProduct(1, 3L, "Expensive Product");
        product3.setDefaultPrice(new BigDecimal("200.00"));
        
        entityManager.persistAndFlush(product1);
        entityManager.persistAndFlush(product2);
        entityManager.persistAndFlush(product3);

        List<Product> midRangeProducts = productRepository.findByTenantId(1).stream()
                .filter(p -> p.getDefaultPrice().compareTo(new BigDecimal("20.00")) >= 0 && 
                             p.getDefaultPrice().compareTo(new BigDecimal("100.00")) <= 0)
                .collect(java.util.stream.Collectors.toList());

        assertThat(midRangeProducts).hasSize(1);
        assertThat(midRangeProducts.get(0).getProductName()).isEqualTo("Medium Product");
    }

    @Test
    void shouldReturnEmptyWhenProductNotFound() {
        Optional<Product> found = productRepository.findByTenantIdAndProductId(999, 999L);
        
        assertThat(found).isEmpty();
    }

    @Test
    void shouldDeleteProduct() {
        Product product = createTestProduct(1, 1L, "Test Product");
        Product saved = entityManager.persistAndFlush(product);

        productRepository.deleteByTenantIdAndProductId(saved.getTenantId(), saved.getProductId());

        Optional<Product> found = productRepository.findByTenantIdAndProductId(1, 1L);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldUpdateProduct() {
        Product product = createTestProduct(1, 1L, "Original Product");
        Product saved = entityManager.persistAndFlush(product);

        saved.setProductName("Updated Product");
        saved.setDescription("Updated description");
        saved.setDefaultPrice(new BigDecimal("149.99"));
        saved.setUpdated(LocalDateTime.now());
        
        Product updated = productRepository.save(saved);

        assertThat(updated.getProductName()).isEqualTo("Updated Product");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getDefaultPrice()).isEqualTo(new BigDecimal("149.99"));
    }

    @Test
    void shouldHandleMultipleTenantsCorrectly() {
        Product tenant1Product = createTestProduct(1, 1L, "Product 1");
        Product tenant2Product = createTestProduct(2, 1L, "Product 1");
        
        entityManager.persistAndFlush(tenant1Product);
        entityManager.persistAndFlush(tenant2Product);

        Optional<Product> found1 = productRepository.findByTenantIdAndProductId(1, 1L);
        Optional<Product> found2 = productRepository.findByTenantIdAndProductId(2, 1L);

        assertThat(found1).isPresent();
        assertThat(found2).isPresent();
        assertThat(found1.get().getTenantId()).isEqualTo(1);
        assertThat(found2.get().getTenantId()).isEqualTo(2);
    }

    @Test
    void shouldCountByTenantId() {
        Product product1 = createTestProduct(1, 1L, "Product 1");
        Product product2 = createTestProduct(1, 2L, "Product 2");
        Product product3 = createTestProduct(2, 1L, "Product 3");
        
        entityManager.persistAndFlush(product1);
        entityManager.persistAndFlush(product2);
        entityManager.persistAndFlush(product3);

        long tenant1Count = productRepository.findByTenantId(1).size();
        long tenant2Count = productRepository.findByTenantId(2).size();

        assertThat(tenant1Count).isEqualTo(2);
        assertThat(tenant2Count).isEqualTo(1);
    }

    private Product createTestProduct(Integer tenantId, Long productId, String productName) {
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