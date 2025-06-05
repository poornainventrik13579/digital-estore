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
    private LocalDateTime downloadDate;
    private String ipAddress;
    private String status;
}