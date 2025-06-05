package com.inventrik.digitalestore.exception.payment;

public class InsufficientRefundAmountException extends PaymentException {
    
    public InsufficientRefundAmountException(String message) {
        super(message, false);
    }
    
    public InsufficientRefundAmountException(String message, Throwable cause) {
        super(message, cause, false);
    }
} 