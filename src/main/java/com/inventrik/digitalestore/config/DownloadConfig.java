package com.inventrik.digitalestore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.inventrik.digitalestore.service.download.DownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class DownloadConfig {
    
    private final DownloadService downloadService;
    
    @Value("${app.download.base-path:/app/downloads}")
    private String downloadBasePath;
    
    @Value("${app.download.token-expiry-hours:24}")
    private int tokenExpiryHours;
    
    @Value("${app.download.max-file-size:104857600}") // 100MB default
    private long maxFileSize;
    
    /**
     * Scheduled task to cleanup expired download tokens
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void cleanupExpiredTokens() {
        try {
            downloadService.cleanupExpiredTokens();
            log.debug("Completed cleanup of expired download tokens");
        } catch (Exception e) {
            log.error("Error during expired token cleanup: {}", e.getMessage(), e);
        }
    }
    
    public String getDownloadBasePath() {
        return downloadBasePath;
    }
    
    public int getTokenExpiryHours() {
        return tokenExpiryHours;
    }
    
    public long getMaxFileSize() {
        return maxFileSize;
    }
}