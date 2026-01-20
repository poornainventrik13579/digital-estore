// src/main/java/com/inventrik/digitalestore/repository/PaymentAuditRepository.java
package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.audit.PaymentAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for accessing payment audit logs.
 */
@Repository
public interface PaymentAuditRepository extends JpaRepository<PaymentAudit, String> {
    
    /**
     * Find audit logs for a specific payment.
     *
     * @param paymentId The payment ID
     * @return List of audit logs
     */
    List<PaymentAudit> findByPaymentIdOrderByTimestampDesc(String paymentId);
    
    /**
     * Find audit logs by event type.
     *
     * @param eventType The event type
     * @return List of audit logs
     */
    List<PaymentAudit> findByEventTypeOrderByTimestampDesc(String eventType);
    
    /**
     * Find audit logs performed by a specific user.
     *
     * @param performedBy The username
     * @return List of audit logs
     */
    List<PaymentAudit> findByPerformedByOrderByTimestampDesc(String performedBy);
    
    /**
     * Find audit logs within a time range.
     *
     * @param startTime The start time
     * @param endTime The end time
     * @return List of audit logs
     */
    List<PaymentAudit> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startTime, LocalDateTime endTime);
}