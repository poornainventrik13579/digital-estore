import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class AuthenticationTest {
    
    private static final String API_BASE_URL = "http://localhost:8080";
    private static final String OAUTH2_TOKEN_URL = API_BASE_URL + "/oauth2/token";
    private static final int TENANT_ID = 1;
    
    public static void main(String[] args) {
        try {
            String accessToken = getOAuth2Token("web-client", "web-secret");
            System.out.println("OAuth2 authentication successful. Access Token: " + accessToken);
            
            testAPIWithToken(accessToken);
        } catch (Exception e) {
            System.err.println("Authentication failed: " + e.getMessage());
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
                .uri(URI.create(OAUTH2_TOKEN_URL))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            String responseBody = response.body();
            String accessToken = extractAccessToken(responseBody);
            return accessToken;
        } else {
            throw new RuntimeException("Failed to get OAuth2 token. Status: " + response.statusCode() + 
                    ", body: " + response.body());
        }
    }
    
    private static void testAPIWithToken(String accessToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/api/v1/tenants/" + TENANT_ID + "/products"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            System.out.println("API call successful: " + response.body());
        } else {
            System.err.println("API call failed. Status: " + response.statusCode() + 
                    ", body: " + response.body());
        }
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