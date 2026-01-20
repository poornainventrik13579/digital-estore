package com.inventrik.digitalestore.service.theme;

import com.inventrik.digitalestore.dto.request.StoreThemeRequest;
import com.inventrik.digitalestore.dto.response.StoreThemeResponse;

import java.util.List;

public interface StoreThemeService {
    List<StoreThemeResponse> getAllThemes(Integer tenantId);
    StoreThemeResponse getTheme(Integer tenantId, String themeId);
    StoreThemeResponse createTheme(Integer tenantId, StoreThemeRequest request, String username);
    StoreThemeResponse updateTheme(Integer tenantId, String themeId, StoreThemeRequest request, String username);
    void deleteTheme(Integer tenantId, String themeId);
}
