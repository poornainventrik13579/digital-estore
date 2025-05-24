package com.inventrik.digitalestore.service.email;

import com.inventrik.digitalestore.domain.order.Order;
import com.inventrik.digitalestore.domain.payment.Payment;
import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.exception.email.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;
    private final SpringTemplateEngine templateEngine;
    
    @Value("${email.from}")
    private String fromAddress;
    
    @Value("${email.sender-name}")
    private String senderName;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public void sendOrderConfirmationWithInvoice(Order order, User user, byte[] invoicePdf) {
        try {
            // Prepare email context
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("order", order);
            templateModel.put("user", user);
            templateModel.put("orderItems", order.getOrderItems());
            templateModel.put("totalAmount", order.getTotalAmount());
            templateModel.put("currency", order.getCurrency());
            
            // Send email with attachment
            sendEmailWithAttachment(
                user.getEmail(),
                "Order Confirmation #" + order.getOrderId(),
                "email/order-confirmation",
                templateModel,
                "Invoice-" + order.getOrderId() + ".pdf",
                invoicePdf
            );
            
            log.info("Order confirmation email sent to {} for order {}", user.getEmail(), order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order {}: {}", order.getOrderId(), e.getMessage(), e);
            throw new EmailSendException("Failed to send order confirmation email", e);
        }
    }

    @Override
    public void sendCancellationNotification(Order order, User user) {
        try {
            // Prepare email context
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("order", order);
            templateModel.put("user", user);
            
            // Send email
            sendEmail(
                user.getEmail(),
                "Order Cancellation #" + order.getOrderId(),
                "email/order-cancellation",
                templateModel
            );
            
            log.info("Order cancellation email sent to {} for order {}", user.getEmail(), order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send order cancellation email for order {}: {}", order.getOrderId(), e.getMessage(), e);
            throw new EmailSendException("Failed to send order cancellation email", e);
        }
    }

    @Override
    public void sendRefundNotification(Order order, Payment payment, User user) {
        try {
            // Prepare email context
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("order", order);
            templateModel.put("payment", payment);
            templateModel.put("user", user);
            
            // Send email
            sendEmail(
                user.getEmail(),
                "Refund Confirmation #" + order.getOrderId(),
                "email/refund-confirmation",
                templateModel
            );
            
            log.info("Refund confirmation email sent to {} for order {}", user.getEmail(), order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send refund confirmation email for order {}: {}", order.getOrderId(), e.getMessage(), e);
            throw new EmailSendException("Failed to send refund confirmation email", e);
        }
    }

    @Override
    public void sendAccountCreationConfirmation(User user) {
        try {
            // Prepare email context
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("user", user);
            
            // Send email
            sendEmail(
                user.getEmail(),
                "Welcome to Digital E-Store!",
                "email/account-creation",
                templateModel
            );
            
            log.info("Account creation email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send account creation email for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw new EmailSendException("Failed to send account creation email", e);
        }
    }

    @Override
    public void sendPasswordResetLink(User user, String resetToken) {
        try {
            // Prepare email context
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("user", user);
            templateModel.put("resetToken", resetToken);
            
            // Send email
            sendEmail(
                user.getEmail(),
                "Password Reset Request",
                "email/password-reset",
                templateModel
            );
            
            log.info("Password reset email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw new EmailSendException("Failed to send password reset email", e);
        }
    }
    
    @Override
    public void sendDigitalProductAccessEmail(Order order, User user) {
        try {
            // Prepare email context
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("order", order);
            templateModel.put("user", user);
            templateModel.put("orderItems", order.getOrderItems());
            
            // Send email
            sendEmail(
                user.getEmail(),
                "Your Digital Purchase is Ready - Order #" + order.getOrderId(),
                "email/digital-fulfillment",
                templateModel
            );
            
            log.info("Digital product access email sent to {} for order {}", user.getEmail(), order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send digital product access email: {}", e.getMessage(), e);
            throw new EmailSendException("Failed to send digital product access email", e);
        }
    }
    
    /**
     * Helper method to send a simple email
     */
    private void sendEmail(String to, String subject, String templateName, Map<String, Object> templateModel) 
            throws MessagingException, UnsupportedEncodingException {
        
        // Add baseUrl to template model
        templateModel.put("baseUrl", baseUrl);
        
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(new InternetAddress(fromAddress, senderName));
        helper.setTo(to);
        helper.setSubject(subject);
        
        Context context = new Context();
        templateModel.forEach(context::setVariable);
        
        String htmlContent = templateEngine.process(templateName, context);
        helper.setText(htmlContent, true);
        
        emailSender.send(message);
    }
    
    /**
     * Helper method to send an email with attachment
     */
    private void sendEmailWithAttachment(
            String to, 
            String subject, 
            String templateName, 
            Map<String, Object> templateModel,
            String attachmentFilename,
            byte[] attachmentData) throws MessagingException, UnsupportedEncodingException {
        
        // Add baseUrl to template model
        templateModel.put("baseUrl", baseUrl);
        
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(new InternetAddress(fromAddress, senderName));
        helper.setTo(to);
        helper.setSubject(subject);
        
        Context context = new Context();
        templateModel.forEach(context::setVariable);
        
        String htmlContent = templateEngine.process(templateName, context);
        helper.setText(htmlContent, true);
        
        // Add attachment
        helper.addAttachment(attachmentFilename, new ByteArrayResource(attachmentData));
        
        emailSender.send(message);
    }
    @Override
    public void sendPaymentFailureNotification(Order order, Payment payment, User user, String failureReason) {
        try {
            // Prepare email context
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("order", order);
            templateModel.put("payment", payment);
            templateModel.put("user", user);
            templateModel.put("failureReason", failureReason);
            
            // Send email
            sendEmail(
                user.getEmail(),
                "Payment Failed - Order #" + order.getOrderId(),
                "email/payment-failure",
                templateModel
            );
            
            log.info("Payment failure email sent to {} for order {}", user.getEmail(), order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send payment failure email for order {}: {}", order.getOrderId(), e.getMessage(), e);
            throw new EmailSendException("Failed to send payment failure email", e);
        }
    }
}