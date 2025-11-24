package com.inventrik.digitalestore.service.logging;

import com.inventrik.digitalestore.domain.audit.PaymentAudit;
import com.inventrik.digitalestore.domain.payment.Payment;
import com.inventrik.digitalestore.repository.PaymentAuditRepository;
import com.inventrik.digitalestore.service.transaction.TransactionCoordinatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventLogger {

    private final PaymentAuditRepository paymentAuditRepository;
    private final TransactionCoordinatorService transactionCoordinator;

    public void logPaymentCreation(Payment payment, String username) {
        logPaymentEvent(payment, "CREATED", "Payment initiated", username);
    }

    public void logPaymentStatusChange(Payment payment, String oldStatus, String newStatus, String username) {
        logPaymentEvent(payment, "STATUS_CHANGED", 
                String.format("Status changed from %s to %s", oldStatus, newStatus), username);
    }

    public void logPaymentConfirmation(Payment payment, String transactionId, String username) {
        logPaymentEvent(payment, "CONFIRMED", 
                String.format("Payment confirmed with transaction ID: %s", transactionId), username);
    }

    public void logPaymentFailure(Payment payment, String errorMessage, String username) {
        logPaymentEvent(payment, "FAILED", 
                String.format("Payment failed: %s", errorMessage), username);
    }

    public void logPaymentCancellation(Payment payment, String username) {
        logPaymentEvent(payment, "CANCELLED", "Payment cancelled", username);
    }

    public void logPaymentRefund(Payment payment, String username) {
        logPaymentEvent(payment, "REFUNDED", "Payment refunded", username);
    }

    public void logPartialRefund(Payment payment, BigDecimal refundAmount, String reason, String username) {
        String details = String.format("Partial refund processed: amount=%s, reason=%s, total_refunded=%s", 
            refundAmount, reason, payment.getRefundedAmount());
        logPaymentEvent(payment, "PARTIAL_REFUND", details, username);
    }

    public void logWebhookEvent(Long paymentId, String eventType, String eventData) {
        PaymentAudit audit = new PaymentAudit();
        audit.setAuditId(UUID.randomUUID().toString());
        audit.setPaymentId(paymentId);
        audit.setEventType("WEBHOOK_" + eventType);
        audit.setEventDetails(eventData);
        audit.setPerformedBy("webhook");
        audit.setTimestamp(LocalDateTime.now());
        
        transactionCoordinator.executeWithIndependentCommit(() -> {
            paymentAuditRepository.save(audit);
            return null;
        });
        
        log.info("Payment webhook event logged: paymentId={}, eventType={}", paymentId, eventType);
    }

    private void logPaymentEvent(Payment payment, String eventType, String details, String username) {
        PaymentAudit audit = new PaymentAudit();
        audit.setAuditId(UUID.randomUUID().toString());
        audit.setPaymentId(payment.getPaymentId());
        audit.setEventType(eventType);
        audit.setEventDetails(details);
        audit.setPerformedBy(username);
        audit.setTimestamp(LocalDateTime.now());
        
        transactionCoordinator.executeWithIndependentCommit(() -> {
            paymentAuditRepository.save(audit);
            return null;
        });
        
        log.info("Payment event logged: paymentId={}, eventType={}, details={}, user={}",
                payment.getPaymentId(), eventType, details, username);
    }
}