
package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.audit.PaymentAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentAuditRepository extends JpaRepository<PaymentAudit, String> {

    List<PaymentAudit> findByTenantIdAndPaymentIdOrderByTimestampDesc(Integer tenantId, Long paymentId);

    List<PaymentAudit> findByTenantIdAndEventTypeOrderByTimestampDesc(Integer tenantId, String eventType);

    List<PaymentAudit> findByTenantIdAndPerformedByOrderByTimestampDesc(Integer tenantId, String performedBy);

    List<PaymentAudit> findByTenantIdAndTimestampBetweenOrderByTimestampDesc(Integer tenantId, LocalDateTime startTime, LocalDateTime endTime);
}