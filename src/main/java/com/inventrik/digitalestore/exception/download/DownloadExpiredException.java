package com.inventrik.digitalestore.exception.download;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class DownloadExpiredException extends DownloadException {
    
    public DownloadExpiredException(String message) {
        super(message);
    }
    
    public DownloadExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}