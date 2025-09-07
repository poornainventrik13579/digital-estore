
package com.inventrik.digitalestore.service.email;

import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.domain.payment.Payment;
import com.inventrik.digitalestore.domain.user.User;

import java.math.BigDecimal;

public interface EmailService {
    void sendOrderConfirmationWithInvoice(Order order, User user, byte[] invoicePdf);
    void sendCancellationNotification(Order order, User user);
    void sendRefundNotification(Order order, Payment payment, User user);
    void sendPartialRefundNotification(Order order, Payment payment, BigDecimal refundAmount, User user);
    void sendAccountCreationConfirmation(User user);
    void sendPasswordResetLink(User user, String resetToken);
    void sendDigitalProductAccessEmail(Order order, User user);
    void sendPaymentFailureNotification(Order order, Payment payment, User user, String failureReason);
}