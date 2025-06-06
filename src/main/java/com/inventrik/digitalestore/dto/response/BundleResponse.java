package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BundleResponse {
    
    private Long bundleId;
    private Integer tenantId;
    private String bundleName;
    private String description;
    private BigDecimal bundlePrice;
    private BigDecimal discountPercent;
    private String currency;
    private String status;
    private LocalDateTime created;
    private LocalDateTime updated;
    private List<BundleItemResponse> bundleItems;
    private BigDecimal totalOriginalPrice;
    private BigDecimal totalSavings;
    private Integer totalProducts;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BundleItemResponse {
        private Long bundleItemId;
        private Long productId;
        private String productName;
        private String productDescription;
        private BigDecimal productPrice;
        private String productCurrency;
        private Integer quantity;
        private BigDecimal itemTotal;
        private String status;
    }
} 