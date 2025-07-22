import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class PostEndpointTest {
    
    private static final String API_BASE_URL = "http://localhost:8080";
    private static final String API_URL = API_BASE_URL + "/api/v1";
    private static final int TENANT_ID = 1;
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Digital E-Store POST Operations Testing ===");
            
            // Get OAuth2 token
            String accessToken = getOAuth2Token("web-client", "web-secret");
            System.out.println("✓ OAuth2 Authentication successful");
            
            // Test POST operations
            System.out.println("\n1. Testing POST Category...");
            testCreateCategory(accessToken);
            
            System.out.println("\n2. Testing POST Product...");
            testCreateProduct(accessToken);
            
            System.out.println("\n3. Testing POST User...");
            testCreateUser(accessToken);
            
            System.out.println("\n4. Testing POST Order...");
            testCreateOrder(accessToken);
            
            System.out.println("\n5. Testing Webhook Endpoint...");
            testWebhookEndpoint();
            
            System.out.println("\n=== POST Tests Completed ===");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String getOAuth2Token(String clientId, String clientSecret) throws Exception {
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        String authHeader = "Basic " + encodedAuth;
        
        String requestBody = "grant_type=client_credentials&scope=read write";
        
        HttpClient client = HttpClient.newHttpClient();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/oauth2/token"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            String responseBody = response.body();
            return extractAccessToken(responseBody);
        } else {
            throw new RuntimeException("Failed to get OAuth2 token. Status: " + response.statusCode());
        }
    }
    
    private static void testCreateCategory(String accessToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        String requestBody = "{\"categoryName\":\"Test Software\",\"description\":\"Test software category\"}";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/tenants/" + TENANT_ID + "/categories"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("POST /categories - Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }
    
    private static void testCreateProduct(String accessToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        String requestBody = "{\"productName\":\"Test Antivirus\",\"description\":\"Test antivirus software\",\"defaultPrice\":29.99,\"defaultCurrency\":\"USD\",\"categoryId\":1}";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/tenants/" + TENANT_ID + "/products"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("POST /products - Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }
    
    private static void testCreateUser(String accessToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        String requestBody = "{\"username\":\"testuser\",\"email\":\"test@example.com\",\"firstName\":\"Test\",\"lastName\":\"User\",\"phone\":\"1234567890\"}";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/tenants/" + TENANT_ID + "/users"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("POST /users - Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }
    
    private static void testCreateOrder(String accessToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        String requestBody = "{\"userId\":1,\"currency\":\"USD\",\"totalAmount\":29.99,\"exchangeRate\":1.0,\"orderItems\":[{\"productId\":1,\"priceAtPurchase\":29.99,\"licenseKey\":\"TEST-LICENSE-123\"}]}";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/tenants/" + TENANT_ID + "/orders"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("POST /orders - Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }
    
    private static void testWebhookEndpoint() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        String requestBody = "{\"id\":\"evt_test_webhook\",\"object\":\"event\",\"type\":\"payment_intent.succeeded\",\"data\":{\"object\":{\"id\":\"pi_test_payment\",\"amount\":2999,\"currency\":\"usd\",\"status\":\"succeeded\"}}}";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/api/webhooks/stripe"))
                .header("Content-Type", "application/json")
                .header("Stripe-Signature", "t=1234567890,v1=test_signature")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("POST /webhooks/stripe - Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }
    
    private static String extractAccessToken(String responseBody) {
        String searchStr = "\"access_token\":\"";
        int startIndex = responseBody.indexOf(searchStr);
        if (startIndex == -1) {
            throw new RuntimeException("access_token not found in response");
        }
        startIndex += searchStr.length();
        int endIndex = responseBody.indexOf("\"", startIndex);
        if (endIndex == -1) {
            throw new RuntimeException("access_token end not found in response");
        }
        return responseBody.substring(startIndex, endIndex);
    }
} 