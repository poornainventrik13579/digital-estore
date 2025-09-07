package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByTenantIdAndPaymentId(Integer tenantId, Long paymentId);
    
    List<Payment> findByTenantIdAndOrderId(Integer tenantId, Long orderId);
    
    List<Payment> findByTenantId(Integer tenantId);
    
    List<Payment> findByTenantIdAndStatus(Integer tenantId, String status);
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    void deleteByTenantIdAndPaymentId(Integer tenantId, Long paymentId);
}