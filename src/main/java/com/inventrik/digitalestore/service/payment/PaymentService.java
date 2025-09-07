package com.inventrik.digitalestore.service.payment;

import com.inventrik.digitalestore.dto.request.PartialRefundRequest;
import com.inventrik.digitalestore.dto.request.PaymentRequest;
import com.inventrik.digitalestore.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {
    
    List<PaymentResponse> getAllPayments(Integer tenantId);
    
    PaymentResponse getPayment(Integer tenantId, Long paymentId);
    
    PaymentResponse createPayment(Integer tenantId, String username, PaymentRequest paymentRequest);
    
    PaymentResponse confirmPayment(Integer tenantId, Long paymentId, String transactionId, String username);
    
    PaymentResponse cancelPayment(Integer tenantId, Long paymentId, String username);
    
    PaymentResponse refundPayment(Integer tenantId, Long paymentId, String username);
    
    PaymentResponse partialRefundPayment(Integer tenantId, Long paymentId, PartialRefundRequest refundRequest, String username);
    
    List<PaymentResponse> getPaymentsByOrder(Integer tenantId, Long orderId);
    
    List<PaymentResponse> getPaymentsByStatus(Integer tenantId, String status);
    
    void handlePaymentWebhook(String payload, String signature);
}