package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadTokenResponse {
    private String token;
    private LocalDateTime expiryDate;
    private String downloadUrl;
    private Integer remainingDownloads; // -1 for unlimited
}