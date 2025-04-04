package com.inventrik.digitalestore.domain.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "default_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal defaultPrice;
    
    @Column(name = "default_currency", nullable = false, length = 3)
    private String defaultCurrency;
    
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "status", nullable = false, length = 2)
    private String status;
    
    @Column(name = "created_by", nullable = false, length = 2)
    private String createdBy;
    
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    
    @Column(name = "updated_by", nullable = false, length = 2)
    private String updatedBy;
    
    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;
    
    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        updated = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
}
