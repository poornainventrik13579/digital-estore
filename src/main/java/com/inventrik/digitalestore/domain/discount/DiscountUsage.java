package com.inventrik.digitalestore.domain.discount;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "discount_usage")
@IdClass(DiscountUsage.DiscountUsagePK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountUsage {
    
    @Id
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Id
    @Column(name = "usage_id")
    private Long usageId;
    
    @Column(name = "discount_id", nullable = false)
    private Long discountId;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;
    
    @Column(name = "used_date", nullable = false)
    private LocalDateTime usedDate;
    
    @Column(name = "status", nullable = false, length = 2)
    private String status = "0";
    
    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;
    
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
    
    @Column(name = "updated_by", nullable = false, length = 50)
    private String updatedBy;
    
    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;
    
    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        updated = LocalDateTime.now();
        if (usedDate == null) {
            usedDate = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return "0".equals(status);
    }
    
    public static class DiscountUsagePK implements Serializable {
        private Integer tenantId;
        private Long usageId;
        
        public DiscountUsagePK() {}
        
        public DiscountUsagePK(Integer tenantId, Long usageId) {
            this.tenantId = tenantId;
            this.usageId = usageId;
        }
        
        public Integer getTenantId() { return tenantId; }
        public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }
        public Long getUsageId() { return usageId; }
        public void setUsageId(Long usageId) { this.usageId = usageId; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DiscountUsagePK)) return false;
            DiscountUsagePK that = (DiscountUsagePK) o;
            return Objects.equals(tenantId, that.tenantId) && Objects.equals(usageId, that.usageId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(tenantId, usageId);
        }
    }
} 