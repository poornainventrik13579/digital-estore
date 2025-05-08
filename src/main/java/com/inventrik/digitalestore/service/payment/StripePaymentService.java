package com.inventrik.digitalestore.service.payment;

import com.inventrik.digitalestore.config.StripeConfig;
import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.domain.payment.Payment;
import com.inventrik.digitalestore.domain.payment.PaymentStatus;
import com.inventrik.digitalestore.dto.request.PaymentRequest;
import com.inventrik.digitalestore.dto.response.PaymentResponse;
import com.inventrik.digitalestore.exception.payment.PaymentNotFoundException;
import com.inventrik.digitalestore.exception.payment.PaymentProcessingException;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.OrderRepository;
import com.inventrik.digitalestore.repository.PaymentRepository;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.email.EmailService;
import com.inventrik.digitalestore.service.invoice.InvoiceService;
import com.inventrik.digitalestore.service.logging.PaymentEventLogger;
import com.inventrik.digitalestore.service.transaction.TransactionCoordinatorService;
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
    
    private final EmailService emailService;
    private final InvoiceService invoiceService;
    private final UserRepository userRepository;

    private PaymentResponse mapToDTO(Payment payment) {
        return new PaymentResponse(
            payment.getPaymentId(),
            payment.getTenantId(),
            payment.getOrderId(),
            payment.getCurrency(),
            payment.getPaymentDate(),
            payment.getAmount(),
            payment.getPaymentMethod(),
            payment.getTransactionId(),
            payment.getStatus(),
            payment.getCreated(),
            payment.getUpdated(),
            null // Client secret is set only when creating a payment
        );
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
    public PaymentResponse createPayment(Integer tenantId, String username, PaymentRequest paymentRequest) {
        // Generate a unique idempotency key for this request
        String idempotencyKey = tenantId + ":" + paymentRequest.getOrderId() + ":" + System.currentTimeMillis();
        
        return transactionCoordinator.executeInTransaction(() -> {
            try {
                // Verify order exists
                Order order = orderRepository.findByTenantIdAndOrderId(tenantId, paymentRequest.getOrderId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + paymentRequest.getOrderId()));
                
                // Generate a new payment ID
                Long newPaymentId = System.currentTimeMillis();
                
                // Create a payment intent with Stripe
                PaymentIntent paymentIntent = retryService.executeWithRetry(() -> {
                    try {
                        // Create parameters for Stripe payment intent
                        Map<String, Object> params = new HashMap<>();
                        params.put("currency", paymentRequest.getCurrency().toLowerCase());
                        params.put("amount", paymentRequest.getAmount().multiply(new java.math.BigDecimal(100)).longValue());
                        params.put("description", "Payment for order #" + paymentRequest.getOrderId());
                        params.put("confirmation_method", "manual");
                        params.put("capture_method", "automatic");
                        
                        // Add metadata
                        Map<String, String> metadata = new HashMap<>();
                        metadata.put("orderId", paymentRequest.getOrderId().toString());
                        metadata.put("tenantId", tenantId.toString());
                        metadata.put("paymentId", newPaymentId.toString());
                        params.put("metadata", metadata);
                        
                        // Add payment method if provided
                        if (paymentRequest.getPaymentToken() != null && !paymentRequest.getPaymentToken().isEmpty()) {
                            params.put("payment_method", paymentRequest.getPaymentToken());
                        }
                        
                        // Create the payment intent using the simplified approach
                        return PaymentIntent.create(params);
                    } catch (StripeException e) {
                        throw new PaymentProcessingException("Failed to create payment with Stripe: " + e.getMessage(), e, true);
                    }
                });
                
                // Create local payment record
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
                
                // Ensure username is truncated to 2 characters as per DB schema
                String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
                payment.setCreatedBy(truncatedUsername);
                payment.setUpdatedBy(truncatedUsername);
                payment.setCreated(LocalDateTime.now());
                payment.setUpdated(LocalDateTime.now());
                
                Payment savedPayment = paymentRepository.save(payment);
                
                // Log the payment creation
                paymentEventLogger.logPaymentCreation(savedPayment, username);
                
                PaymentResponse response = mapToDTO(savedPayment);
                
                // Add client secret for client-side confirmation
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
                        
                        if ("requires_confirmation".equals(paymentIntent.getStatus())) {
                            Map<String, Object> params = new HashMap<>();
                            paymentIntent.confirm(params);
                        }
                        
                        return paymentIntent;
                    } catch (StripeException e) {
                        throw new PaymentProcessingException("Failed to confirm payment with Stripe: " + e.getMessage(), e, true);
                    }
                });
                
                // Re-fetch the payment intent to get the latest status
                PaymentIntent paymentIntent = retryService.executeWithRetry(() -> {
                    try {
                        return PaymentIntent.retrieve(payment.getTransactionId());
                    } catch (StripeException e) {
                        throw new PaymentProcessingException("Failed to retrieve payment intent: " + e.getMessage(), e, true);
                    }
                });
                
                if ("succeeded".equals(paymentIntent.getStatus())) {
                    payment.setStatus(PaymentStatus.SUCCESSFUL.getDisplayName());
                    
                    // Update the order status if necessary
                    Order order = orderRepository.findByTenantIdAndOrderId(tenantId, payment.getOrderId())
                            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + payment.getOrderId()));
                    
                    order.setStatus("Processing");
                    order.setUpdatedBy(username.length() > 2 ? username.substring(0, 2) : username);
                    order.setUpdated(LocalDateTime.now());
                    Order savedOrder = orderRepository.save(order);
                    
                    // Generate invoice and send email
                    userRepository.findByTenantIdAndUserId(tenantId, savedOrder.getUserId()).ifPresent(user -> {
                        try {
                            // Generate invoice PDF
                            byte[] invoicePdf = invoiceService.generateInvoice(savedOrder, user);
                            
                            // Store invoice for future reference
                            invoiceService.storeInvoice(savedOrder, invoicePdf);
                            
                            // Send confirmation email with invoice attachment
                            emailService.sendOrderConfirmationWithInvoice(savedOrder, user, invoicePdf);
                        } catch (Exception e) {
                            log.error("Failed to generate invoice or send email for order {}: {}", 
                                    savedOrder.getOrderId(), e.getMessage(), e);
                        }
                    });
                    
                    // Log successful payment confirmation
                    paymentEventLogger.logPaymentConfirmation(payment, transactionId, username);
                } else {
                    // Handle other statuses
                    payment.setStatus(PaymentStatus.PROCESSING.getDisplayName());
                    
                    // Log payment status change
                    paymentEventLogger.logPaymentStatusChange(payment, oldStatus, payment.getStatus(), username);
                }
                
                // Ensure username is truncated to 2 characters as per DB schema
                String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
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
                    // Continue with local cancelation even if Stripe call fails
                }
                
                payment.setStatus(PaymentStatus.FAILED.getDisplayName());
                
                // Ensure username is truncated to 2 characters as per DB schema
                String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
                payment.setUpdatedBy(truncatedUsername);
                payment.setUpdated(LocalDateTime.now());
                
                Payment updatedPayment = paymentRepository.save(payment);
                
                // Log payment cancellation
                paymentEventLogger.logPaymentCancellation(updatedPayment, username);
                
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
    public PaymentResponse refundPayment(Integer tenantId, Long paymentId, String username) {
        return transactionCoordinator.executeInStrictTransaction(() -> {
            try {
                Payment payment = paymentRepository.findByTenantIdAndPaymentId(tenantId, paymentId)
                        .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
                
                if (!PaymentStatus.SUCCESSFUL.getDisplayName().equals(payment.getStatus())) {
                    throw new PaymentProcessingException("Only successful payments can be refunded", false);
                }
                
                retryService.executeWithRetry(() -> {
                    try {
                        // Create a refund with Stripe
                        Map<String, Object> params = new HashMap<>();
                        params.put("payment_intent", payment.getTransactionId());
                        
                        com.stripe.model.Refund.create(params);
                        return null;
                    } catch (StripeException e) {
                        throw new PaymentProcessingException("Failed to process refund with Stripe: " + e.getMessage(), e, true);
                    }
                });
                
                payment.setStatus(PaymentStatus.REFUNDED.getDisplayName());
                
                // Update the order status
                Order order = orderRepository.findByTenantIdAndOrderId(tenantId, payment.getOrderId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + payment.getOrderId()));
                order.setStatus("Refunded");
                order.setUpdatedBy(username.length() > 2 ? username.substring(0, 2) : username);
                order.setUpdated(LocalDateTime.now());
                orderRepository.save(order);
                
                // Ensure username is truncated to 2 characters as per DB schema
                String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
                payment.setUpdatedBy(truncatedUsername);
                payment.setUpdated(LocalDateTime.now());
                
                Payment updatedPayment = paymentRepository.save(payment);
                
                // Log payment refund
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
            // Verify the webhook signature
            Event event = Webhook.constructEvent(payload, signature, stripeConfig.getWebhookSecret());
            
            // Process the event in a new transaction
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
    
    /**
     * Process a Stripe event from a webhook.
     *
     * @param event The Stripe event
     */
    private void processStripeEvent(Event event) {
        // Extract the Stripe object from the event
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            log.error("Failed to deserialize Stripe event object");
            return;
        }
        
        // Extract payment intent ID
        String paymentIntentId = null;
        
        // Handle different event types
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
        
        // Log the webhook event
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
        // Find the payment by transaction ID
        paymentRepository.findByTransactionId(paymentIntent.getId()).ifPresent(payment -> {
            String oldStatus = payment.getStatus();
            payment.setStatus(PaymentStatus.SUCCESSFUL.getDisplayName());
            payment.setUpdated(LocalDateTime.now());
            
            Payment updatedPayment = paymentRepository.save(payment);
            
            // Log the status change
            paymentEventLogger.logPaymentStatusChange(
                updatedPayment, 
                oldStatus,
                PaymentStatus.SUCCESSFUL.getDisplayName(),
                "webhook"
            );
            
            // Update the order status
            orderRepository.findByTenantIdAndOrderId(payment.getTenantId(), payment.getOrderId()).ifPresent(order -> {
                if (!"Completed".equals(order.getStatus()) && 
                    !"Cancelled".equals(order.getStatus()) && 
                    !"Refunded".equals(order.getStatus())) {
                    order.setStatus("Processing");
                    order.setUpdated(LocalDateTime.now());
                    order.setUpdatedBy("wh"); // webhook
                    Order savedOrder = orderRepository.save(order);
                    
                    // Send order confirmation email with invoice
                    userRepository.findByTenantIdAndUserId(order.getTenantId(), order.getUserId()).ifPresent(user -> {
                        try {
                            // Generate invoice PDF
                            byte[] invoicePdf = invoiceService.generateInvoice(savedOrder, user);
                            
                            // Store invoice for future reference
                            invoiceService.storeInvoice(savedOrder, invoicePdf);
                            
                            // Send confirmation email with invoice attachment
                            emailService.sendOrderConfirmationWithInvoice(savedOrder, user, invoicePdf);
                            
                            log.info("Order confirmation email sent for order {}", savedOrder.getOrderId());
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
        // Find the payment by transaction ID
        paymentRepository.findByTransactionId(paymentIntent.getId()).ifPresent(payment -> {
            // String oldStatus = payment.getStatus();
            payment.setStatus(PaymentStatus.FAILED.getDisplayName());
            payment.setUpdated(LocalDateTime.now());
            
            Payment updatedPayment = paymentRepository.save(payment);
            
            // Get failure message
            String failureMessage = "Unknown error";
            if (paymentIntent.getLastPaymentError() != null) {
                failureMessage = paymentIntent.getLastPaymentError().getMessage();
            }
            
            // Log the payment failure
            paymentEventLogger.logPaymentFailure(updatedPayment, failureMessage, "webhook");
            
            // Update the order status if necessary
            orderRepository.findByTenantIdAndOrderId(payment.getTenantId(), payment.getOrderId()).ifPresent(order -> {
                // Only update if the order is in a pending state
                if ("Pending".equals(order.getStatus())) {
                    order.setStatus("Payment Failed");
                    order.setUpdated(LocalDateTime.now());
                    order.setUpdatedBy("wh"); // webhook
                    orderRepository.save(order);
                }
            });
        });
    }
    
    private void handleRefundedCharge(com.stripe.model.Charge charge) {
        try {
            // We need to get the PaymentIntent to find our payment
            PaymentIntent paymentIntent = PaymentIntent.retrieve(charge.getPaymentIntent());
            
            paymentRepository.findByTransactionId(paymentIntent.getId()).ifPresent(payment -> {
                String oldStatus = payment.getStatus();
                
                // Check if it's a full or partial refund
                if (charge.getAmountRefunded().equals(charge.getAmount())) {
                    payment.setStatus(PaymentStatus.REFUNDED.getDisplayName());
                } else {
                    payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED.getDisplayName());
                }
                
                payment.setUpdated(LocalDateTime.now());
                
                Payment updatedPayment = paymentRepository.save(payment);
                
                // Log the refund
                paymentEventLogger.logPaymentStatusChange(
                    updatedPayment,
                    oldStatus,
                    payment.getStatus(),
                    "webhook"
                );
                
                // Update the order status
                orderRepository.findByTenantIdAndOrderId(payment.getTenantId(), payment.getOrderId()).ifPresent(order -> {
                    if (PaymentStatus.REFUNDED.getDisplayName().equals(payment.getStatus())) {
                        order.setStatus("Refunded");
                    } else {
                        order.setStatus("Partially Refunded");
                    }
                    order.setUpdated(LocalDateTime.now());
                    order.setUpdatedBy("wh"); // webhook
                    orderRepository.save(order);
                });
            });
        } catch (StripeException e) {
            log.error("Error retrieving payment intent for refunded charge: {}", e.getMessage(), e);
        }
    }
}