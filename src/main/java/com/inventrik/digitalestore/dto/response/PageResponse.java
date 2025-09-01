package com.inventrik.digitalestore.dto.response;

import com.inventrik.digitalestore.domain.page.PageStatus;
import com.inventrik.digitalestore.domain.page.PageVisibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse {
    
    private Long id;
    private Integer tenantId;
    private String title;
    private String slug;
    private String content;
    private String metaTitle;
    private String metaDescription;
    private PageStatus status;
    private PageVisibility visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private Boolean isDefault;
    private String language;
    private String createdBy;
    private String updatedBy;
}
