package com.inventrik.digitalestore.dto.request;

import lombok.Data;

@Data
public class StoreThemeRequest {
    private String themeName;
    private String tagline;
    private String description;
    private String bannerImage;
    private String joinCta;
    private String copyrightText;
    private String heroTitle;
    private String heroDescription;
}
