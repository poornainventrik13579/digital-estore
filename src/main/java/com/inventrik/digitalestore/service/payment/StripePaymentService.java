package com.inventrik.digitalestore.service.payment;

import com.inventrik.digitalestore.config.StripeConfig;
import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.domain.order.OrderStatus;
import com.inventrik.digitalestore.domain.payment.Payment;
import com.inventrik.digitalestore.domain.payment.PaymentStatus;
import com.inventrik.digitalestore.dto.request.PartialRefundRequest;
import com.inventrik.digitalestore.dto.request.PaymentRequest;
import com.inventrik.digitalestore.dto.response.PaymentResponse;
import com.inventrik.digitalestore.exception.payment.InsufficientRefundAmountException;
import com.inventrik.digitalestore.exception.payment.PaymentNotFoundException;
import com.inventrik.digitalestore.exception.payment.PaymentProcessingException;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.OrderRepository;
import com.inventrik.digitalestore.repository.PaymentRepository;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import com.inventrik.digitalestore.service.email.EmailService;
import com.inventrik.digitalestore.service.invoice.InvoiceService;
import com.inventrik.digitalestore.service.logging.PaymentEventLogger;
import com.inventrik.digitalestore.service.transaction.TransactionCoordinatorService;
import com.inventrik.digitalestore.service.user.UserService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final StripeConfig stripeConfig;
    private final TransactionCoordinatorService transactionCoordinator;
    private final PaymentRetryService retryService;
    private final PaymentEventLogger paymentEventLogger;
    private final IdempotencyKeyService idempotencyKeyService;
    private final IdGeneratorService idGeneratorService;
    
    private final EmailService emailService;
    private final InvoiceService invoiceService;
    private final UserRepository userRepository;
    private final UserService userService;

    private PaymentResponse mapToDTO(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setTenantId(payment.getTenantId());
        response.setOrderId(payment.getOrderId());
        response.setCurrency(payment.getCurrency());
        response.setPaymentDate(payment.getPaymentDate());
        response.setAmount(payment.getAmount());
        response.setRefundedAmount(payment.getRefundedAmount());
        response.setRemainingAmount(payment.getRemainingAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setTransactionId(payment.getTransactionId());
        response.setStatus(payment.getStatus());
        response.setRefundReason(payment.getRefundReason());
        response.setCreated(payment.getCreated());
        response.setUpdated(payment.getUpdated());
        response.setClientSecret(null); 
        return response;
    }
    
    @Override
    public List<PaymentResponse> getAllPayments(Integer tenantId) {
        return paymentRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public PaymentResponse getPayment(Integer tenantId, Long paymentId) {
        Payment payment = paymentRepository.findByTenantIdAndPaymentId(tenantId, paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
        return mapToDTO(payment);
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public PaymentResponse createPayment(Integer tenantId, String username, PaymentRequest paymentRequest) {
        String idempotencyKey = tenantId + ":" + paymentRequest.getOrderId() + ":" + username;
        
        return transactionCoordinator.executeInStrictTransaction(() -> {
            try {
                
                if (idempotencyKeyService.isKeyRegistered(idempotencyKey)) {
                    
                    List<Payment> existingPayments = paymentRepository.findByTenantIdAndOrderId(tenantId, paymentRequest.getOrderId());
                    if (!existingPayments.isEmpty()) {
                        Payment existingPayment = existingPayments.get(0);
                        log.warn("Duplicate payment attempt detected for order {}, returning existing payment {}", 
                                paymentRequest.getOrderId(), existingPayment.getPaymentId());
                        return mapToDTO(existingPayment);
                    }
                }
                
                if (!idempotencyKeyService.registerKey(idempotencyKey)) {
                    throw new PaymentProcessingException("Duplicate payment request detected", false);
                }
                
                Order order = orderRepository.findByTenantIdAndOrderId(tenantId, paymentRequest.getOrderId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + paymentRequest.getOrderId()));
                
                Long newPaymentId = idGeneratorService.generateId(tenantId, "PAYMENT");
                
                PaymentIntent paymentIntent = retryService.executeWithRetry(() -> {
                    try {
                        Map<String, Object> params = new HashMap<>();
                        params.put("currency", paymentRequest.getCurrency().toLowerCase());
                        
                        long amountLong = paymentRequest.getAmount()
                            .multiply(new BigDecimal("100"))
                            .setScale(0, RoundingMode.HALF_UP).longValue();
                        params.put("amount", amountLong);
                        params.put("description", "Payment for order #" + paymentRequest.getOrderId());
                        params.put("capture_method", "automatic");
                        
                        Map<String, Object> automaticPaymentMethods = new HashMap<>();
                        automaticPaymentMethods.put("enabled", true);
                        automaticPaymentMethods.put("allow_redirects", "never");
                        params.put("automatic_payment_methods", automaticPaymentMethods);
                        
                        Map<String, String> metadata = new HashMap<>();
                        metadata.put("orderId", paymentRequest.getOrderId().toString());
                        metadata.put("tenantId", tenantId.toString());
                        metadata.put("paymentId", newPaymentId.toString());
                        params.put("metadata", metadata);
                        
                        return PaymentIntent.create(params);
                    } catch (StripeException e) {
                        throw new PaymentProcessingException("Failed to create payment with Stripe: " + e.getMessage(), e, true);
                    }
                });
                
                Payment payment = new Payment();
                payment.setTenantId(tenantId);
                payment.setPaymentId(newPaymentId);
                payment.setOrderId(paymentRequest.getOrderId());
                payment.setCurrency(paymentRequest.getCurrency());
                payment.setPaymentDate(LocalDateTime.now());
                payment.setAmount(paymentRequest.getAmount());
                payment.setPaymentMethod(paymentRequest.getPaymentMethod());
                payment.setTransactionId(paymentIntent.getId());
                payment.setStatus(PaymentStatus.PENDING.getDisplayName());
                
                payment.setCreatedBy(username);
                payment.setUpdatedBy(username);
                payment.setCreated(LocalDateTime.now());
                payment.setUpdated(LocalDateTime.now());
                
                Payment savedPayment = paymentRepository.save(payment);
                
                paymentEventLogger.logPaymentCreation(savedPayment, username);
                
                PaymentResponse response = mapToDTO(savedPayment);
                
                response.setClientSecret(paymentIntent.getClientSecret());
                
                return response;
            } catch (Exception e) {
                if (e instanceof PaymentProcessingException || e instanceof ResourceNotFoundException) {
                    throw e;
                }
                throw new PaymentProcessingException("Failed to create payment: " + e.getMessage(), e, false);
            }
        });
    }
        
    @Override
    public PaymentResponse confirmPayment(Integer tenantId, Long paymentId, String transactionId, String username) {
    return transactionCoordinator.executeInStrictTransaction(() -> {
        try {
            Payment payment = paymentRepository.findByTenantIdAndPaymentId(tenantId, paymentId)
                    .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
            
            if (!PaymentStatus.PENDING.getDisplayName().equals(payment.getStatus()) && 
                !PaymentStatus.PROCESSING.getDisplayName().equals(payment.getStatus())) {
                throw new PaymentProcessingException("Cannot confirm payment that is not in pending or processing state", false);
            }
            
            String oldStatus = payment.getStatus();
            
            retryService.executeWithRetry(() -> {
                try {
                    PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getTransactionId());
                    
                    if ("requires_confirmation".equals(paymentIntent.getStatus()) || 
                        "requires_payment_method".equals(paymentIntent.getStatus())) {
                        
                        Map<String, Object> confirmParams = new HashMap<>();
                        confirmParams.put("payment_method", "pm_card_visa"); 
                        confirmParams.put("return_url", "http://localhost:8080/payment/success");
                        
                        paymentIntent.confirm(confirmParams);
                    }
                    
                    return paymentIntent;
                } catch (StripeException e) {
                    throw new PaymentProcessingException("Failed to confirm payment with Stripe: " + e.getMessage(), e, true);
                }
            });
            
            PaymentIntent paymentIntent = retryService.executeWithRetry(() -> {
                try {
                    return PaymentIntent.retrieve(payment.getTransactionId());
                } catch (StripeException e) {
                    throw new PaymentProcessingException("Failed to retrieve payment intent: " + e.getMessage(), e, true);
                }
            });
            
            if ("succeeded".equals(paymentIntent.getStatus())) {
                payment.setStatus(PaymentStatus.SUCCESSFUL.getDisplayName());
                
                Order order = orderRepository.findByTenantIdAndOrderId(tenantId, payment.getOrderId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + payment.getOrderId()));
                
                order.setStatus("Processing");
                order.setUpdatedBy(userService.getAuditCode(username));
                order.setUpdated(LocalDateTime.now());
                Order savedOrder = orderRepository.save(order);
                
                paymentEventLogger.logPaymentConfirmation(payment, transactionId, username);
                
                userRepository.findByTenantIdAndUserId(tenantId, savedOrder.getUserId()).ifPresent(user -> {
                    try {
                        
                        byte[] invoicePdf = invoiceService.generateInvoice(savedOrder, user);
                        
                        invoiceService.storeInvoice(savedOrder, invoicePdf);
                        
                        emailService.sendOrderConfirmationWithInvoice(savedOrder, user, invoicePdf);
                        
                        log.info("Order confirmation email sent for order {}", savedOrder.getOrderId());
                    } catch (Exception e) {
                        log.error("Failed to generate invoice or send email for order {}: {}", 
                                savedOrder.getOrderId(), e.getMessage(), e);
                    }
                });
            } else {
                
                payment.setStatus(PaymentStatus.PROCESSING.getDisplayName());
                
                paymentEventLogger.logPaymentStatusChange(payment, oldStatus, payment.getStatus(), username);
            }
            
            String truncatedUsername = userService.getAuditCode(username);
            payment.setUpdatedBy(truncatedUsername);
            payment.setUpdated(LocalDateTime.now());
            
            Payment updatedPayment = paymentRepository.save(payment);
            
            return mapToDTO(updatedPayment);
        } catch (Exception e) {
            if (e instanceof PaymentProcessingException || e instanceof ResourceNotFoundException ||
                e instanceof PaymentNotFoundException) {
                throw e;
            }
            throw new PaymentProcessingException("Failed to confirm payment: " + e.getMessage(), e, false);
        }
    });
}     
    
    @Override
    public PaymentResponse cancelPayment(Integer tenantId, Long paymentId, String username) {
        return transactionCoordinator.executeInTransaction(() -> {
            try {
                Payment payment = paymentRepository.findByTenantIdAndPaymentId(tenantId, paymentId)
                        .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
                
                if (PaymentStatus.SUCCESSFUL.getDisplayName().equals(payment.getStatus()) || 
                    PaymentStatus.REFUNDED.getDisplayName().equals(payment.getStatus())) {
                    throw new PaymentProcessingException("Cannot cancel payment that is already completed or refunded", false);
                }
                
                try {
                    retryService.executeWithRetry(() -> {
                        try {
                            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getTransactionId());
                            if ("requires_payment_method".equals(paymentIntent.getStatus()) || 
                                "requires_confirmation".equals(paymentIntent.getStatus()) ||
                                "requires_action".equals(paymentIntent.getStatus())) {
                                Map<String, Object> params = new HashMap<>();
                                paymentIntent.cancel(params);
                            }
                            return null;
                        } catch (StripeException e) {
                            throw new PaymentProcessingException("Failed to cancel payment with Stripe: " + e.getMessage(), e, true);
                        }
                    });
                } catch (PaymentProcessingException e) {
                    log.error("Error canceling payment with Stripe: {}", e.getMessage(), e);
                    
                }
                
                payment.setStatus(PaymentStatus.FAILED.getDisplayName());
                
                String truncatedUsername = userService.truncateUsernameForAudit(username);
                payment.setUpdatedBy(truncatedUsername);
                payment.setUpdated(LocalDateTime.now());
                
                Payment updatedPayment = paymentRepository.save(payment);
                
                paymentEventLogger.logPaymentCancellation(updatedPayment, username);
                
                orderRepository.findByTenantIdAndOrderId(tenantId, payment.getOrderId()).ifPresent(order -> {
                    order.setStatus(OrderStatus.CANCELLED.getDisplayName());
                    order.setUpdatedBy(truncatedUsername);
                    order.setUpdated(LocalDateTime.now());
                    Order savedOrder = orderRepository.save(order);
                    
                    userRepository.findByTenantIdAndUserId(tenantId, order.getUserId()).ifPresent(user -> {
                        try {
                            emailService.sendCancellationNotification(savedOrder, user);
                            log.info("Order cancellation email sent for order {}", savedOrder.getOrderId());
                        } catch (Exception e) {
                            log.error("Failed to send cancellation email: {}", e.getMessage(), e);
                        }
                    });
                });
                
                return mapToDTO(updatedPayment);
            } catch (Exception e) {
                if (e instanceof PaymentProcessingException || e instanceof PaymentNotFoundException) {
                    throw e;
                }
                throw new PaymentProcessingException("Failed to cancel payment: " + e.getMessage(), e, false);
            }
        });
    }
    
    @Override
    public PaymentResponse partialRefundPayment(Integer tenantId, Long paymentId, PartialRefundRequest refundRequest, String username) {
        return transactionCoordinator.executeInStrictTransaction(() -> {
            try {
                Payment payment = paymentRepository.findByTenantIdAndPaymentId(tenantId, paymentId)
                        .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
                
                if (!PaymentStatus.SUCCESSFUL.getDisplayName().equals(payment.getStatus()) && 
                    !PaymentStatus.PARTIALLY_REFUNDED.getDisplayName().equals(payment.getStatus())) {
                    throw new PaymentProcessingException("Only successful or partially refunded payments can be partially refunded", false);
                }
                
                BigDecimal currentRefundedAmount = payment.getRefundedAmount() != null ? payment.getRefundedAmount() : BigDecimal.ZERO;
                BigDecimal remainingAmount = payment.getAmount().subtract(currentRefundedAmount);
                
                if (refundRequest.getRefundAmount().compareTo(remainingAmount) > 0) {
                    throw new InsufficientRefundAmountException("Refund amount (" + refundRequest.getRefundAmount() + 
                        ") exceeds remaining amount (" + remainingAmount + ")");
                }
                
                if (refundRequest.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new InsufficientRefundAmountException("Refund amount must be greater than zero");
                }
                
                retryService.executeWithRetry(() -> {
                    try {
                        Map<String, Object> params = new HashMap<>();
                        params.put("payment_intent", payment.getTransactionId());
                        params.put("amount", refundRequest.getRefundAmount().multiply(new BigDecimal(100)).longValue());
                        params.put("reason", "requested_by_customer");
                        
                        Map<String, String> metadata = new HashMap<>();
                        metadata.put("paymentId", paymentId.toString());
                        metadata.put("tenantId", tenantId.toString());
                        metadata.put("partialRefund", "true");
                        params.put("metadata", metadata);
                        
                        com.stripe.model.Refund.create(params);
                        return null;
                    } catch (StripeException e) {
                        throw new PaymentProcessingException("Failed to process partial refund with Stripe: " + e.getMessage(), e, true);
                    }
                });
                
                BigDecimal newRefundedAmount = currentRefundedAmount.add(refundRequest.getRefundAmount());
                payment.setRefundedAmount(newRefundedAmount);
                payment.setRefundReason(refundRequest.getReason());
                
                if (newRefundedAmount.compareTo(payment.getAmount()) >= 0) {
                    payment.setStatus(PaymentStatus.REFUNDED.getDisplayName());
                } else {
                    payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED.getDisplayName());
                }
                
                String truncatedUsername = userService.getAuditCode(username);
                payment.setUpdatedBy(truncatedUsername);
                payment.setUpdated(LocalDateTime.now());
                
                Payment updatedPayment = paymentRepository.save(payment);
                
                Order order = orderRepository.findByTenantIdAndOrderId(tenantId, payment.getOrderId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + payment.getOrderId()));
                
                if (payment.isFullyRefunded()) {
                    order.setStatus("Refunded");
                } else {
                    order.setStatus("Partially Refunded");
                }
                order.setUpdatedBy(truncatedUsername);
                order.setUpdated(LocalDateTime.now());
                orderRepository.save(order);
                
                userRepository.findByTenantIdAndUserId(tenantId, order.getUserId()).ifPresent(user -> {
                    try {
                        emailService.sendPartialRefundNotification(order, updatedPayment, refundRequest.getRefundAmount(), user);
                        log.info("Partial refund notification email sent for payment {}, amount {}", paymentId, refundRequest.getRefundAmount());
                    } catch (Exception e) {
                        log.error("Failed to send partial refund notification email: {}", e.getMessage(), e);
                    }
                });
                
                paymentEventLogger.logPartialRefund(updatedPayment, refundRequest.getRefundAmount(), refundRequest.getReason(), username);
                
                return mapToDTO(updatedPayment);
            } catch (Exception e) {
                if (e instanceof PaymentProcessingException || e instanceof ResourceNotFoundException || 
                    e instanceof InsufficientRefundAmountException) {
                    throw e;
                }
                throw new PaymentProcessingException("Failed to process partial refund: " + e.getMessage(), e, false);
            }
        });
    }

    @Override
    public PaymentResponse refundPayment(Integer tenantId, Long paymentId, String username) {
        return transactionCoordinator.executeInStrictTransaction(() -> {
            try {
                Payment payment = paymentRepository.findByTenantIdAndPaymentId(tenantId, paymentId)
                        .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
                
                if (!PaymentStatus.SUCCESSFUL.getDisplayName().equals(payment.getStatus()) && 
                    !PaymentStatus.PARTIALLY_REFUNDED.getDisplayName().equals(payment.getStatus())) {
                    throw new PaymentProcessingException("Only successful or partially refunded payments can be refunded", false);
                }
                
                BigDecimal currentRefundedAmount = payment.getRefundedAmount() != null ? payment.getRefundedAmount() : BigDecimal.ZERO;
                BigDecimal remainingAmount = payment.getAmount().subtract(currentRefundedAmount);
                
                if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new PaymentProcessingException("Payment is already fully refunded", false);
                }
                
                retryService.executeWithRetry(() -> {
                    try {
                        Map<String, Object> params = new HashMap<>();
                        params.put("payment_intent", payment.getTransactionId());
                        params.put("amount", remainingAmount.multiply(new BigDecimal(100)).longValue());
                        
                        com.stripe.model.Refund.create(params);
                        return null;
                    } catch (StripeException e) {
                        throw new PaymentProcessingException("Failed to process refund with Stripe: " + e.getMessage(), e, true);
                    }
                });
                
                payment.setRefundedAmount(payment.getAmount());
                payment.setStatus(PaymentStatus.REFUNDED.getDisplayName());
                payment.setRefundReason("Full refund requested");
                
                Order order = orderRepository.findByTenantIdAndOrderId(tenantId, payment.getOrderId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + payment.getOrderId()));
                order.setStatus("Refunded");
                order.setUpdatedBy(userService.getAuditCode(username));
                order.setUpdated(LocalDateTime.now());
                orderRepository.save(order);
                
                String truncatedUsername = userService.getAuditCode(username);
                payment.setUpdatedBy(truncatedUsername);
                payment.setUpdated(LocalDateTime.now());
                
                Payment updatedPayment = paymentRepository.save(payment);
                
                userRepository.findByTenantIdAndUserId(tenantId, order.getUserId()).ifPresent(user -> {
                    try {
                        emailService.sendRefundNotification(order, updatedPayment, user);
                        log.info("Refund notification email sent for payment {}", paymentId);
                    } catch (Exception e) {
                        log.error("Failed to send refund notification email: {}", e.getMessage(), e);
                    }
                });
                
                paymentEventLogger.logPaymentRefund(updatedPayment, username);
                
                return mapToDTO(updatedPayment);
            } catch (Exception e) {
                if (e instanceof PaymentProcessingException || e instanceof ResourceNotFoundException) {
                    throw e;
                }
                throw new PaymentProcessingException("Failed to refund payment: " + e.getMessage(), e, false);
            }
        });
    }
    
    @Override
    public List<PaymentResponse> getPaymentsByOrder(Integer tenantId, Long orderId) {
        return paymentRepository.findByTenantIdAndOrderId(tenantId, orderId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PaymentResponse> getPaymentsByStatus(Integer tenantId, String status) {
        return paymentRepository.findByTenantIdAndStatus(tenantId, status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public void handlePaymentWebhook(String payload, String signature) {
        try {
            
            Event event = Webhook.constructEvent(payload, signature, stripeConfig.getWebhookSecret());
            
            transactionCoordinator.executeInNewTransaction(() -> {
                try {
                    processStripeEvent(event);
                    return null;
                } catch (Exception e) {
                    log.error("Error processing Stripe webhook event: {}", e.getMessage(), e);
                    throw e;
                }
            });
        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe webhook signature: {}", e.getMessage());
            throw new PaymentProcessingException("Invalid webhook signature", e, false);
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Error processing webhook: " + e.getMessage(), e, false);
        }
    }
    
    private void processStripeEvent(Event event) {
        
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            log.error("Failed to deserialize Stripe event object");
            return;
        }
        
        String paymentIntentId = null;
        
        switch (event.getType()) {
            case "payment_intent.succeeded":
                PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                paymentIntentId = paymentIntent.getId();
                handleSuccessfulPayment(paymentIntent);
                break;
                
            case "payment_intent.payment_failed":
                paymentIntent = (PaymentIntent) stripeObject;
                paymentIntentId = paymentIntent.getId();
                handleFailedPayment(paymentIntent);
                break;
                
            case "charge.refunded":
                com.stripe.model.Charge charge = (com.stripe.model.Charge) stripeObject;
                paymentIntentId = charge.getPaymentIntent();
                handleRefundedCharge(charge);
                break;
                
            default:
                log.info("Unhandled event type: {}", event.getType());
                break;
        }
        
        if (paymentIntentId != null) {
            paymentRepository.findByTransactionId(paymentIntentId).ifPresent(payment -> {
                paymentEventLogger.logWebhookEvent(
                    payment.getPaymentId(),
                    event.getType(),
                    String.format("Received webhook event: %s, ID: %s", event.getType(), event.getId())
                );
            });
        }
    }
    
    private void handleSuccessfulPayment(PaymentIntent paymentIntent) {
        
        paymentRepository.findByTransactionId(paymentIntent.getId()).ifPresent(payment -> {
            String oldStatus = payment.getStatus();
            payment.setStatus(PaymentStatus.SUCCESSFUL.getDisplayName());
            payment.setUpdated(LocalDateTime.now());
            
            Payment updatedPayment = paymentRepository.save(payment);
            
            paymentEventLogger.logPaymentStatusChange(
                updatedPayment, 
                oldStatus,
                PaymentStatus.SUCCESSFUL.getDisplayName(),
                "webhook"
            );
            
            orderRepository.findByTenantIdAndOrderId(payment.getTenantId(), payment.getOrderId()).ifPresent(order -> {
                if (!"Completed".equals(order.getStatus()) && 
                    !"Cancelled".equals(order.getStatus()) && 
                    !"Refunded".equals(order.getStatus())) {
                    order.setStatus("Processing");
                    order.setUpdated(LocalDateTime.now());
                    order.setUpdatedBy("wh"); 
                    Order savedOrder = orderRepository.save(order);
                    
                    userRepository.findByTenantIdAndUserId(payment.getTenantId(), order.getUserId()).ifPresent(user -> {
                        try {
                            
                            byte[] invoicePdf = invoiceService.generateInvoice(savedOrder, user);
                            
                            invoiceService.storeInvoice(savedOrder, invoicePdf);
                            
                            emailService.sendOrderConfirmationWithInvoice(savedOrder, user, invoicePdf);
                            
                            log.info("Order confirmation email sent via webhook for order {}", savedOrder.getOrderId());
                        } catch (Exception e) {
                            log.error("Failed to generate invoice or send email for order {}: {}", 
                                    savedOrder.getOrderId(), e.getMessage(), e);
                        }
                    });
                }
            });
        });
    }
    
    private void handleFailedPayment(PaymentIntent paymentIntent) {
        
        paymentRepository.findByTransactionId(paymentIntent.getId()).ifPresent(payment -> {
            
            payment.setStatus(PaymentStatus.FAILED.getDisplayName());
            payment.setUpdated(LocalDateTime.now());
            
            Payment updatedPayment = paymentRepository.save(payment);
            
            String failureMessage = "Unknown error";
            if (paymentIntent.getLastPaymentError() != null) {
                failureMessage = paymentIntent.getLastPaymentError().getMessage();
            }
            
            final String finalFailureMessage = failureMessage;
            
            paymentEventLogger.logPaymentFailure(updatedPayment, finalFailureMessage, "webhook");
            
            orderRepository.findByTenantIdAndOrderId(payment.getTenantId(), payment.getOrderId()).ifPresent(order -> {
                
                if ("Pending".equals(order.getStatus())) {
                    order.setStatus("Payment Failed");
                    order.setUpdated(LocalDateTime.now());
                    order.setUpdatedBy("wh"); 
                    Order savedOrder = orderRepository.save(order);
                    
                    userRepository.findByTenantIdAndUserId(payment.getTenantId(), order.getUserId()).ifPresent(user -> {
                        try {
                            emailService.sendPaymentFailureNotification(savedOrder, updatedPayment, user, finalFailureMessage);
                            log.info("Payment failure email sent via webhook for order {}", savedOrder.getOrderId());
                        } catch (Exception e) {
                            log.error("Failed to send payment failure email: {}", e.getMessage(), e);
                        }
                    });
                }
            });
        });
    }
    
    private void handleRefundedCharge(com.stripe.model.Charge charge) {
        try {
            
            PaymentIntent paymentIntent = PaymentIntent.retrieve(charge.getPaymentIntent());
            
            paymentRepository.findByTransactionId(paymentIntent.getId()).ifPresent(payment -> {
                String oldStatus = payment.getStatus();
                
                if (charge.getAmountRefunded().equals(charge.getAmount())) {
                    payment.setStatus(PaymentStatus.REFUNDED.getDisplayName());
                } else {
                    payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED.getDisplayName());
                }
                
                payment.setUpdated(LocalDateTime.now());
                
                Payment updatedPayment = paymentRepository.save(payment);
                
                paymentEventLogger.logPaymentStatusChange(
                    updatedPayment,
                    oldStatus,
                    payment.getStatus(),
                    "webhook"
                );
                
                orderRepository.findByTenantIdAndOrderId(payment.getTenantId(), payment.getOrderId()).ifPresent(order -> {
                    if (PaymentStatus.REFUNDED.getDisplayName().equals(payment.getStatus())) {
                        order.setStatus("Refunded");
                    } else {
                        order.setStatus("Partially Refunded");
                    }
                    order.setUpdated(LocalDateTime.now());
                    order.setUpdatedBy("wh"); 
                    Order savedOrder = orderRepository.save(order);
                    
                    userRepository.findByTenantIdAndUserId(payment.getTenantId(), order.getUserId()).ifPresent(user -> {
                        try {
                            emailService.sendRefundNotification(savedOrder, updatedPayment, user);
                            log.info("Refund notification email sent via webhook for order {}", savedOrder.getOrderId());
                        } catch (Exception e) {
                            log.error("Failed to send refund notification email: {}", e.getMessage(), e);
                        }
                    });
                });
            });
        } catch (StripeException e) {
            log.error("Error retrieving payment intent for refunded charge: {}", e.getMessage(), e);
        }
    }

}