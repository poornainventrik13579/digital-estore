package com.inventrik.digitalestore.domain.audit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAudit {
    
    @Id
    @Column(name = "audit_id")
    private String auditId;
    
    @Column(name = "payment_id")
    private Long paymentId;
    
    @Column(name = "event_type", nullable = false)
    private String eventType;
    
    @Column(name = "event_details", columnDefinition = "TEXT")
    private String eventDetails;
    
    @Column(name = "performed_by", nullable = false)
    private String performedBy;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}