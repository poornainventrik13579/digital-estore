package com.inventrik.digitalestore.exception.payment;

public class PaymentException extends RuntimeException {
    
    private final boolean retryable;
    
    public PaymentException(String message) {
        super(message);
        this.retryable = false;
    }
    
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.retryable = false;
    }
    
    public PaymentException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }
    
    public PaymentException(String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.retryable = retryable;
    }
    
    public boolean isRetryable() {
        return retryable;
    }
}