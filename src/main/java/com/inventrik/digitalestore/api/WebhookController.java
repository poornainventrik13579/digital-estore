package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhook Management", description = "APIs for handling payment webhooks")
@Slf4j
public class WebhookController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/stripe")
    @Operation(summary = "Handle Stripe webhook events", description = "Process payment webhook events from Stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = true) String signature) {
        
        try {
            paymentService.handlePaymentWebhook(payload, signature);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (IllegalArgumentException e) {
            log.warn("Invalid webhook payload: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid payload");
        } catch (SecurityException e) {
            log.error("Webhook signature validation failed: {}", e.getMessage());
            return ResponseEntity.status(401).body("Unauthorized");
        } catch (Exception e) {
            log.error("Unexpected error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}