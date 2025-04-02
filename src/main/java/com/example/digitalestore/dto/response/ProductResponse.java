package com.example.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long productId;
    private Integer tenantId;
    private String productName;
    private String description;
    private BigDecimal defaultPrice;
    private String defaultCurrency;
    private Long categoryId;
    private String status;
    private LocalDateTime created;
    private LocalDateTime updated;
}