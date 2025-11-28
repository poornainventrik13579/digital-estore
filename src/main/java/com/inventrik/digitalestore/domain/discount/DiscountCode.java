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
@Table(name = "discount_codes")
@IdClass(DiscountCode.DiscountCodePK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountCode {
    
    @Id
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Id
    @Column(name = "discount_id")
    private Long discountId;
    
    @Column(name = "code", nullable = false, length = 50)
    private String code;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType = DiscountType.PERCENTAGE;
    
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;
    
    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount = BigDecimal.ZERO;
    
    @Column(name = "max_uses")
    private Integer maxUses = 0;
    
    @Column(name = "used_count")
    private Integer usedCount = 0;
    
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;
    
    @Column(name = "valid_to")
    private LocalDateTime validTo;
    
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
    
    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        updated = LocalDateTime.now();
        if (validFrom == null) {
            validFrom = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return "0".equals(status);
    }
    
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive() && 
               (validFrom == null || !now.isBefore(validFrom)) &&
               (validTo == null || !now.isAfter(validTo));
    }
    
    public boolean hasUsesRemaining() {
        return maxUses == 0 || usedCount < maxUses;
    }
    
    public boolean canBeUsed() {
        return isValid() && hasUsesRemaining();
    }
    
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (!canBeUsed() || orderAmount.compareTo(minOrderAmount) < 0) {
            return BigDecimal.ZERO;
        }
        
        if (discountType == DiscountType.PERCENTAGE) {
            return orderAmount.multiply(discountValue).divide(new BigDecimal(100));
        } else {
            return discountValue;
        }
    }
    
    public static class DiscountCodePK implements Serializable {
        private Integer tenantId;
        private Long discountId;
        
        public DiscountCodePK() {}
        
        public DiscountCodePK(Integer tenantId, Long discountId) {
            this.tenantId = tenantId;
            this.discountId = discountId;
        }
        
        public Integer getTenantId() { return tenantId; }
        public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }
        public Long getDiscountId() { return discountId; }
        public void setDiscountId(Long discountId) { this.discountId = discountId; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DiscountCodePK)) return false;
            DiscountCodePK that = (DiscountCodePK) o;
            return Objects.equals(tenantId, that.tenantId) && Objects.equals(discountId, that.discountId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(tenantId, discountId);
        }
    }
} 