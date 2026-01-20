package com.inventrik.digitalestore.service.product;

import com.inventrik.digitalestore.domain.product.Product;
import com.inventrik.digitalestore.domain.product.ProductPrice;
import com.inventrik.digitalestore.repository.ProductPriceRepository;
import com.inventrik.digitalestore.repository.ProductRepository;
import com.inventrik.digitalestore.service.currency.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductPriceServiceImpl implements ProductPriceService {
    
    private final ProductPriceRepository productPriceRepository;
    private final ProductRepository productRepository;
    private final CurrencyService currencyService;
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductPrice> getProductPrices(Integer tenantId, String productId) {
        return productPriceRepository.findByTenantIdAndProductIdAndStatus(tenantId, productId, "0");
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPrice getProductPrice(Integer tenantId, String productId, String currencyCode) {
        return productPriceRepository.findByTenantIdAndProductIdAndCurrencyCodeAndStatus(tenantId, productId, currencyCode, "0")
                .orElseThrow(() -> new RuntimeException("Product price not found"));
    }

    @Override
    public ProductPrice createProductPrice(Integer tenantId, String productId, String currencyCode, BigDecimal price) {
        ProductPrice productPrice = new ProductPrice();
        productPrice.setTenantId(tenantId);
        productPrice.setProductId(productId);
        productPrice.setCurrencyCode(currencyCode);
        productPrice.setPrice(price);
        productPrice.setStatus("0");
        productPrice.setCreatedBy("1");
        productPrice.setUpdatedBy("1");
        
        return productPriceRepository.save(productPrice);
    }
    
    @Override
    public ProductPrice updateProductPrice(Integer tenantId, String productId, String currencyCode, BigDecimal price) {
        ProductPrice productPrice = getProductPrice(tenantId, productId, currencyCode);
        productPrice.setPrice(price);
        productPrice.setUpdatedBy("1");

        return productPriceRepository.save(productPrice);
    }

    @Override
    public void deleteProductPrice(Integer tenantId, String productId, String currencyCode) {
        ProductPrice productPrice = getProductPrice(tenantId, productId, currencyCode);
        productPrice.setStatus("-1");
        productPrice.setUpdatedBy("1");
        productPriceRepository.save(productPrice);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getProductPriceInCurrency(Integer tenantId, String productId, String currencyCode) {
        return productPriceRepository.findByTenantIdAndProductIdAndCurrencyCodeAndStatus(tenantId, productId, currencyCode, "0")
                .map(ProductPrice::getPrice)
                .orElseGet(() -> {
                    Product product = productRepository.findByTenantIdAndProductIdAndStatus(tenantId, productId, "0")
                            .orElseThrow(() -> new RuntimeException("Product not found"));
                    return currencyService.convertAmount(product.getDefaultPrice(), product.getDefaultCurrency(), currencyCode, tenantId);
                });
    }
} 