package com.inventrik.digitalestore.exception.download;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class DownloadLimitExceededException extends DownloadException {
    
    public DownloadLimitExceededException(String message) {
        super(message);
    }
    
    public DownloadLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}