package com.inventrik.digitalestore.domain.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "payments")
@IdClass(Payment.PaymentPK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Id
    @Column(name = "payment_id")
    private Long paymentId;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "currency", length = 3)
    private String currency;
    
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "refunded_amount", precision = 10, scale = 2)
    private BigDecimal refundedAmount = BigDecimal.ZERO;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    @Column(name = "transaction_id", length = 100)
    private String transactionId;
    
    @Column(name = "status", length = 20)
    private String status = PaymentStatus.PENDING.getDisplayName();
    
    @Column(name = "refund_reason", columnDefinition = "TEXT")
    private String refundReason;
    
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
        paymentDate = LocalDateTime.now();
        if (refundedAmount == null) {
            refundedAmount = BigDecimal.ZERO;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
    
    public BigDecimal getRemainingAmount() {
        return amount.subtract(refundedAmount != null ? refundedAmount : BigDecimal.ZERO);
    }
    
    public boolean isFullyRefunded() {
        return refundedAmount != null && refundedAmount.compareTo(amount) >= 0;
    }
    
    public boolean isPartiallyRefunded() {
        return refundedAmount != null && refundedAmount.compareTo(BigDecimal.ZERO) > 0 && !isFullyRefunded();
    }
    
    public static class PaymentPK implements Serializable {
        private Integer tenantId;
        private Long paymentId;
        
        public PaymentPK() {}
        
        public PaymentPK(Integer tenantId, Long paymentId) {
            this.tenantId = tenantId;
            this.paymentId = paymentId;
        }
        
        public Integer getTenantId() { return tenantId; }
        public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }
        public Long getPaymentId() { return paymentId; }
        public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PaymentPK)) return false;
            PaymentPK paymentPK = (PaymentPK) o;
            return Objects.equals(tenantId, paymentPK.tenantId) && Objects.equals(paymentId, paymentPK.paymentId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(tenantId, paymentId);
        }
    }
}