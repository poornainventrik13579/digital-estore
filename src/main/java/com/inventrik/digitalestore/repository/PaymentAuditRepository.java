
package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.audit.PaymentAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentAuditRepository extends JpaRepository<PaymentAudit, String> {
    
    List<PaymentAudit> findByPaymentIdOrderByTimestampDesc(Long paymentId);
    
    List<PaymentAudit> findByEventTypeOrderByTimestampDesc(String eventType);
    
    List<PaymentAudit> findByPerformedByOrderByTimestampDesc(String performedBy);
    
    List<PaymentAudit> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startTime, LocalDateTime endTime);
}