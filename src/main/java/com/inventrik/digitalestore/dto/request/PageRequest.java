package com.inventrik.digitalestore.dto.request;

import com.inventrik.digitalestore.domain.page.PageStatus;
import com.inventrik.digitalestore.domain.page.PageVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    
    @NotNull(message = "Tenant ID is required")
    private Integer tenantId;
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @NotBlank(message = "Slug is required")
    @Size(max = 100, message = "Slug must not exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    private String slug;
    
    private String content;
    
    @Size(max = 256, message = "Meta title must not exceed 256 characters")
    private String metaTitle;
    
    @Size(max = 256, message = "Meta description must not exceed 256 characters")
    private String metaDescription;
    
    private PageStatus status = PageStatus.DRAFT;
    
    private PageVisibility visibility = PageVisibility.PUBLIC;
    
    private Boolean isDefault = false;
    
    @Size(max = 10, message = "Language code must not exceed 10 characters")
    @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "Language must be in format 'en' or 'en-US'")
    private String language = "en";
}
