package com.inventrik.digitalestore.exception.payment;

/**
 * Exception thrown when a payment cannot be processed due to an error
 * with the payment processor or other system issues.
 */
public class PaymentProcessingException extends PaymentException {
    
    public PaymentProcessingException(String message) {
        super(message, true); // Most processing exceptions are retryable
    }
    
    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause, true);
    }
    
    public PaymentProcessingException(String message, boolean retryable) {
        super(message, retryable);
    }
    
    public PaymentProcessingException(String message, Throwable cause, boolean retryable) {
        super(message, cause, retryable);
    }
}