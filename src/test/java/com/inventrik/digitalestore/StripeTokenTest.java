package com.inventrik.digitalestore;

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
        String apiKey = loadStripeApiKey();
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
    
    private static String loadStripeApiKey() {
        Properties props = new Properties();
        try {
            // Try loading from application.properties
            InputStream input = new FileInputStream("src/main/resources/application.properties");
            props.load(input);
            String apiKey = props.getProperty("stripe.api.key");
            
            if (apiKey != null && !apiKey.isEmpty()) {
                System.out.println("Successfully loaded API key from properties");
                return apiKey;
            }
            
            // Try environment variable
            apiKey = System.getenv("STRIPE_API_KEY");
            if (apiKey != null && !apiKey.isEmpty()) {
                System.out.println("Successfully loaded API key from environment");
                return apiKey;
            }
            
            System.out.println("Warning: No API key found");
            return "YOUR_STRIPE_TEST_KEY";
        } catch (IOException e) {
            System.out.println("Could not load properties file: " + e.getMessage());
            System.out.println("Using environment variable instead");
            String apiKey = System.getenv("STRIPE_API_KEY");
            return apiKey != null ? apiKey : "YOUR_STRIPE_TEST_KEY";
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