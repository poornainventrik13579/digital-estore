package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.payment.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByTenantIdAndPaymentId(Integer tenantId, Long paymentId);

    List<Payment> findByTenantIdAndOrderId(Integer tenantId, Long orderId);

    List<Payment> findByTenantId(Integer tenantId);
    Page<Payment> findByTenantId(Integer tenantId, Pageable pageable);

    List<Payment> findByTenantIdAndStatus(Integer tenantId, String status);
    Page<Payment> findByTenantIdAndStatus(Integer tenantId, String status, Pageable pageable);

    Optional<Payment> findByTenantIdAndTransactionId(Integer tenantId, String transactionId);

    /**
     * WARNING: Internal use only for Stripe webhooks.
     * Never expose this method through public API endpoints.
     * Webhooks are already authenticated via Stripe signature verification.
     */
    Optional<Payment> findByTransactionId(String transactionId);

    void deleteByTenantIdAndPaymentId(Integer tenantId, Long paymentId);
}