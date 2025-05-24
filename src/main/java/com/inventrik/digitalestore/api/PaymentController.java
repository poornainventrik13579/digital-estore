package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.PaymentRequest;
import com.inventrik.digitalestore.dto.response.PaymentResponse;
import com.inventrik.digitalestore.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for processing payments")
public class PaymentController {

    private final PaymentService paymentService;
    
    @GetMapping
    @Operation(summary = "Get all payments")
    public ResponseEntity<List<PaymentResponse>> getAllPayments(
            @PathVariable Integer tenantId) {
        return ResponseEntity.ok(paymentService.getAllPayments(tenantId));
    }
    
    @GetMapping("/{paymentId}")
    @Operation(summary = "Get a payment by ID")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable Integer tenantId,
            @PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(tenantId, paymentId));
    }
    
    @PostMapping
    @Operation(summary = "Create a new payment")
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable Integer tenantId,
            @Valid @RequestBody PaymentRequest paymentRequest,
            Authentication authentication) {
        
        // Get username from authentication or use a default
        String username = (authentication != null) ? authentication.getName() : "system";
        
        PaymentResponse createdPayment = paymentService.createPayment(tenantId, username, paymentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
    }
    
    @PostMapping("/{paymentId}/confirm")
    @Operation(summary = "Confirm a payment")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable Integer tenantId,
            @PathVariable Long paymentId,
            @RequestParam String transactionId,
            Authentication authentication) {
        
        // Get username from authentication or use a default
        String username = (authentication != null) ? authentication.getName() : "system";
        
        PaymentResponse confirmedPayment = paymentService.confirmPayment(tenantId, paymentId, transactionId, username);
        return ResponseEntity.ok(confirmedPayment);
    }
    
    @PostMapping("/{paymentId}/cancel")
    @Operation(summary = "Cancel a payment")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable Integer tenantId,
            @PathVariable Long paymentId,
            Authentication authentication) {
        
        // Get username from authentication or use a default
        String username = (authentication != null) ? authentication.getName() : "system";
        
        PaymentResponse cancelledPayment = paymentService.cancelPayment(tenantId, paymentId, username);
        return ResponseEntity.ok(cancelledPayment);
    }
    
    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund a payment")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Integer tenantId,
            @PathVariable Long paymentId,
            Authentication authentication) {
        
        // Get username from authentication or use a default
        String username = (authentication != null) ? authentication.getName() : "system";
        
        PaymentResponse refundedPayment = paymentService.refundPayment(tenantId, paymentId, username);
        return ResponseEntity.ok(refundedPayment);
    }
    
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payments by order")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrder(
            @PathVariable Integer tenantId,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrder(tenantId, orderId));
    }
    
    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get payments by status",
        description = "Retrieves payments filtered by status. Valid status values: " +
                      "Pending, Processing, Successful, Failed, Refunded, Partially Refunded"
    )
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(
            @PathVariable Integer tenantId,
            @Parameter(
                description = "Payment status", 
                required = true
            ) 
            @PathVariable String status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(tenantId, status));
    }
}