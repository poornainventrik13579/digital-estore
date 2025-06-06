package com.inventrik.digitalestore.domain.currency;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyId implements Serializable {
    private Integer tenantId;
    private String currencyCode;
} 