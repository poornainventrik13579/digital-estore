package com.inventrik.digitalestore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.WebhookEndpoint;
import com.stripe.param.PaymentIntentCreateParams;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Test for Stripe webhook handling
 */
public class WebhookHandlerTest {
    private static final String DEFAULT_API_KEY = "sk_test_your_test_key";
    private static final String DEFAULT_WEBHOOK_SECRET = "whsec_your_webhook_secret";
    private static final String API_BASE_URL = "http://localhost:8080/api/webhooks/stripe";
    
    private static final HttpClient client = HttpClient.newHttpClient();
    
    public static void main(String[] args) {
        try {
            // Load API key and webhook secret
            String apiKey = loadStripeApiKey();
            String webhookSecret = loadWebhookSecret();
            
            Stripe.apiKey = apiKey;
            
            // Step 1: Create a test payment intent
            PaymentIntent intent = createTestPaymentIntent();
            System.out.println("Created payment intent: " + intent.getId());
            
            // Step 2: Create a simulated webhook event
            String payload = createWebhookPayload(intent.getId());
            System.out.println("Created webhook payload");
            
            // Step 3: Calculate webhook signature
            String signature = calculateSignature(payload, webhookSecret);
            System.out.println("Calculated signature: " + signature);
            
            // Step 4: Send webhook request to application
            HttpResponse<String> response = sendWebhookRequest(payload, signature);
            System.out.println("Webhook response status: " + response.statusCode());
            System.out.println("Webhook response body: " + response.body());
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
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
            
            if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your_stripe_test_key")) {
                System.out.println("Successfully loaded API key from properties");
                return apiKey;
            }
            
            // If key not found or is default placeholder, try environment
            apiKey = System.getenv("STRIPE_API_KEY");
            if (apiKey != null && !apiKey.isEmpty()) {
                System.out.println("Successfully loaded API key from environment");
                return apiKey;
            }
            
            System.out.println("Warning: Using default API key. Set a valid key in application.properties");
            return DEFAULT_API_KEY;
        } catch (IOException e) {
            System.out.println("Could not load properties file: " + e.getMessage());
            System.out.println("Using default API key");
            return DEFAULT_API_KEY;
        }
    }
    
    private static String loadWebhookSecret() {
        Properties props = new Properties();
        try {
            // Try loading from application.properties
            InputStream input = new FileInputStream("src/main/resources/application.properties");
            props.load(input);
            String secret = props.getProperty("stripe.webhook.secret");
            
            if (secret != null && !secret.isEmpty() && !secret.equals("your_webhook_secret")) {
                System.out.println("Successfully loaded webhook secret from properties");
                return secret;
            }
            
            // Try environment variable
            secret = System.getenv("STRIPE_WEBHOOK_SECRET");
            if (secret != null && !secret.isEmpty()) {
                System.out.println("Successfully loaded webhook secret from environment");
                return secret;
            }
            
            System.out.println("Warning: Using default webhook secret. Set a valid secret in application.properties");
            return DEFAULT_WEBHOOK_SECRET;
        } catch (IOException e) {
            System.out.println("Could not load properties file: " + e.getMessage());
            System.out.println("Using default webhook secret");
            return DEFAULT_WEBHOOK_SECRET;
        }
    }
    
    private static PaymentIntent createTestPaymentIntent() throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setCurrency("usd")
                .setAmount(1000L) // $10.00
                .setPaymentMethod("pm_card_visa") // Test payment method
                .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
                .build();
        
        return PaymentIntent.create(params);
    }
    
    private static String createWebhookPayload(String paymentIntentId) throws StripeException {
        // Retrieve the payment intent to ensure we have the latest data
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
        
        // Create a mock event payload
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", "evt_" + System.currentTimeMillis());
        eventData.put("object", "event");
        eventData.put("api_version", "2020-08-27");
        eventData.put("created", System.currentTimeMillis() / 1000);
        eventData.put("type", "payment_intent.succeeded");
        
        Map<String, Object> dataObject = new HashMap<>();
        dataObject.put("object", intent);
        
        Map<String, Object> data = new HashMap<>();
        data.put("object", dataObject);
        
        eventData.put("data", data);
        
        try {
            return new ObjectMapper().writeValueAsString(eventData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create webhook payload", e);
        }
    }
    
    private static String calculateSignature(String payload, String secret) {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String signedPayload = timestamp + "." + payload;
            
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secretKey);
            byte[] hash = sha256_HMAC.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return "t=" + timestamp + ",v1=" + hexString.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to calculate signature", e);
        }
    }
    
    private static HttpResponse<String> sendWebhookRequest(String payload, String signature) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL))
                .header("Content-Type", "application/json")
                .header("Stripe-Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}