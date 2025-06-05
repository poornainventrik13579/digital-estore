package com.inventrik.digitalestore.domain.order;

import com.inventrik.digitalestore.domain.product.Product;
import com.inventrik.digitalestore.domain.download.DigitalDownload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "OrderItems")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    
    @Id
    @Column(name = "order_item_id")
    private Long orderItemId;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @JoinColumns({
        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id"),
        @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    })
    private Order order;
    
    @JoinColumns({
        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id"),
        @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    })
    private Product product;
    
    @Column(name = "price_at_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;
    
    @Column(name = "license_key", length = 100)
    private String licenseKey;
    
    @Column(name = "status", nullable = false, length = 2)
    private String status = "0"; // Active status
    
    @Column(name = "created_by", nullable = false, length = 2)
    private String createdBy;
    
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    
    @Column(name = "updated_by", nullable = false, length = 2)
    private String updatedBy;
    
    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;
    
    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<DigitalDownload> downloads = new ArrayList<>();
    
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