package com.inventrik.digitalestore.service.payment;

import com.inventrik.digitalestore.dto.request.PartialRefundRequest;
import com.inventrik.digitalestore.dto.request.PaymentRequest;
import com.inventrik.digitalestore.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {
    
    // Get all payments for a tenant
    List<PaymentResponse> getAllPayments(Integer tenantId);
    
    // Get a single payment by ID
    PaymentResponse getPayment(Integer tenantId, Long paymentId);
    
    // Create a new payment (initiate payment process)
    PaymentResponse createPayment(Integer tenantId, String username, PaymentRequest paymentRequest);
    
    // Confirm a payment (after successful processing)
    PaymentResponse confirmPayment(Integer tenantId, Long paymentId, String transactionId, String username);
    
    // Cancel a payment
    PaymentResponse cancelPayment(Integer tenantId, Long paymentId, String username);
    
    // Process a refund
    PaymentResponse refundPayment(Integer tenantId, Long paymentId, String username);
    
    // Process a partial refund
    PaymentResponse partialRefundPayment(Integer tenantId, Long paymentId, PartialRefundRequest refundRequest, String username);
    
    // Get payments by order
    List<PaymentResponse> getPaymentsByOrder(Integer tenantId, Long orderId);
    
    // Get payments by status
    List<PaymentResponse> getPaymentsByStatus(Integer tenantId, String status);
    
    // Handle webhook from payment processor
    void handlePaymentWebhook(String payload, String signature);
}