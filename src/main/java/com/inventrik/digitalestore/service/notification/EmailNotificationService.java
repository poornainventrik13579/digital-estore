
package com.inventrik.digitalestore.service.notification;

import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.domain.order.OrderStatus;

import com.inventrik.digitalestore.domain.user.User;

import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.email.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final EmailService emailService;
    
    private final UserRepository userRepository;
    
    @Async
    public void handleOrderStatusChange(Order order, String oldStatus, String newStatus) {
        try {
            
            User user = userRepository.findByTenantIdAndUserId(order.getTenantId(), order.getUserId())
                    .orElse(null);
            
            if (user == null) {
                log.error("User not found for order {}, cannot send notification", order.getOrderId());
                return;
            }
            
            if (OrderStatus.COMPLETED.getDisplayName().equals(newStatus)) {
                sendDigitalFulfillmentEmail(order, user);
            } else if (OrderStatus.CANCELLED.getDisplayName().equals(newStatus)) {
                emailService.sendCancellationNotification(order, user);
            } else if (OrderStatus.REFUNDED.getDisplayName().equals(newStatus) ||
                      OrderStatus.PARTIALLY_REFUNDED.getDisplayName().equals(newStatus)) {
                
            }
            
        } catch (Exception e) {
            log.error("Failed to send notification for order status change: {}", e.getMessage(), e);
        }
    }
    
    @Async
    public void sendDigitalFulfillmentEmail(Order order, User user) {
        try {
            
            sendDigitalProductAccessEmail(order, user);
            
            log.info("Digital fulfillment email sent for order {}", order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send digital fulfillment email for order {}: {}", 
                    order.getOrderId(), e.getMessage(), e);
        }
    }
    
    private void sendDigitalProductAccessEmail(Order order, User user) {
        
        emailService.sendDigitalProductAccessEmail(order, user);
    }
    
    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            emailService.sendPasswordResetLink(user, resetToken);
            log.info("Password reset email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage(), e);
        }
    }
    
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