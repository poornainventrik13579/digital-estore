// Update src/main/java/com/inventrik/digitalestore/service/email/EmailService.java
package com.inventrik.digitalestore.service.email;

import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.domain.payment.Payment;
import com.inventrik.digitalestore.domain.user.User;

public interface EmailService {
    void sendOrderConfirmationWithInvoice(Order order, User user, byte[] invoicePdf);
    void sendCancellationNotification(Order order, User user);
    void sendRefundNotification(Order order, Payment payment, User user);
    void sendAccountCreationConfirmation(User user);
    void sendPasswordResetLink(User user, String resetToken);
    // Add new method for digital product access
    void sendDigitalProductAccessEmail(Order order, User user);
}