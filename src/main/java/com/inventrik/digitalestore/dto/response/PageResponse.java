package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PageResponse {
    private Integer tenantId;
    private String pageId;
    private String title;
    private String slug;
    private String content;
    private String metaTitle;
    private String metaDescription;
    private String template;
    private String status;
    private String visibility;
    private Boolean isDefault;
    private String language;
    private LocalDateTime created;
    private LocalDateTime updated;
    private LocalDateTime publishedAt;
}
