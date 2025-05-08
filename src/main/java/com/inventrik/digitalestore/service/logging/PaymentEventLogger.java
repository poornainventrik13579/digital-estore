package com.inventrik.digitalestore.service.logging;

import com.inventrik.digitalestore.domain.audit.PaymentAudit;
import com.inventrik.digitalestore.domain.payment.Payment;
import com.inventrik.digitalestore.repository.PaymentAuditRepository;
import com.inventrik.digitalestore.service.transaction.TransactionCoordinatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for logging payment events for audit and troubleshooting purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventLogger {

    private final PaymentAuditRepository paymentAuditRepository;
    private final TransactionCoordinatorService transactionCoordinator;

    /**
     * Log a payment creation event.
     *
     * @param payment The payment being created
     * @param username The user performing the action
     */
    public void logPaymentCreation(Payment payment, String username) {
        logPaymentEvent(payment, "CREATED", "Payment initiated", username);
    }

    /**
     * Log a payment status change.
     *
     * @param payment The payment being updated
     * @param oldStatus The previous status
     * @param newStatus The new status
     * @param username The user performing the action
     */
    public void logPaymentStatusChange(Payment payment, String oldStatus, String newStatus, String username) {
        logPaymentEvent(payment, "STATUS_CHANGED", 
                String.format("Status changed from %s to %s", oldStatus, newStatus), username);
    }

    /**
     * Log a payment confirmation event.
     *
     * @param payment The payment being confirmed
     * @param transactionId The external transaction ID
     * @param username The user performing the action
     */
    public void logPaymentConfirmation(Payment payment, String transactionId, String username) {
        logPaymentEvent(payment, "CONFIRMED", 
                String.format("Payment confirmed with transaction ID: %s", transactionId), username);
    }

    /**
     * Log a payment failure event.
     *
     * @param payment The payment that failed
     * @param errorMessage The error message
     * @param username The user performing the action
     */
    public void logPaymentFailure(Payment payment, String errorMessage, String username) {
        logPaymentEvent(payment, "FAILED", 
                String.format("Payment failed: %s", errorMessage), username);
    }

    /**
     * Log a payment cancellation event.
     *
     * @param payment The payment being cancelled
     * @param username The user performing the action
     */
    public void logPaymentCancellation(Payment payment, String username) {
        logPaymentEvent(payment, "CANCELLED", "Payment cancelled", username);
    }

    /**
     * Log a payment refund event.
     *
     * @param payment The payment being refunded
     * @param username The user performing the action
     */
    public void logPaymentRefund(Payment payment, String username) {
        logPaymentEvent(payment, "REFUNDED", "Payment refunded", username);
    }

    /**
     * Log a payment webhook event.
     *
     * @param paymentId The payment ID
     * @param eventType The webhook event type
     * @param eventData The webhook event data
     */
    public void logWebhookEvent(Long paymentId, String eventType, String eventData) {
        PaymentAudit audit = new PaymentAudit();
        audit.setAuditId(UUID.randomUUID().toString());
        audit.setPaymentId(paymentId);
        audit.setEventType("WEBHOOK_" + eventType);
        audit.setEventDetails(eventData);
        audit.setPerformedBy("webhook");
        audit.setTimestamp(LocalDateTime.now());
        
        // Use a separate transaction to ensure the audit log is recorded
        // even if the main transaction fails
        transactionCoordinator.executeWithIndependentCommit(() -> {
            paymentAuditRepository.save(audit);
            return null;
        });
        
        log.info("Payment webhook event logged: paymentId={}, eventType={}", paymentId, eventType);
    }

    /**
     * Generic method for logging payment events.
     *
     * @param payment The payment
     * @param eventType The type of event
     * @param details Event details
     * @param username The user performing the action
     */
    private void logPaymentEvent(Payment payment, String eventType, String details, String username) {
        PaymentAudit audit = new PaymentAudit();
        audit.setAuditId(UUID.randomUUID().toString());
        audit.setPaymentId(payment.getPaymentId());
        audit.setEventType(eventType);
        audit.setEventDetails(details);
        audit.setPerformedBy(username);
        audit.setTimestamp(LocalDateTime.now());
        
        // Use a separate transaction to ensure the audit log is recorded
        // even if the main transaction fails
        transactionCoordinator.executeWithIndependentCommit(() -> {
            paymentAuditRepository.save(audit);
            return null;
        });
        
        log.info("Payment event logged: paymentId={}, eventType={}, details={}, user={}",
                payment.getPaymentId(), eventType, details, username);
    }
}