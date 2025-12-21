package com.inventrik.digitalestore.service.theme;

import com.inventrik.digitalestore.domain.theme.StoreTheme;
import com.inventrik.digitalestore.dto.request.StoreThemeRequest;
import com.inventrik.digitalestore.dto.response.StoreThemeResponse;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.StoreThemeRepository;
import com.inventrik.digitalestore.repository.TenantRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreThemeServiceImpl implements StoreThemeService {

    private final StoreThemeRepository storeThemeRepository;
    private final TenantRepository tenantRepository;
    private final IdGeneratorService idGeneratorService;

    private StoreThemeResponse mapToDTO(StoreTheme theme) {
        return new StoreThemeResponse(
            theme.getTenantId(),
            theme.getThemeId(),
            theme.getThemeName(),
            theme.getTagline(),
            theme.getDescription(),
            theme.getBannerImage(),
            theme.getJoinCta(),
            theme.getCopyrightText(),
            theme.getHeroTitle(),
            theme.getHeroDescription(),
            theme.getStatus(),
            theme.getCreated(),
            theme.getUpdated()
        );
    }

    @Override
    public List<StoreThemeResponse> getAllThemes(Integer tenantId) {
        return storeThemeRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StoreThemeResponse getTheme(Integer tenantId, Long themeId) {
        StoreTheme theme = storeThemeRepository.findByTenantIdAndThemeId(tenantId, themeId)
                .orElseThrow(() -> new ResourceNotFoundException("Theme not found"));
        return mapToDTO(theme);
    }

    @Override
    @Transactional
    public StoreThemeResponse createTheme(Integer tenantId, StoreThemeRequest request, String username) {
        tenantRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

        StoreTheme theme = new StoreTheme();
        theme.setTenantId(tenantId);
        theme.setThemeId(idGeneratorService.generateId(tenantId, "THEME"));
        theme.setThemeName(request.getThemeName());
        theme.setTagline(request.getTagline());
        theme.setDescription(request.getDescription());
        theme.setBannerImage(request.getBannerImage());
        theme.setJoinCta(request.getJoinCta());
        theme.setCopyrightText(request.getCopyrightText());
        theme.setHeroTitle(request.getHeroTitle());
        theme.setHeroDescription(request.getHeroDescription());
        theme.setStatus("0");
        theme.setCreatedBy(username.substring(0, Math.min(2, username.length())));
        theme.setUpdatedBy(username.substring(0, Math.min(2, username.length())));

        StoreTheme saved = storeThemeRepository.save(theme);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public StoreThemeResponse updateTheme(Integer tenantId, Long themeId, StoreThemeRequest request, String username) {
        StoreTheme theme = storeThemeRepository.findByTenantIdAndThemeId(tenantId, themeId)
                .orElseThrow(() -> new ResourceNotFoundException("Theme not found"));

        theme.setThemeName(request.getThemeName());
        theme.setTagline(request.getTagline());
        theme.setDescription(request.getDescription());
        theme.setBannerImage(request.getBannerImage());
        theme.setJoinCta(request.getJoinCta());
        theme.setCopyrightText(request.getCopyrightText());

        theme.setHeroTitle(request.getHeroTitle());
        theme.setHeroDescription(request.getHeroDescription());
        
        theme.setUpdatedBy(username.substring(0, Math.min(2, username.length())));

        StoreTheme updated = storeThemeRepository.save(theme);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteTheme(Integer tenantId, Long themeId) {
        if (!storeThemeRepository.findByTenantIdAndThemeId(tenantId, themeId).isPresent()) {
            throw new ResourceNotFoundException("Theme not found");
        }
        storeThemeRepository.deleteByTenantIdAndThemeId(tenantId, themeId);
    }
}
