package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadHistoryResponse {
    private String downloadId;
    private String orderItemId;
    private LocalDateTime downloadDate;
    private String ipAddress;
    private String status;
}