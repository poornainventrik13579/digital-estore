package com.inventrik.digitalestore.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for test-related operations
 */
public class TestUtils {

    private TestUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Extract access token from authentication response body
     * @param responseBody the JSON response body from authentication
     * @return the access token
     */
    public static String extractAccessToken(String responseBody) {
        String searchStr = "\"access_token\":\"";
        int startIndex = responseBody.indexOf(searchStr);
        if (startIndex == -1) {
            throw new RuntimeException("access_token not found in response");
        }
        startIndex += searchStr.length();
        int endIndex = responseBody.indexOf("\"", startIndex);
        if (endIndex == -1) {
            throw new RuntimeException("Invalid access_token format in response");
        }
        return responseBody.substring(startIndex, endIndex);
    }

    /**
     * Load Stripe API key from application.properties or environment variable
     * @return the Stripe API key
     */
    public static String loadStripeApiKey() {
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
            input.close();
        } catch (Exception e) {
            System.out.println("Could not load from properties: " + e.getMessage());
        }
        
        // Try environment variable as fallback
        String envApiKey = System.getenv("STRIPE_TEST_KEY");
        if (envApiKey != null && !envApiKey.isEmpty()) {
            System.out.println("Successfully loaded API key from environment variable");
            return envApiKey;
        }
        
        throw new RuntimeException("Stripe API key not found. Please set 'stripe.api.key' in application.properties or 'STRIPE_TEST_KEY' environment variable");
    }
} 