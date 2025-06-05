package com.inventrik.digitalestore.exception.download;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DownloadException extends RuntimeException {
    
    public DownloadException(String message) {
        super(message);
    }
    
    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}