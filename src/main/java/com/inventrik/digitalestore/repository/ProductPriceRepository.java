package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.product.ProductPrice;
import com.inventrik.digitalestore.domain.product.ProductPriceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductPriceRepository extends JpaRepository<ProductPrice, ProductPriceId> {
    
    List<ProductPrice> findByTenantIdAndProductIdAndStatus(Integer tenantId, String productId, String status);

    Optional<ProductPrice> findByTenantIdAndProductIdAndCurrencyCodeAndStatus(Integer tenantId, String productId, String currencyCode, String status);
    
    List<ProductPrice> findByTenantIdAndStatus(Integer tenantId, String status);
} 