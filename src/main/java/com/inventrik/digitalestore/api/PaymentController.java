package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.request.PartialRefundRequest;
import com.inventrik.digitalestore.dto.request.PaymentRequest;
import com.inventrik.digitalestore.dto.response.PaymentResponse;
import com.inventrik.digitalestore.exception.payment.InsufficientRefundAmountException;
import com.inventrik.digitalestore.exception.payment.PaymentNotFoundException;
import com.inventrik.digitalestore.exception.payment.PaymentProcessingException;
import com.inventrik.digitalestore.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for managing payments")
@SecurityRequirement(name = "oauth2")
public class PaymentController {

    private final PaymentService paymentService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get all payments with optional filters: ?orderId={id} or ?status={status}")
    public ResponseEntity<List<PaymentResponse>> getAllPayments(
            @PathVariable Integer tenantId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(paymentService.getAllPayments(tenantId, orderId, status));
    }
    
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Get a payment by ID")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable Integer tenantId,
            @PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(tenantId, paymentId));
    }
    
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Create a new payment (JSON)")
    public ResponseEntity<PaymentResponse> createPaymentJson(
            @PathVariable Integer tenantId,
            @Valid @RequestBody PaymentRequest paymentRequest,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        PaymentResponse createdPayment = paymentService.createPayment(tenantId, username, paymentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
    }
    
    @PostMapping("/{paymentId}/confirm")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Confirm a payment")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable Integer tenantId,
            @PathVariable Long paymentId,
            @RequestParam String transactionId,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        PaymentResponse confirmedPayment = paymentService.confirmPayment(tenantId, paymentId, transactionId, username);
        return ResponseEntity.ok(confirmedPayment);
    }
    
    @PostMapping("/{paymentId}/cancel")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Cancel a payment")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable Integer tenantId,
            @PathVariable Long paymentId,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        PaymentResponse cancelledPayment = paymentService.cancelPayment(tenantId, paymentId, username);
        return ResponseEntity.ok(cancelledPayment);
    }
    
    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Refund a payment")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Integer tenantId,
            @PathVariable Long paymentId,
            Authentication authentication) {
        
        String username = (authentication != null) ? authentication.getName() : "system";
        PaymentResponse refundedPayment = paymentService.refundPayment(tenantId, paymentId, username);
        return ResponseEntity.ok(refundedPayment);
    }
    
    @PostMapping("/{paymentId}/partial-refund")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TENANT')")
    @Operation(summary = "Process partial refund", description = "Process a partial refund for a payment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Partial refund processed successfully"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "400", description = "Invalid refund request")
    })
    public ResponseEntity<PaymentResponse> partialRefundPayment(
            @PathVariable Integer tenantId,
            @PathVariable Long paymentId,
            @Valid @RequestBody PartialRefundRequest refundRequest,
            Authentication authentication) {
        
        try {
            PaymentResponse response = paymentService.partialRefundPayment(tenantId, paymentId, refundRequest, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (PaymentNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (InsufficientRefundAmountException | PaymentProcessingException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
}