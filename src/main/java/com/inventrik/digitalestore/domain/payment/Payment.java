package com.inventrik.digitalestore.domain.payment;

import com.inventrik.digitalestore.domain.order.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @Column(name = "payment_id")
    private Long paymentId;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false),
        @JoinColumn(name = "order_id", referencedColumnName = "order_id", insertable = false, updatable = false)
    })
    private Order order;
    
    @Column(name = "currency", length = 3)
    private String currency;
    
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    @Column(name = "transaction_id", length = 100)
    private String transactionId;
    
    @Column(name = "status", length = 20)
    private String status = PaymentStatus.PENDING.getDisplayName();
    
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
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
}