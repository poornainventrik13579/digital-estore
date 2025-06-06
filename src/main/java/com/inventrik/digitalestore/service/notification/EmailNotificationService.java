// src/main/java/com/inventrik/digitalestore/service/notification/EmailNotificationService.java
package com.inventrik.digitalestore.service.notification;

import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.domain.order.OrderStatus;
// import com.inventrik.digitalestore.domain.payment.Payment;
import com.inventrik.digitalestore.domain.user.User;
// import com.inventrik.digitalestore.repository.OrderRepository;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.email.EmailService;
// import com.inventrik.digitalestore.service.invoice.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final EmailService emailService;
    // private final InvoiceService invoiceService;
    private final UserRepository userRepository;
    // private final OrderRepository orderRepository;
    
    /**
     * Send notifications for order status changes
     */
    @Async
    public void handleOrderStatusChange(Order order, String oldStatus, String newStatus) {
        try {
            // Get the user who placed the order
            User user = userRepository.findByTenantIdAndUserId(order.getTenantId(), order.getUserId())
                    .orElse(null);
            
            if (user == null) {
                log.error("User not found for order {}, cannot send notification", order.getOrderId());
                return;
            }
            
            // Handle different status transitions
            if (OrderStatus.COMPLETED.getDisplayName().equals(newStatus)) {
                sendDigitalFulfillmentEmail(order, user);
            } else if (OrderStatus.CANCELLED.getDisplayName().equals(newStatus)) {
                emailService.sendCancellationNotification(order, user);
            } else if (OrderStatus.REFUNDED.getDisplayName().equals(newStatus) ||
                      OrderStatus.PARTIALLY_REFUNDED.getDisplayName().equals(newStatus)) {
                // The refund email is typically handled by the payment service
                // but we could add a backup trigger here if needed
            }
            
        } catch (Exception e) {
            log.error("Failed to send notification for order status change: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Send digital product fulfillment email
     */
    @Async
    public void sendDigitalFulfillmentEmail(Order order, User user) {
        try {
            // In a real implementation, you might prepare download links or access information here
            // For this example, we'll use a template that displays order details and license keys
            
            // Use a different template than the one used for order confirmation
            // You need to implement the method in EmailService interface and implementation
            sendDigitalProductAccessEmail(order, user);
            
            log.info("Digital fulfillment email sent for order {}", order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send digital fulfillment email for order {}: {}", 
                    order.getOrderId(), e.getMessage(), e);
        }
    }
    
    /**
     * This method needs to be added to the EmailService interface and implementation
     */
    private void sendDigitalProductAccessEmail(Order order, User user) {
        // Create a context for the digital-fulfillment.html template
        // with order and user information
        // This is a placeholder - the actual implementation would be in EmailServiceImpl
        emailService.sendDigitalProductAccessEmail(order, user);
    }
    
    /**
     * Send password reset email
     */
    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            emailService.sendPasswordResetLink(user, resetToken);
            log.info("Password reset email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Send welcome email on account creation
     */
    @Async
    public void sendWelcomeEmail(User user) {
        try {
            emailService.sendAccountCreationConfirmation(user);
            log.info("Welcome email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email: {}", e.getMessage(), e);
        }
    }
}