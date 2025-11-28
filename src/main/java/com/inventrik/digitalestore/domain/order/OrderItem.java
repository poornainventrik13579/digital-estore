package com.inventrik.digitalestore.domain.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "order_items")
@IdClass(OrderItem.OrderItemPK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    
    @Id
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Id
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Id
    @Column(name = "order_item_id")
    private Long orderItemId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "price_at_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;
    
    @Column(name = "license_key", length = 100)
    private String licenseKey;
    
    @Column(name = "status", nullable = false, length = 2)
    private String status = "0";
    
    @Column(name = "created_by", nullable = false, length = 2)
    private String createdBy;
    
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    
    @Column(name = "updated_by", nullable = false, length = 50)
    private String updatedBy;
    
    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false),
        @JoinColumn(name = "order_id", referencedColumnName = "order_id", insertable = false, updatable = false)
    })
    private Order order;
    
    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        updated = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
    
    public static class OrderItemPK implements Serializable {
        private Integer tenantId;
        private Long orderId;
        private Long orderItemId;
        
        public OrderItemPK() {}
        
        public OrderItemPK(Integer tenantId, Long orderId, Long orderItemId) {
            this.tenantId = tenantId;
            this.orderId = orderId;
            this.orderItemId = orderItemId;
        }
        
        public Integer getTenantId() { return tenantId; }
        public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public Long getOrderItemId() { return orderItemId; }
        public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OrderItemPK)) return false;
            OrderItemPK that = (OrderItemPK) o;
            return Objects.equals(tenantId, that.tenantId) && 
                   Objects.equals(orderId, that.orderId) && 
                   Objects.equals(orderItemId, that.orderItemId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(tenantId, orderId, orderItemId);
        }
    }
}