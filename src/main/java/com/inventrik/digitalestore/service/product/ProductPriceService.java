package com.inventrik.digitalestore.service.product;

import com.inventrik.digitalestore.domain.product.ProductPrice;

import java.math.BigDecimal;
import java.util.List;

public interface ProductPriceService {

    List<ProductPrice> getProductPrices(Integer tenantId, String productId);

    ProductPrice getProductPrice(Integer tenantId, String productId, String currencyCode);

    ProductPrice createProductPrice(Integer tenantId, String productId, String currencyCode, BigDecimal price);

    ProductPrice updateProductPrice(Integer tenantId, String productId, String currencyCode, BigDecimal price);

    void deleteProductPrice(Integer tenantId, String productId, String currencyCode);

    BigDecimal getProductPriceInCurrency(Integer tenantId, String productId, String currencyCode);
} 