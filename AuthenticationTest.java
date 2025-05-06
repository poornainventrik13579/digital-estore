import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class AuthenticationTest {
    
    private static final String API_BASE_URL = "http://localhost:8080/api/v1";
    private static final int TENANT_ID = 1;
    
    public static void main(String[] args) {
        try {
            String authToken = authenticateUser("admin", "admin");
            System.out.println("Authentication successful. Token: " + authToken);
        } catch (Exception e) {
            System.err.println("Authentication failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String authenticateUser(String username, String password) throws Exception {
        // Create basic auth header
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        String authHeader = "Basic " + encodedAuth;
        
        // Create HTTP client
        HttpClient client = HttpClient.newHttpClient();
        
        // Create request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/tenants/" + TENANT_ID + "/products"))
                .header("Authorization", authHeader)
                .GET()
                .build();
        
        // Send request
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Check response
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return authHeader; // Return auth header for reuse
        } else {
            throw new RuntimeException("Failed with status code: " + response.statusCode() + 
                    ", body: " + response.body());
        }
    }
}