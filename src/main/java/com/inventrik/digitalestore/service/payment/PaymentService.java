package com.inventrik.digitalestore.service.payment;

import com.inventrik.digitalestore.dto.request.PartialRefundRequest;
import com.inventrik.digitalestore.dto.request.PaymentRequest;
import com.inventrik.digitalestore.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

    List<PaymentResponse> getAllPayments(Integer tenantId, String username, boolean canAccessAllPayments, String orderId, String status);

    PaymentResponse getPayment(Integer tenantId, String paymentId);

    PaymentResponse createPayment(Integer tenantId, String username, PaymentRequest paymentRequest);

    PaymentResponse confirmPayment(Integer tenantId, String paymentId, String transactionId, String username);

    PaymentResponse cancelPayment(Integer tenantId, String paymentId, String username);

    PaymentResponse refundPayment(Integer tenantId, String paymentId, String username);

    PaymentResponse partialRefundPayment(Integer tenantId, String paymentId, PartialRefundRequest refundRequest, String username);

    void handlePaymentWebhook(String payload, String signature);
}