package com.inventrik.digitalestore.dto.response;

import com.inventrik.digitalestore.domain.page.PageStatus;
import com.inventrik.digitalestore.domain.page.PageVisibility;
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
    private PageStatus status;
    private PageVisibility visibility;
    private Boolean isDefault;
    private String language;
    private LocalDateTime created;
    private LocalDateTime updated;
    private LocalDateTime publishedAt;
}
