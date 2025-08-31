package com.inventrik.digitalestore.service.theme;

import com.inventrik.digitalestore.domain.theme.StoreTheme;
import com.inventrik.digitalestore.dto.request.StoreThemeRequest;
import com.inventrik.digitalestore.dto.request.StoreThemeUpdateRequest;
import com.inventrik.digitalestore.dto.response.StoreThemeResponse;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.StoreThemeRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreThemeServiceImpl implements StoreThemeService {
    
    private final StoreThemeRepository storeThemeRepository;
    private final IdGeneratorService idGeneratorService;
    
    private StoreThemeResponse mapToDTO(StoreTheme storeTheme) {
        return new StoreThemeResponse(
            storeTheme.getTenantId(),
            storeTheme.getThemeId(),
            storeTheme.getThemeName(),
            storeTheme.getTagline(),
            storeTheme.getDescription(),
            storeTheme.getBannerImage(),
            storeTheme.getJoinCta(),
            storeTheme.getCopyrightText(),
            storeTheme.getStatus(),
            storeTheme.getCreatedBy(),
            storeTheme.getCreated(),
            storeTheme.getUpdatedBy(),
            storeTheme.getUpdated()
        );
    }
    
    private StoreTheme mapToEntity(StoreThemeRequest request, String username) {
        StoreTheme storeTheme = new StoreTheme();
        storeTheme.setTenantId(request.getTenantId());
        storeTheme.setThemeId(idGeneratorService.generateTenantId());
        storeTheme.setThemeName(request.getThemeName());
        storeTheme.setTagline(request.getTagline());
        storeTheme.setDescription(request.getDescription());
        storeTheme.setBannerImage(request.getBannerImage());
        storeTheme.setJoinCta(request.getJoinCta());
        storeTheme.setCopyrightText(request.getCopyrightText());
        storeTheme.setStatus(request.getStatus());
        storeTheme.setCreatedBy(username);
        storeTheme.setUpdatedBy(username);
        return storeTheme;
    }
    
    private void updateEntityFromRequest(StoreTheme storeTheme, StoreThemeUpdateRequest request, String username) {
        if (request.getThemeName() != null) {
            storeTheme.setThemeName(request.getThemeName());
        }
        if (request.getTagline() != null) {
            storeTheme.setTagline(request.getTagline());
        }
        if (request.getDescription() != null) {
            storeTheme.setDescription(request.getDescription());
        }
        if (request.getBannerImage() != null) {
            storeTheme.setBannerImage(request.getBannerImage());
        }
        if (request.getJoinCta() != null) {
            storeTheme.setJoinCta(request.getJoinCta());
        }
        if (request.getCopyrightText() != null) {
            storeTheme.setCopyrightText(request.getCopyrightText());
        }
        if (request.getStatus() != null) {
            storeTheme.setStatus(request.getStatus());
        }
        storeTheme.setUpdatedBy(username);
        storeTheme.setUpdated(LocalDateTime.now());
    }
    
    @Override
    public List<StoreThemeResponse> getAllThemes() {
        return storeThemeRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public StoreThemeResponse getTheme(Integer tenantId, Integer themeId) {
        StoreTheme storeTheme = storeThemeRepository.findByTenantIdAndThemeId(tenantId, themeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store theme not found with tenant id: " + tenantId + " and theme id: " + themeId));
        return mapToDTO(storeTheme);
    }
    
    @Override
    public List<StoreThemeResponse> getThemesByTenant(Integer tenantId) {
        return storeThemeRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<StoreThemeResponse> getThemesByTenantAndStatus(Integer tenantId, String status) {
        return storeThemeRepository.findByTenantIdAndStatus(tenantId, status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public StoreThemeResponse createTheme(String username, StoreThemeRequest themeRequest) {
        StoreTheme storeTheme = mapToEntity(themeRequest, username);
        StoreTheme savedTheme = storeThemeRepository.save(storeTheme);
        return mapToDTO(savedTheme);
    }
    
    @Override
    @Transactional
    public StoreThemeResponse updateTheme(Integer tenantId, Integer themeId, String username, StoreThemeUpdateRequest updateRequest) {
        StoreTheme storeTheme = storeThemeRepository.findByTenantIdAndThemeId(tenantId, themeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store theme not found with tenant id: " + tenantId + " and theme id: " + themeId));
        
        updateEntityFromRequest(storeTheme, updateRequest, username);
        StoreTheme updatedTheme = storeThemeRepository.save(storeTheme);
        return mapToDTO(updatedTheme);
    }
    
    @Override
    @Transactional
    public void deleteTheme(Integer tenantId, Integer themeId) {
        StoreTheme storeTheme = storeThemeRepository.findByTenantIdAndThemeId(tenantId, themeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store theme not found with tenant id: " + tenantId + " and theme id: " + themeId));
        storeThemeRepository.delete(storeTheme);
    }
    
    @Override
    public List<StoreThemeResponse> getThemesByName(String themeName) {
        return storeThemeRepository.findByThemeName(themeName).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByTenantAndName(Integer tenantId, String themeName) {
        return storeThemeRepository.existsByTenantIdAndThemeName(tenantId, themeName);
    }
}
