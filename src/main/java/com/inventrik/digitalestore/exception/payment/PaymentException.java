package com.inventrik.digitalestore.exception.payment;

/**
 * Base exception class for all payment-related exceptions.
 */
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
    
    /**
     * Indicates whether this exception represents a condition that may be resolved
     * by retrying the operation.
     *
     * @return true if the operation can be retried, false otherwise
     */
    public boolean isRetryable() {
        return retryable;
    }
}