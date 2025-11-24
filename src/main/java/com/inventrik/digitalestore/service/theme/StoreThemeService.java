package com.inventrik.digitalestore.service.theme;

import com.inventrik.digitalestore.dto.request.StoreThemeRequest;
import com.inventrik.digitalestore.dto.request.StoreThemeUpdateRequest;
import com.inventrik.digitalestore.dto.response.StoreThemeResponse;

import java.util.List;

public interface StoreThemeService {
    
    List<StoreThemeResponse> getAllThemes();
    StoreThemeResponse getTheme(Integer tenantId, Integer themeId);
    List<StoreThemeResponse> getThemesByTenant(Integer tenantId);
    List<StoreThemeResponse> getThemesByTenantAndStatus(Integer tenantId, String status);
    StoreThemeResponse createTheme(String username, StoreThemeRequest themeRequest);
    StoreThemeResponse updateTheme(Integer tenantId, Integer themeId, String username, StoreThemeUpdateRequest updateRequest);
    void deleteTheme(Integer tenantId, Integer themeId);
    List<StoreThemeResponse> getThemesByName(Integer tenantId, String themeName);
    List<StoreThemeResponse> getAllThemesByName(String themeName);
    boolean existsByTenantAndName(Integer tenantId, String themeName);
}
