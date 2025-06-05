package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalProductDetailsResponse {
    private Long productId;
    private Integer tenantId;
    private String fileUrl;
    private Long fileSize;
    private String fileFormat;
    private String licenseInfo;
    private String version;
    private Integer downloadLimit;
    private Integer expiryDays;
    private String fileHash;
    private String status;
    private LocalDateTime created;
    private LocalDateTime updated;
}