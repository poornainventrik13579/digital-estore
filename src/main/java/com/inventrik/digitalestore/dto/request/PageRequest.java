package com.inventrik.digitalestore.dto.request;

import com.inventrik.digitalestore.domain.page.PageStatus;
import com.inventrik.digitalestore.domain.page.PageVisibility;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class PageRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Slug is required")
    private String slug;

    private String content;
    private String metaTitle;
    private String metaDescription;
    private String template;
    private PageStatus status;
    private PageVisibility visibility;
    private Boolean isDefault;
    private String language;
}
