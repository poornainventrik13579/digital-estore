package com.inventrik.digitalestore.service.payment;

import com.inventrik.digitalestore.exception.payment.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

@Service
@Slf4j
public class PaymentRetryService {

    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final Duration DEFAULT_INITIAL_DELAY = Duration.ofSeconds(1);
    private static final double DEFAULT_BACKOFF_MULTIPLIER = 2.0;

    public <T> T executeWithRetry(Callable<T> operation) throws PaymentException {
        return executeWithRetry(operation, DEFAULT_MAX_RETRIES, DEFAULT_INITIAL_DELAY, 
                DEFAULT_BACKOFF_MULTIPLIER, ex -> ex instanceof PaymentException && ((PaymentException) ex).isRetryable());
    }

    public <T> T executeWithRetry(Callable<T> operation, int maxRetries, Duration initialDelay, 
                                double backoffMultiplier, Predicate<Throwable> retryPredicate) throws PaymentException {
        int attempts = 0;
        Duration delay = initialDelay;
        Throwable lastException = null;

        while (attempts <= maxRetries) {
            try {
                if (attempts > 0) {
                    log.info("Retry attempt {} for payment operation", attempts);
                }
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (!retryPredicate.test(e) || attempts >= maxRetries) {
                    break;
                }
                
                log.warn("Payment operation failed, will retry in {} ms. Attempt: {}/{}. Error: {}", 
                        delay.toMillis(), attempts + 1, maxRetries, e.getMessage());
                
                try {
                    
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("Payment retry was interrupted");
                    throw new PaymentException("Payment retry interrupted", ie, false);
                }
                
                delay = Duration.ofMillis((long) (delay.toMillis() * backoffMultiplier));
                attempts++;
            }
        }
        
        if (lastException instanceof PaymentException) {
            throw (PaymentException) lastException;
        } else {
            throw new PaymentException("Payment operation failed after " + attempts + " retries", 
                    lastException, false);
        }
    }
}