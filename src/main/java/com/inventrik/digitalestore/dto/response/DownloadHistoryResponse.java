package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadHistoryResponse {
    private Long downloadId;
    private Long orderItemId;
    private Long productId;
    private LocalDateTime downloadDate;
    private String ipAddress;
    private String downloadStatus;
    private Long fileSizeDownloaded;
    private String status;
}