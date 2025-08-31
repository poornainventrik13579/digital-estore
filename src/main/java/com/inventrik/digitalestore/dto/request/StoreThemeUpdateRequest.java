package com.inventrik.digitalestore.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreThemeUpdateRequest {
    
    @Size(max = 100, message = "Theme name must not exceed 100 characters")
    private String themeName;
    
    @Size(max = 256, message = "Tagline must not exceed 256 characters")
    private String tagline;
    
    @Size(max = 256, message = "Description must not exceed 256 characters")
    private String description;
    
    @Size(max = 256, message = "Banner image URL must not exceed 256 characters")
    private String bannerImage;
    
    @Size(max = 256, message = "Join CTA must not exceed 256 characters")
    private String joinCta;
    
    @Size(max = 256, message = "Copyright text must not exceed 256 characters")
    private String copyrightText;
    
    @Size(max = 2, message = "Status must not exceed 2 characters")
    private String status;
}
