package com.inventrik.digitalestore.domain.product;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ProductPriceId implements Serializable {
    private Integer tenantId;
    private Long productId;
    private String currencyCode;
} 