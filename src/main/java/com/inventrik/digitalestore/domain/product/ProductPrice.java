package com.inventrik.digitalestore.domain.product;

import com.inventrik.digitalestore.domain.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "productprices")
@IdClass(ProductPriceId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductPrice extends AuditableEntity {
    
    @Id
    @Column(name = "tenant_id")
    private Integer tenantId;
    
    @Id
    @Column(name = "product_id")
    private Long productId;
    
    @Id
    @Column(name = "currency_code", length = 3)
    private String currencyCode;
    
    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;
    
    @Column(name = "status", length = 2, nullable = false)
    private String status;
} 