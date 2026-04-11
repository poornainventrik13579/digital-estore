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
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

import com.inventrik.digitalestore.service.config.ConfigService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final ConfigService configService;

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
    public List<PaymentResponse> getAllPayments(Integer tenantId, String username, boolean canAccessAllPayments, String orderId, String status) {
        String userId = null;

        if (!canAccessAllPayments) {
            userId = userRepository.findByTenantIdAndUsername(tenantId, username)
                    .map(user -> user.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        }

        if (userId != null && orderId != null) {
            Order order = orderRepository.findByTenantIdAndOrderId(tenantId, orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

            if (!order.getUserId().equals(userId)) {
                return List.of();
            }

            return paymentRepository.findByTenantIdAndOrderId(tenantId, orderId).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        if (userId != null) {
            List<Order> userOrders = orderRepository.findByTenantIdAndUserId(tenantId, userId);
            return userOrders.stream()
                    .flatMap(order -> paymentRepository.findByTenantIdAndOrderId(tenantId, order.getOrderId()).stream())
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        if (orderId != null) {
            return paymentRepository.findByTenantIdAndOrderId(tenantId, orderId).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        if (status != null && !status.trim().isEmpty()) {
            return paymentRepository.findByTenantIdAndStatus(tenantId, status).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        return paymentRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse getPayment(Integer tenantId, String paymentId) {
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
                log.info("createPayment called for order {} by user {}. Idempotency key: {}",
                        paymentRequest.getOrderId(), username, idempotencyKey);
                boolean keyRegistered = idempotencyKeyService.isKeyRegistered(idempotencyKey);
                log.info("Idempotency key registered: {}", keyRegistered);

                if (keyRegistered) {
                    // Find existing payment for this order
                    List<Payment> existingPayments = paymentRepository.findByTenantIdAndOrderId(tenantId, paymentRequest.getOrderId());
                    log.info("Found {} existing payments for order {}", existingPayments.size(), paymentRequest.getOrderId());
                    if (!existingPayments.isEmpty()) {
                        Payment existingPayment = existingPayments.get(0);
                        log.info("Existing payment status: {}, transactionId: {}",
                                existingPayment.getStatus(), existingPayment.getTransactionId());

                        // Allow retry if previous payment failed or Stripe session expired (webhook may not have arrived yet)
                        boolean allowRetry = PaymentStatus.FAILED.getDisplayName().equals(existingPayment.getStatus());

                        if (!allowRetry && PaymentStatus.PENDING.getDisplayName().equals(existingPayment.getStatus())) {
                            String txId = existingPayment.getTransactionId();
                            try {
                                if (txId != null && txId.startsWith("cs_")) {
                                    Session stripeSession = Session.retrieve(txId);
                                    log.info("Checking Stripe Session {} status: {}", txId, stripeSession.getStatus());
                                    if ("expired".equals(stripeSession.getStatus())) {
                                        log.info("Stripe Session expired for order {}. Allowing retry.", paymentRequest.getOrderId());
                                        allowRetry = true;
                                    }
                                } else {
                                    PaymentIntent stripeIntent = PaymentIntent.retrieve(txId);
                                    log.info("Checking Stripe PaymentIntent {} status: {}, has lastPaymentError: {}",
                                            txId, stripeIntent.getStatus(), stripeIntent.getLastPaymentError() != null);

                                    if ("canceled".equals(stripeIntent.getStatus()) ||
                                        "requires_payment_method".equals(stripeIntent.getStatus())) {
                                        log.info("Stripe PaymentIntent is in retryable state for order {}. Status: {}. Allowing retry.",
                                                paymentRequest.getOrderId(), stripeIntent.getStatus());
                                        allowRetry = true;
                                    }
                                }
                            } catch (StripeException e) {
                                log.warn("Failed to check Stripe status for {}: {}", txId, e.getMessage());
                                allowRetry = true;
                            }
                        }

                        if (allowRetry) {
                            log.info("Previous payment failed for order {}. Allowing retry.", paymentRequest.getOrderId());
                            idempotencyKeyService.removeKey(idempotencyKey);
                        } else {
                            log.warn("Duplicate payment attempt detected for order {}, returning existing payment {} (allowRetry: {})",
                                    paymentRequest.getOrderId(), existingPayment.getPaymentId(), allowRetry);
                            return mapToDTO(existingPayment);
                        }
                    }
                }

                if (!idempotencyKeyService.registerKey(idempotencyKey)) {
                    throw new PaymentProcessingException("Duplicate payment request detected", false);
                }
                
                Order order = orderRepository.findByTenantIdAndOrderId(tenantId, paymentRequest.getOrderId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + paymentRequest.getOrderId()));

                String newPaymentId = idGeneratorService.generateId(tenantId, "PAYMENT");
                String paymentProvider = configService.getPaymentProvider(tenantId);
                log.info("Payment provider for tenant {}: {}", tenantId, paymentProvider);

                String transactionId;
                String clientSecret = null;
                String sessionUrl = null;

                if ("STRIPE_REDIRECT".equals(paymentProvider)) {
                    Session checkoutSession = retryService.executeWithRetry(() -> {
                        try {
                            long amountLong = paymentRequest.getAmount().setScale(0, BigDecimal.ROUND_HALF_UP).longValue();

                            SessionCreateParams params = SessionCreateParams.builder()
                                    .setMode(SessionCreateParams.Mode.PAYMENT)
                                    .setSuccessUrl(stripeConfig.getCheckoutSuccessUrl())
                                    .setCancelUrl(stripeConfig.getCheckoutCancelUrl())
                                    .addLineItem(SessionCreateParams.LineItem.builder()
                                            .setQuantity(1L)
                                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(paymentRequest.getCurrency().toLowerCase())
                                                    .setUnitAmount(amountLong)
                                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName("Order #" + paymentRequest.getOrderId())
                                                            .build())
                                                    .build())
                                            .build())
                                    .putMetadata("orderId", paymentRequest.getOrderId())
                                    .putMetadata("tenantId", tenantId.toString())
                                    .putMetadata("paymentId", newPaymentId)
                                    .build();

                            return Session.create(params);
                        } catch (StripeException e) {
                            throw new PaymentProcessingException("Failed to create Stripe Checkout Session: " + e.getMessage(), e, true);
                        }
                    });

                    transactionId = checkoutSession.getId();
                    sessionUrl = checkoutSession.getUrl();
                    log.info("Created Stripe Checkout Session {} for order {}", transactionId, paymentRequest.getOrderId());

                } else {
                    PaymentIntent paymentIntent = retryService.executeWithRetry(() -> {
                        try {
                            Map<String, Object> params = new HashMap<>();
                            params.put("currency", paymentRequest.getCurrency().toLowerCase());
                            long amountLong = paymentRequest.getAmount().setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
                            params.put("amount", amountLong);
                            params.put("description", "Payment for order #" + paymentRequest.getOrderId());
                            params.put("capture_method", "automatic");

                            Map<String, Object> automaticPaymentMethods = new HashMap<>();
                            automaticPaymentMethods.put("enabled", true);
                            automaticPaymentMethods.put("allow_redirects", "never");
                            params.put("automatic_payment_methods", automaticPaymentMethods);

                            Map<String, String> metadata = new HashMap<>();
                            metadata.put("orderId", paymentRequest.getOrderId());
                            metadata.put("tenantId", tenantId.toString());
                            metadata.put("paymentId", newPaymentId);
                            params.put("metadata", metadata);

                            return PaymentIntent.create(params);
                        } catch (StripeException e) {
                            throw new PaymentProcessingException("Failed to create payment with Stripe: " + e.getMessage(), e, true);
                        }
                    });

                    transactionId = paymentIntent.getId();
                    clientSecret = paymentIntent.getClientSecret();
                    log.info("Created PaymentIntent {} for order {}", transactionId, paymentRequest.getOrderId());
                }

                Payment payment = new Payment();
                payment.setTenantId(tenantId);
                payment.setPaymentId(newPaymentId);
                payment.setOrderId(paymentRequest.getOrderId());
                payment.setCurrency(paymentRequest.getCurrency());
                payment.setPaymentDate(LocalDateTime.now());
                payment.setAmount(paymentRequest.getAmount());
                payment.setPaymentMethod(paymentRequest.getPaymentMethod());
                payment.setTransactionId(transactionId);
                payment.setStatus(PaymentStatus.PENDING.getDisplayName());

                String truncatedUsername = userService.truncateUsernameForAudit(username);
                payment.setCreatedBy(truncatedUsername);
                payment.setUpdatedBy(truncatedUsername);
                payment.setCreated(LocalDateTime.now());
                payment.setUpdated(LocalDateTime.now());

                Payment savedPayment = paymentRepository.save(payment);

                paymentEventLogger.logPaymentCreation(savedPayment, username);

                PaymentResponse response = mapToDTO(savedPayment);
                response.setClientSecret(clientSecret);
                response.setSessionUrl(sessionUrl);
                response.setPaymentProvider(paymentProvider);

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
    public PaymentResponse confirmPayment(Integer tenantId, String paymentId, String transactionId, String username) {
    return transactionCoordinator.executeInStrictTransaction(() -> {
        try {
            Payment payment = paymentRepository.findByTenantIdAndPaymentId(tenantId, paymentId)
                    .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
            
            if (!PaymentStatus.PENDING.getDisplayName().equals(payment.getStatus()) &&
                !PaymentStatus.PROCESSING.getDisplayName().equals(payment.getStatus())) {
                throw new PaymentProcessingException("Cannot confirm payment that is not in pending or processing state", false);
            }

            if (payment.getTransactionId() != null && payment.getTransactionId().startsWith("cs_")) {
                throw new PaymentProcessingException(
                    "Redirect payments are confirmed automatically via Stripe webhook. Do not call confirmPayment.", false);
            }

            String oldStatus = payment.getStatus();

            retryService.executeWithRetry(() -> {
                try {
                    PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getTransactionId());
                    
                    if ("requires_confirmation".equals(paymentIntent.getStatus())) {
                        Map<String, Object> confirmParams = new HashMap<>();
                        confirmParams.put("return_url", stripeConfig.getCheckoutSuccessUrl());
                        paymentIntent.confirm(confirmParams);
                    } else if ("requires_payment_method".equals(paymentIntent.getStatus())) {
                        throw new PaymentProcessingException(
                            "Payment requires a payment method. Complete payment through the checkout form.", false);
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

            String truncatedUsername = userService.truncateUsernameForAudit(username);

            if ("succeeded".equals(paymentIntent.getStatus())) {
                payment.setStatus(PaymentStatus.SUCCESSFUL.getDisplayName());

                Order order = orderRepository.findByTenantIdAndOrderId(tenantId, payment.getOrderId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + payment.getOrderId()));

                order.setStatus("Completed");
                order.setUpdatedBy(truncatedUsername);
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
    public PaymentResponse cancelPayment(Integer tenantId, String paymentId, String username) {
        return transactionCoordinator.executeInTransaction(() -> {
            try {
                Payment payment = paymentRepository.findByTenantIdAndPaymentId(tenantId, paymentId)
                        .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
                
                if (PaymentStatus.SUCCESSFUL.getDisplayName().equals(payment.getStatus()) || 
                    PaymentStatus.REFUNDED.getDisplayName().equals(payment.getStatus())) {
                    throw new PaymentProcessingException("Cannot cancel payment that is already completed or refunded", false);
                }
                
                try {
                    String txId = payment.getTransactionId();
                    if (txId != null && txId.startsWith("cs_")) {
                        retryService.executeWithRetry(() -> {
                            try {
                                Session stripeSession = Session.retrieve(txId);
                                if ("open".equals(stripeSession.getStatus())) {
                                    stripeSession.expire();
                                }
                                return null;
                            } catch (StripeException e) {
                                throw new PaymentProcessingException("Failed to expire Checkout Session: " + e.getMessage(), e, true);
                            }
                        });
                    } else {
                        retryService.executeWithRetry(() -> {
                            try {
                                PaymentIntent paymentIntent = PaymentIntent.retrieve(txId);
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
                    }
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
    public PaymentResponse partialRefundPayment(Integer tenantId, String paymentId, PartialRefundRequest refundRequest, String username) {
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
                        params.put("amount", refundRequest.getRefundAmount()
                                .multiply(new BigDecimal(100))
                                .setScale(0, java.math.RoundingMode.HALF_UP)
                                .longValue());
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

                String truncatedUsername = userService.truncateUsernameForAudit(username);
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
    public PaymentResponse refundPayment(Integer tenantId, String paymentId, String username) {
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
                        params.put("amount", remainingAmount
                                .multiply(new BigDecimal(100))
                                .setScale(0, java.math.RoundingMode.HALF_UP)
                                .longValue());
                        
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

                String truncatedUsername = userService.truncateUsernameForAudit(username);
                order.setUpdatedBy(truncatedUsername);
                order.setUpdated(LocalDateTime.now());
                orderRepository.save(order);

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
        StripeObject stripeObject;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            try {
                stripeObject = dataObjectDeserializer.deserializeUnsafe();
            } catch (EventDataObjectDeserializationException e) {
                log.error("Failed to deserialize Stripe event {} type={} apiVersion={}: {}",
                        event.getId(), event.getType(), event.getApiVersion(), e.getMessage());
                throw new PaymentProcessingException("Event deserialization failed", e, true);
            }
        }


        String transactionIdForLog = null;

        switch (event.getType()) {
            case "payment_intent.succeeded":
                PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                transactionIdForLog = paymentIntent.getId();
                handleSuccessfulPayment(paymentIntent);
                break;

            case "payment_intent.payment_failed":
                paymentIntent = (PaymentIntent) stripeObject;
                transactionIdForLog = paymentIntent.getId();
                handleFailedPayment(paymentIntent);
                break;

            case "charge.refunded":
                com.stripe.model.Charge charge = (com.stripe.model.Charge) stripeObject;
                transactionIdForLog = charge.getPaymentIntent();
                handleRefundedCharge(charge);
                break;

            case "checkout.session.completed":
                Session session = (Session) stripeObject;
                transactionIdForLog = session.getId();
                handleCheckoutSessionCompleted(session);
                break;

            case "checkout.session.expired":
                session = (Session) stripeObject;
                transactionIdForLog = session.getId();
                handleCheckoutSessionExpired(session);
                break;

            default:
                log.info("Unhandled event type: {}", event.getType());
                break;
        }

        if (transactionIdForLog != null) {
            final String txId = transactionIdForLog;
            paymentRepository.findByTransactionId(txId).ifPresent(payment -> {
                paymentEventLogger.logWebhookEvent(
                    payment.getPaymentId(),
                    event.getType(),
                    String.format("Received webhook event: %s, ID: %s", event.getType(), event.getId())
                );
            });
        }
    }
    
    private void handleSuccessfulPayment(PaymentIntent paymentIntent) {
        paymentRepository.findByTransactionId(paymentIntent.getId()).ifPresentOrElse(payment -> {
            if (PaymentStatus.SUCCESSFUL.getDisplayName().equals(payment.getStatus())) {
                log.info("Payment {} already SUCCESSFUL, skipping duplicate payment_intent.succeeded", payment.getPaymentId());
                return;
            }

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
                    order.setStatus("Completed");
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
        }, () -> log.warn("Webhook payment_intent.succeeded: no Payment found for transactionId {}", paymentIntent.getId()));
    }

    private void handleFailedPayment(PaymentIntent paymentIntent) {
        paymentRepository.findByTransactionId(paymentIntent.getId()).ifPresentOrElse(payment -> {
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
        }, () -> log.warn("Webhook payment_intent.payment_failed: no Payment found for transactionId {}", paymentIntent.getId()));
    }

    private void handleCheckoutSessionCompleted(Session session) {
        paymentRepository.findByTransactionId(session.getId()).ifPresentOrElse(payment -> {
            String oldStatus = payment.getStatus();
            payment.setStatus(PaymentStatus.SUCCESSFUL.getDisplayName());
            payment.setUpdated(LocalDateTime.now());
            payment.setUpdatedBy("wh");

            // Mutate transactionId from session (cs_*) to PaymentIntent (pi_*) for refund/cancel compatibility
            String paymentIntentId = session.getPaymentIntent();
            if (paymentIntentId != null) {
                log.info("Updating transactionId from {} to {} for payment {}",
                        payment.getTransactionId(), paymentIntentId, payment.getPaymentId());
                payment.setTransactionId(paymentIntentId);
            }

            Payment updatedPayment = paymentRepository.save(payment);

            paymentEventLogger.logPaymentStatusChange(
                updatedPayment, oldStatus, PaymentStatus.SUCCESSFUL.getDisplayName(), "webhook");

            orderRepository.findByTenantIdAndOrderId(payment.getTenantId(), payment.getOrderId()).ifPresent(order -> {
                if (!"Completed".equals(order.getStatus()) &&
                    !"Cancelled".equals(order.getStatus()) &&
                    !"Refunded".equals(order.getStatus())) {

                    order.setStatus("Completed");
                    order.setUpdated(LocalDateTime.now());
                    order.setUpdatedBy("wh");
                    Order savedOrder = orderRepository.save(order);

                    userRepository.findByTenantIdAndUserId(payment.getTenantId(), order.getUserId()).ifPresent(user -> {
                        try {
                            byte[] invoicePdf = invoiceService.generateInvoice(savedOrder, user);
                            invoiceService.storeInvoice(savedOrder, invoicePdf);
                            emailService.sendOrderConfirmationWithInvoice(savedOrder, user, invoicePdf);
                            log.info("Order confirmation email sent via checkout webhook for order {}", savedOrder.getOrderId());
                        } catch (Exception e) {
                            log.error("Failed to generate invoice or send email for order {}: {}",
                                    savedOrder.getOrderId(), e.getMessage(), e);
                        }
                    });
                }
            });
        }, () -> log.warn("Webhook checkout.session.completed: no Payment found for sessionId {}", session.getId()));
    }

    private void handleCheckoutSessionExpired(Session session) {
        paymentRepository.findByTransactionId(session.getId()).ifPresentOrElse(payment -> {
            String oldStatus = payment.getStatus();
            payment.setStatus(PaymentStatus.FAILED.getDisplayName());
            payment.setUpdated(LocalDateTime.now());
            payment.setUpdatedBy("wh");

            Payment updatedPayment = paymentRepository.save(payment);

            paymentEventLogger.logPaymentStatusChange(
                updatedPayment, oldStatus, PaymentStatus.FAILED.getDisplayName(), "webhook");

            orderRepository.findByTenantIdAndOrderId(payment.getTenantId(), payment.getOrderId()).ifPresent(order -> {
                if ("Pending".equals(order.getStatus())) {
                    order.setStatus("Payment Failed");
                    order.setUpdated(LocalDateTime.now());
                    order.setUpdatedBy("wh");
                    Order savedOrder = orderRepository.save(order);

                    userRepository.findByTenantIdAndUserId(payment.getTenantId(), order.getUserId()).ifPresent(user -> {
                        try {
                            emailService.sendPaymentFailureNotification(savedOrder, updatedPayment, user, "Checkout session expired");
                            log.info("Checkout expiry email sent for order {}", savedOrder.getOrderId());
                        } catch (Exception e) {
                            log.error("Failed to send checkout expiry email: {}", e.getMessage(), e);
                        }
                    });
                }
            });
        }, () -> log.warn("Webhook checkout.session.expired: no Payment found for sessionId {}", session.getId()));
    }

    private void handleRefundedCharge(com.stripe.model.Charge charge) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(charge.getPaymentIntent());
            
            paymentRepository.findByTransactionId(paymentIntent.getId()).ifPresentOrElse(payment -> {
                BigDecimal stripeRefundedCents = new BigDecimal(charge.getAmountRefunded());
                BigDecimal currentRefunded = payment.getRefundedAmount() != null ? payment.getRefundedAmount() : BigDecimal.ZERO;
                BigDecimal currentRefundedCents = currentRefunded.multiply(new BigDecimal(100))
                        .setScale(0, java.math.RoundingMode.HALF_UP);

                if (stripeRefundedCents.compareTo(currentRefundedCents) == 0
                        && (PaymentStatus.REFUNDED.getDisplayName().equals(payment.getStatus())
                            || PaymentStatus.PARTIALLY_REFUNDED.getDisplayName().equals(payment.getStatus()))) {
                    log.info("Payment {} already at refund state, skipping duplicate charge.refunded webhook", payment.getPaymentId());
                    return;
                }

                String oldStatus = payment.getStatus();

                BigDecimal refundedDollars = new BigDecimal(charge.getAmountRefunded())
                        .divide(new BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);
                payment.setRefundedAmount(refundedDollars);

                if (charge.getAmountRefunded().equals(charge.getAmount())) {
                    payment.setStatus(PaymentStatus.REFUNDED.getDisplayName());
                } else {
                    payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED.getDisplayName());
                }

                payment.setUpdated(LocalDateTime.now());

                Payment updatedPayment = paymentRepository.save(payment);

                paymentEventLogger.logPaymentStatusChange(
                    updatedPayment, oldStatus, payment.getStatus(), "webhook");

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
            }, () -> log.warn("Webhook charge.refunded: no Payment found for transactionId {}", paymentIntent.getId()));
        } catch (StripeException e) {
            log.error("Error retrieving payment intent for refunded charge: {}", e.getMessage(), e);
        }
    }


}