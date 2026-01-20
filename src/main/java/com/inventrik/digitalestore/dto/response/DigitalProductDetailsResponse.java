package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalProductDetailsResponse {
    private String productId;
    private Integer tenantId;
    private String fileUrl;
    private Integer fileSize;
    private String fileFormat;
    private String licenseInfo;
    private String version;
    private String status;
    private LocalDateTime created;
    private LocalDateTime updated;
}