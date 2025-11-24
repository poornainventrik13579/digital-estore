package com.inventrik.digitalestore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentMethodCreateParams;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class PaymentApiIntegrationTest {
    
    private static final String API_BASE_URL = "http://localhost:8080/api/v1";
    private static final int TENANT_ID = 1;
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String STRIPE_API_KEY = "sk_test_your_test_key";
    
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static void main(String[] args) {
        try {
            Stripe.apiKey = STRIPE_API_KEY;
            
            String authHeader = getBasicAuthHeader(USERNAME, PASSWORD);
            
            Long orderId = createOrder(authHeader);
            System.out.println("Created order with ID: " + orderId);
            
            PaymentMethod paymentMethod = createTestPaymentMethod();
            System.out.println("Created payment method with ID: " + paymentMethod.getId());
            
            Map<String, Object> paymentResponse = createPayment(authHeader, orderId, paymentMethod.getId());
            Long paymentId = ((Number) paymentResponse.get("paymentId")).longValue();
            String clientSecret = (String) paymentResponse.get("clientSecret");
            System.out.println("Created payment with ID: " + paymentId);
            System.out.println("Client secret: " + clientSecret);
            
            confirmPaymentWithStripe(clientSecret);
            
            Map<String, Object> confirmedPayment = confirmPayment(authHeader, paymentId, 
                    (String) paymentResponse.get("transactionId"));
            System.out.println("Payment confirmed. Status: " + confirmedPayment.get("status"));
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String getBasicAuthHeader(String username, String password) {
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        return "Basic " + encodedAuth;
    }
    
    private static Long createOrder(String authHeader) throws Exception {
        
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("userId", 1L);
        orderRequest.put("currency", "USD");
        orderRequest.put("totalAmount", 49.99);
        orderRequest.put("exchangeRate", 1.0);
        
        Map<String, Object> orderItem = new HashMap<>();
        orderItem.put("productId", 1L);
        orderItem.put("priceAtPurchase", 49.99);
        orderItem.put("licenseKey", "TEST-LICENSE-123");
        
        orderRequest.put("orderItems", new Object[]{orderItem});
        
        String requestBody = objectMapper.writeValueAsString(orderRequest);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/tenants/" + TENANT_ID + "/orders"))
                .header("Content-Type", "application/json")
                .header("Authorization", authHeader)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
            return ((Number) responseMap.get("orderId")).longValue();
        } else {
            throw new RuntimeException("Failed to create order. Status: " + response.statusCode() + 
                    ", body: " + response.body());
        }
    }
    
    private static PaymentMethod createTestPaymentMethod() throws StripeException {
        PaymentMethodCreateParams params = PaymentMethodCreateParams.builder()
                .setType(PaymentMethodCreateParams.Type.CARD)
                .setCard(PaymentMethodCreateParams.CardDetails.builder()
                        .setNumber("4242424242424242") 
                        .setExpMonth(12L)
                        .setExpYear(2030L)
                        .setCvc("123")
                        .build())
                .build();
        
        return PaymentMethod.create(params);
    }
    
    private static Map<String, Object> createPayment(String authHeader, Long orderId, String paymentMethodId) 
            throws Exception {
        
        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("orderId", orderId);
        paymentRequest.put("currency", "USD");
        paymentRequest.put("amount", 49.99);
        paymentRequest.put("paymentMethod", "Credit Card");
        paymentRequest.put("paymentToken", paymentMethodId);
        
        String requestBody = objectMapper.writeValueAsString(paymentRequest);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/tenants/" + TENANT_ID + "/payments"))
                .header("Content-Type", "application/json")
                .header("Authorization", authHeader)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), Map.class);
        } else {
            throw new RuntimeException("Failed to create payment. Status: " + response.statusCode() + 
                    ", body: " + response.body());
        }
    }
    
    private static void confirmPaymentWithStripe(String clientSecret) throws StripeException {
        
        String paymentIntentId = clientSecret.split("_secret_")[0];
        
        com.stripe.model.PaymentIntent intent = 
                com.stripe.model.PaymentIntent.retrieve(paymentIntentId);
        
        Map<String, Object> params = new HashMap<>();
        params.put("payment_method", "pm_card_visa"); 
        
        intent.confirm(params);
    }
    
    private static Map<String, Object> confirmPayment(String authHeader, Long paymentId, String transactionId) 
            throws Exception {
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/tenants/" + TENANT_ID + "/payments/" + 
                        paymentId + "/confirm?transactionId=" + transactionId))
                .header("Authorization", authHeader)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), Map.class);
        } else {
            throw new RuntimeException("Failed to confirm payment. Status: " + response.statusCode() + 
                    ", body: " + response.body());
        }
    }
}