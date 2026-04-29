package com.inventrik.digitalestore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.inventrik.digitalestore.util.HttpUtils;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    @Value("${rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${rate-limit.default.requests-per-minute:60}")
    private int defaultRequestsPerMinute;

    @Value("${rate-limit.auth.requests-per-minute:5}")
    private int authRequestsPerMinute;

    @Value("${rate-limit.payment.requests-per-minute:10}")
    private int paymentRequestsPerMinute;

    @Value("${rate-limit.admin.requests-per-minute:100}")
    private int adminRequestsPerMinute;

    private RateLimitInterceptor rateLimitInterceptorInstance;

    @Bean
    public RateLimitInterceptor rateLimitInterceptor() {
        if (rateLimitInterceptorInstance == null) {
            rateLimitInterceptorInstance = new RateLimitInterceptor(
                    rateLimitEnabled,
                    defaultRequestsPerMinute,
                    authRequestsPerMinute,
                    paymentRequestsPerMinute,
                    adminRequestsPerMinute);
        }
        return rateLimitInterceptorInstance;
    }

    @PreDestroy
    public void cleanup() {
        if (rateLimitInterceptorInstance != null) {
            rateLimitInterceptorInstance.shutdown();
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (rateLimitEnabled) {
            registry.addInterceptor(rateLimitInterceptor())
                    .addPathPatterns("/api/**");
        }
    }

    public static class RateLimitInterceptor implements org.springframework.web.servlet.HandlerInterceptor {

        private final boolean enabled;
        private final int defaultLimit;
        private final int authLimit;
        private final int paymentLimit;
        private final int adminLimit;

        // Store request counts per IP and endpoint type with thread-safe operations
        private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        public RateLimitInterceptor(boolean enabled, int defaultLimit, int authLimit, int paymentLimit,
                int adminLimit) {
            this.enabled = enabled;
            this.defaultLimit = defaultLimit;
            this.authLimit = authLimit;
            this.paymentLimit = paymentLimit;
            this.adminLimit = adminLimit;

            // Reset counters every minute
            scheduler.scheduleAtFixedRate(requestCounts::clear, 1, 1, TimeUnit.MINUTES);
        }

        public void shutdown() {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override
        public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response,
                Object handler) throws Exception {

            if (!enabled) {
                return true;
            }

            String clientIp = HttpUtils.getClientIpAddress(request);
            String requestUri = request.getRequestURI();
            String key = clientIp + ":" + getEndpointType(requestUri);

            // Thread-safe increment and get operation
            AtomicInteger count = requestCounts.computeIfAbsent(key, k -> new AtomicInteger(0));
            int currentCount = count.incrementAndGet();
            int limit = getLimit(requestUri);

            if (currentCount > limit) {
                response.setStatus(429); // Too Many Requests
                response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
                response.setHeader("X-RateLimit-Remaining", "0");
                response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000));
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Rate limit exceeded. Try again later.\"}");
                return false;
            }

            // Add rate limit headers
            response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - currentCount)));
            response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000));

            return true;
        }

        private String getEndpointType(String requestUri) {
            if (requestUri.contains("/auth/")) {
                return "auth";
            } else if (requestUri.contains("/payments/")) {
                return "payment";
            } else if (requestUri.contains("/admin/")) {
                return "admin";
            }
            return "default";
        }

        private int getLimit(String requestUri) {
            String endpointType = getEndpointType(requestUri);
            switch (endpointType) {
                case "auth":
                    return authLimit;
                case "payment":
                    return paymentLimit;
                case "admin":
                    return adminLimit;
                default:
                    return defaultLimit;
            }
        }
    }
}