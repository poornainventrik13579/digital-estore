package com.inventrik.digitalestore;

import com.inventrik.digitalestore.util.TestUtils;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class SimpleEndpointTest {
    
    private static final String API_BASE_URL = "http://localhost:8080";
    private static final String API_URL = API_BASE_URL + "/api/v1";
    private static final int TENANT_ID = 1;
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Digital E-Store API Testing ===");
            
            System.out.println("\n1. Testing OAuth2 Authentication...");
            String accessToken = getOAuth2Token("web-client", "web-secret");
            System.out.println("✓ OAuth2 Authentication successful");
            
            System.out.println("\n2. Testing Categories Endpoint...");
            testCategoriesEndpoint(accessToken);
            
            System.out.println("\n3. Testing Products Endpoint...");
            testProductsEndpoint(accessToken);
            
            System.out.println("\n4. Testing Users Endpoint...");
            testUsersEndpoint(accessToken);
            
            System.out.println("\n5. Testing Orders Endpoint...");
            testOrdersEndpoint(accessToken);
            
            System.out.println("\n6. Testing Payments Endpoint...");
            testPaymentsEndpoint(accessToken);
            
            System.out.println("\n7. Testing Downloads Endpoint...");
            testDownloadsEndpoint(accessToken);
            
            System.out.println("\n8. Testing Basic Authentication...");
            testBasicAuth();
            
            System.out.println("\n=== All Tests Completed ===");
            
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
            return TestUtils.extractAccessToken(responseBody);
        } else {
            throw new RuntimeException("Failed to get OAuth2 token. Status: " + response.statusCode());
        }
    }
    
    private static void testCategoriesEndpoint(String accessToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/tenants/" + TENANT_ID + "/categories"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("GET /categories - Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }
    
    private static void testProductsEndpoint(String accessToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/tenants/" + TENANT_ID + "/products"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("GET /products - Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }
    
    private static void testUsersEndpoint(String accessToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/tenants/" + TENANT_ID + "/users"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("GET /users - Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }
    
    private static void testOrdersEndpoint(String accessToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/tenants/" + TENANT_ID + "/orders"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("GET /orders - Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }
    
    private static void testPaymentsEndpoint(String accessToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/tenants/" + TENANT_ID + "/payments"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("GET /payments - Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }
    
    private static void testDownloadsEndpoint(String accessToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/tenants/" + TENANT_ID + "/downloads"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("GET /downloads - Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }
    
    private static void testBasicAuth() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        String auth = "admin:admin";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        String authHeader = "Basic " + encodedAuth;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/tenants/" + TENANT_ID + "/products"))
                .header("Authorization", authHeader)
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Basic Auth Test - Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }

} 