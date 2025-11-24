
package com.inventrik.digitalestore.exception.payment;

public class PaymentNotFoundException extends PaymentException {
    
    public PaymentNotFoundException(String message) {
        super(message, false); 
    }
    
    public PaymentNotFoundException(String message, Throwable cause) {
        super(message, cause, false);
    }
}