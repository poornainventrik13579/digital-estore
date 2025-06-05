package com.inventrik.digitalestore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class DownloadConfig {
    
    @Value("${app.download.base-path:/app/downloads}")
    private String downloadBasePath;
    
    @Value("${app.download.token-expiry-hours:24}")
    private int tokenExpiryHours;
    
    @Value("${app.download.max-file-size:104857600}")
    private long maxFileSize;
}