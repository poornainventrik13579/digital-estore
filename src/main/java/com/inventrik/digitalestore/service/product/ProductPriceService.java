package com.inventrik.digitalestore.service.product;

import com.inventrik.digitalestore.domain.product.ProductPrice;

import java.math.BigDecimal;
import java.util.List;

public interface ProductPriceService {
    
    List<ProductPrice> getProductPrices(Integer tenantId, Long productId);
    
    ProductPrice getProductPrice(Integer tenantId, Long productId, String currencyCode);
    
    ProductPrice createProductPrice(Integer tenantId, Long productId, String currencyCode, BigDecimal price);
    
    ProductPrice updateProductPrice(Integer tenantId, Long productId, String currencyCode, BigDecimal price);
    
    void deleteProductPrice(Integer tenantId, Long productId, String currencyCode);
    
    BigDecimal getProductPriceInCurrency(Integer tenantId, Long productId, String currencyCode);
} 