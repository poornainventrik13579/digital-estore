package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreThemeResponse {
    
    private Integer tenantId;
    private Integer themeId;
    private String themeName;
    private String tagline;
    private String description;
    private String bannerImage;
    private String joinCta;
    private String copyrightText;
    private String status;
    private String createdBy;
    private LocalDateTime created;
    private String updatedBy;
    private LocalDateTime updated;
}
