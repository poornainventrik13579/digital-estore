package com.inventrik.digitalestore.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {
    
    @Value("${stripe.api.key}")
    private String stripeApiKey;
    
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${stripe.checkout.success-url:http://localhost:4200/payment-success?session_id={CHECKOUT_SESSION_ID}}")
    private String checkoutSuccessUrl;

    @Value("${stripe.checkout.cancel-url:http://localhost:4200/pay?cancelled=true}")
    private String checkoutCancelUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public String getCheckoutSuccessUrl() {
        return checkoutSuccessUrl;
    }

    public String getCheckoutCancelUrl() {
        return checkoutCancelUrl;
    }
}