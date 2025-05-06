package com.inventrik.digitalestore.service.payment;

import com.inventrik.digitalestore.config.StripeConfig;
import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.domain.payment.Payment;
import com.inventrik.digitalestore.domain.payment.PaymentStatus;
import com.inventrik.digitalestore.dto.request.PaymentRequest;
import com.inventrik.digitalestore.dto.response.PaymentResponse;
import com.inventrik.digitalestore.exception.BusinessException;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.OrderRepository;
import com.inventrik.digitalestore.repository.PaymentRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    
    private PaymentResponse mapToDTO(Payment payment) {
        PaymentResponse response = new PaymentResponse(
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
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        return mapToDTO(payment);
    }
    
    @Override
    @Transactional
    public PaymentResponse createPayment(Integer tenantId, String username, PaymentRequest paymentRequest) {
        // Verify order exists
        Order order = orderRepository.findByTenantIdAndOrderId(tenantId, paymentRequest.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + paymentRequest.getOrderId()));
        
        // Generate a new payment ID
        Long newPaymentId = System.currentTimeMillis();
        
        // Create a payment intent with Stripe
        PaymentIntent paymentIntent;
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setCurrency(paymentRequest.getCurrency().toLowerCase())
                .setAmount(paymentRequest.getAmount().multiply(new java.math.BigDecimal(100)).longValue())
                .setDescription("Payment for order #" + paymentRequest.getOrderId())
                .setPaymentMethod(paymentRequest.getPaymentToken())
                .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
                .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.AUTOMATIC)
                .build();
            
            paymentIntent = PaymentIntent.create(params);
        } catch (StripeException e) {
            throw new BusinessException("Failed to create payment with Stripe: " + e.getMessage(), e);
        }
        
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
        
        Payment savedPayment = paymentRepository.save(payment);
        PaymentResponse response = mapToDTO(savedPayment);
        
        // Add client secret for client-side confirmation
        response.setClientSecret(paymentIntent.getClientSecret());
        
        return response;
    }
    
    @Override
    @Transactional
    public PaymentResponse confirmPayment(Integer tenantId, Long paymentId, String transactionId, String username) {
        Payment payment = paymentRepository.findByTenantIdAndPaymentId(tenantId, paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        
        if (!PaymentStatus.PENDING.getDisplayName().equals(payment.getStatus()) && 
            !PaymentStatus.PROCESSING.getDisplayName().equals(payment.getStatus())) {
            throw new BusinessException("Cannot confirm payment that is not in pending or processing state");
        }
        
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getTransactionId());
            
            if (paymentIntent.getStatus().equals("requires_confirmation")) {
                paymentIntent.confirm();
            }
            
            if (paymentIntent.getStatus().equals("succeeded")) {
                payment.setStatus(PaymentStatus.SUCCESSFUL.getDisplayName());
                
                // Update the order status if necessary
                Order order = orderRepository.findByTenantIdAndOrderId(tenantId, payment.getOrderId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + payment.getOrderId()));
                order.setStatus("Processing");
                order.setUpdatedBy(username.length() > 2 ? username.substring(0, 2) : username);
                order.setUpdated(LocalDateTime.now());
                orderRepository.save(order);
            } else {
                // Handle other statuses like requires_payment_method, requires_action, etc.
                payment.setStatus(PaymentStatus.PROCESSING.getDisplayName());
            }
        } catch (StripeException e) {
            payment.setStatus(PaymentStatus.FAILED.getDisplayName());
            throw new BusinessException("Failed to confirm payment with Stripe: " + e.getMessage(), e);
        }
        
        // Ensure username is truncated to 2 characters as per DB schema
        String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
        payment.setUpdatedBy(truncatedUsername);
        payment.setUpdated(LocalDateTime.now());
        
        Payment updatedPayment = paymentRepository.save(payment);
        
        return mapToDTO(updatedPayment);
    }
    
    @Override
    @Transactional
    public PaymentResponse cancelPayment(Integer tenantId, Long paymentId, String username) {
        Payment payment = paymentRepository.findByTenantIdAndPaymentId(tenantId, paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        
        if (PaymentStatus.SUCCESSFUL.getDisplayName().equals(payment.getStatus()) || 
            PaymentStatus.REFUNDED.getDisplayName().equals(payment.getStatus())) {
            throw new BusinessException("Cannot cancel payment that is already completed or refunded");
        }
        
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getTransactionId());
            if (paymentIntent.getStatus().equals("requires_payment_method") || 
                paymentIntent.getStatus().equals("requires_confirmation") ||
                paymentIntent.getStatus().equals("requires_action")) {
                paymentIntent.cancel();
            }
        } catch (StripeException e) {
            log.error("Failed to cancel payment with Stripe: {}", e.getMessage(), e);
            // Continue with local cancelation even if Stripe call fails
        }
        
        payment.setStatus(PaymentStatus.FAILED.getDisplayName());
        
        // Ensure username is truncated to 2 characters as per DB schema
        String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
        payment.setUpdatedBy(truncatedUsername);
        payment.setUpdated(LocalDateTime.now());
        
        Payment updatedPayment = paymentRepository.save(payment);
        
        return mapToDTO(updatedPayment);
    }
    
    @Override
    @Transactional
    public PaymentResponse refundPayment(Integer tenantId, Long paymentId, String username) {
        Payment payment = paymentRepository.findByTenantIdAndPaymentId(tenantId, paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        
        if (!PaymentStatus.SUCCESSFUL.getDisplayName().equals(payment.getStatus())) {
            throw new BusinessException("Only successful payments can be refunded");
        }
        
        try {
            // Create a refund with Stripe
            Map<String, Object> params = new HashMap<>();
            params.put("payment_intent", payment.getTransactionId());
            
            com.stripe.model.Refund.create(params);
            
            payment.setStatus(PaymentStatus.REFUNDED.getDisplayName());
            
            // Update the order status
            Order order = orderRepository.findByTenantIdAndOrderId(tenantId, payment.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + payment.getOrderId()));
            order.setStatus("Refunded");
            order.setUpdatedBy(username.length() > 2 ? username.substring(0, 2) : username);
            order.setUpdated(LocalDateTime.now());
            orderRepository.save(order);
            
        } catch (StripeException e) {
            throw new BusinessException("Failed to process refund with Stripe: " + e.getMessage(), e);
        }
        
        // Ensure username is truncated to 2 characters as per DB schema
        String truncatedUsername = username.length() > 2 ? username.substring(0, 2) : username;
        payment.setUpdatedBy(truncatedUsername);
        payment.setUpdated(LocalDateTime.now());
        
        Payment updatedPayment = paymentRepository.save(payment);
        
        return mapToDTO(updatedPayment);
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
    @Transactional
    public void handlePaymentWebhook(String payload, String signature) {
        try {
            // Verify the webhook signature
            Event event = Webhook.constructEvent(payload, signature, stripeConfig.getWebhookSecret());
            
            // Process the event
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject = null;
            
            if (dataObjectDeserializer.getObject().isPresent()) {
                stripeObject = dataObjectDeserializer.getObject().get();
            }
            
            // Handle different event types
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                    handleSuccessfulPayment(paymentIntent);
                    break;
                
                case "payment_intent.payment_failed":
                    paymentIntent = (PaymentIntent) stripeObject;
                    handleFailedPayment(paymentIntent);
                    break;
                
                case "charge.refunded":
                    // Handle refund event
                    // For simplicity, we can look up the payment by transaction ID
                    // and update its status to refunded
                    break;
                
                default:
                    log.info("Unhandled event type: {}", event.getType());
                    break;
            }
        } catch (SignatureVerificationException e) {
            throw new BusinessException("Invalid webhook signature", e);
        } catch (Exception e) {
            throw new BusinessException("Error processing webhook: " + e.getMessage(), e);
        }
    }
    
    private void handleSuccessfulPayment(PaymentIntent paymentIntent) {
        // Find the payment by transaction ID
        paymentRepository.findByTransactionId(paymentIntent.getId()).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.SUCCESSFUL.getDisplayName());
            payment.setUpdated(LocalDateTime.now());
            
            paymentRepository.save(payment);
            
            // Update the order status
            orderRepository.findByTenantIdAndOrderId(payment.getTenantId(), payment.getOrderId()).ifPresent(order -> {
                order.setStatus("Processing");
                order.setUpdated(LocalDateTime.now());
                orderRepository.save(order);
            });
        });
    }
    
    private void handleFailedPayment(PaymentIntent paymentIntent) {
        // Find the payment by transaction ID
        paymentRepository.findByTransactionId(paymentIntent.getId()).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.FAILED.getDisplayName());
            payment.setUpdated(LocalDateTime.now());
            
            paymentRepository.save(payment);
            
            // Update the order status if necessary
            orderRepository.findByTenantIdAndOrderId(payment.getTenantId(), payment.getOrderId()).ifPresent(order -> {
                // Only update if the order is in a pending state
                if ("Pending".equals(order.getStatus())) {
                    order.setStatus("Payment Failed");
                    order.setUpdated(LocalDateTime.now());
                    orderRepository.save(order);
                }
            });
        });
    }
}