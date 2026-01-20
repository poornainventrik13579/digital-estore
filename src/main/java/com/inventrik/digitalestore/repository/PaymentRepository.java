package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Payment.PaymentPK> {

    Optional<Payment> findByTenantIdAndPaymentId(Integer tenantId, String paymentId);

    @Query("SELECT p FROM Payment p WHERE p.tenantId = :tenantId AND p.orderId = :orderId ORDER BY p.paymentDate DESC")
    List<Payment> findByTenantIdAndOrderId(@Param("tenantId") Integer tenantId, @Param("orderId") String orderId);

    @Query("SELECT p FROM Payment p WHERE p.tenantId = :tenantId ORDER BY p.paymentDate DESC")
    List<Payment> findByTenantId(@Param("tenantId") Integer tenantId);

    @Query("SELECT p FROM Payment p WHERE p.tenantId = :tenantId AND p.status = :status ORDER BY p.paymentDate DESC")
    List<Payment> findByTenantIdAndStatus(@Param("tenantId") Integer tenantId, @Param("status") String status);

    Optional<Payment> findByTransactionId(String transactionId);

    void deleteByTenantIdAndPaymentId(Integer tenantId, String paymentId);
}