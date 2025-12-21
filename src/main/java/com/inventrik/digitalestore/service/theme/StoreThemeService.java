package com.inventrik.digitalestore.service.theme;

import com.inventrik.digitalestore.dto.request.StoreThemeRequest;
import com.inventrik.digitalestore.dto.response.StoreThemeResponse;

import java.util.List;

public interface StoreThemeService {
    List<StoreThemeResponse> getAllThemes(Integer tenantId);
    StoreThemeResponse getTheme(Integer tenantId, Long themeId);
    StoreThemeResponse createTheme(Integer tenantId, StoreThemeRequest request, String username);
    StoreThemeResponse updateTheme(Integer tenantId, Long themeId, StoreThemeRequest request, String username);
    void deleteTheme(Integer tenantId, Long themeId);
}
