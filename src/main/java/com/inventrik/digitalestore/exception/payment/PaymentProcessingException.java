package com.inventrik.digitalestore.exception.payment;

public class PaymentProcessingException extends PaymentException {
    
    public PaymentProcessingException(String message) {
        super(message, true); 
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