package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Find payment by tenant and payment ID
    Optional<Payment> findByTenantIdAndPaymentId(Integer tenantId, Long paymentId);
    
    // Find payments by tenant and order ID
    List<Payment> findByTenantIdAndOrderId(Integer tenantId, Long orderId);
    
    // Find all payments for a tenant
    List<Payment> findByTenantId(Integer tenantId);
    
    // Find payments by tenant and status
    List<Payment> findByTenantIdAndStatus(Integer tenantId, String status);
    
    // Find payment by transaction ID
    Optional<Payment> findByTransactionId(String transactionId);
    
    // Delete payment by tenant and payment ID
    void deleteByTenantIdAndPaymentId(Integer tenantId, Long paymentId);
}