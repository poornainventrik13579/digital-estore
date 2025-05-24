// src/main/java/com/inventrik/digitalestore/exception/payment/PaymentNotFoundException.java
package com.inventrik.digitalestore.exception.payment;

public class PaymentNotFoundException extends PaymentException {
    
    public PaymentNotFoundException(String message) {
        super(message, false); // Payment not found errors are not retryable
    }
    
    public PaymentNotFoundException(String message, Throwable cause) {
        super(message, cause, false);
    }
}