package com.inventrik.digitalestore;

import com.inventrik.digitalestore.util.TestUtils;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class StripeTokenTest {
    
    public static void main(String[] args) {
        // Load API key from properties
                    String apiKey = TestUtils.loadStripeApiKey();
        Stripe.apiKey = apiKey;
        
        try {
            PaymentIntent intent = createPaymentIntent("pm_card_visa", "usd", 1999);
            System.out.println("Created payment intent: " + intent.getId());
            System.out.println("Status: " + intent.getStatus());
        } catch (StripeException e) {
            System.err.println("Stripe error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    private static PaymentIntent createPaymentIntent(String paymentMethod, String currency, int amount) 
            throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setCurrency(currency)
            .setAmount((long)amount)
            .setPaymentMethod(paymentMethod)
            .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
            .build();
        
        return PaymentIntent.create(params);
    }
}