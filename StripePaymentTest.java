import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentMethodCreateParams;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Tests direct Stripe API interactions without using our app.
 */
public class StripePaymentTest {
    
    private static final String DEFAULT_API_KEY = "sk_test_your_test_key"; // Fallback key
    
    public static void main(String[] args) {
        // Load API key from properties
        String apiKey = loadStripeApiKey();
        Stripe.apiKey = apiKey;
        
        try {
            // Step 1: Create a payment method (normally done by Stripe.js)
            PaymentMethod paymentMethod = createTestPaymentMethod();
            System.out.println("Payment method created: " + paymentMethod.getId());
            
            // Step 2: Create a payment intent
            PaymentIntent paymentIntent = createPaymentIntent(paymentMethod.getId(), "USD", 1000); // $10.00
            System.out.println("Payment intent created: " + paymentIntent.getId());
            System.out.println("Client secret: " + paymentIntent.getClientSecret());
            
            // Step 3: Confirm the payment
            PaymentIntent confirmedIntent = confirmPaymentIntent(paymentIntent.getId());
            System.out.println("Payment intent confirmed. Status: " + confirmedIntent.getStatus());
            
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
            
            if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your_stripe_test_key")) {
                System.out.println("Successfully loaded API key from properties");
                return apiKey;
            }
            
            // If key not found or is the default placeholder value, try loading from environment
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
    
    private static PaymentMethod createTestPaymentMethod() throws StripeException {
        PaymentMethodCreateParams params = PaymentMethodCreateParams.builder()
                .setType(PaymentMethodCreateParams.Type.CARD)
                .setCard(PaymentMethodCreateParams.CardDetails.builder()
                        .setNumber("4242424242424242") // Test card number
                        .setExpMonth(12L)
                        .setExpYear(2030L)
                        .setCvc("123")
                        .build())
                .build();
        
        return PaymentMethod.create(params);
    }
    
    private static PaymentIntent createPaymentIntent(String paymentMethodId, String currency, int amount) 
            throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount);
        params.put("currency", currency.toLowerCase());
        params.put("payment_method", paymentMethodId);
        params.put("confirmation_method", "manual");
        params.put("confirm", false);
        
        return PaymentIntent.create(params);
    }
    
    private static PaymentIntent confirmPaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
        Map<String, Object> params = new HashMap<>();
        params.put("payment_method", "pm_card_visa");
        
        return intent.confirm(params);
    }
}