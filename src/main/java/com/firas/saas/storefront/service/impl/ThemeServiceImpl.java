package com.firas.saas.storefront.service.impl;

import com.firas.saas.common.exception.ResourceNotFoundException;
import com.firas.saas.storefront.dto.ThemeResponse;
import com.firas.saas.storefront.entity.Theme;
import com.firas.saas.storefront.repository.StoreSettingsRepository;
import com.firas.saas.storefront.repository.ThemeRepository;
import com.firas.saas.storefront.service.ThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThemeServiceImpl implements ThemeService {

    private final ThemeRepository themeRepository;
    private final StoreSettingsRepository storeSettingsRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ThemeResponse> getActiveThemes() {
        return themeRepository.findByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ThemeResponse getThemeById(Long themeId) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("Theme", themeId));
        return mapToResponse(theme);
    }

    @Override
    @Transactional(readOnly = true)
    public Theme getThemeEntityById(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("Theme", themeId));
    }

    @Override
    @Transactional
    public ThemeResponse createTheme(String name, String description, Map<String, Object> defaultLayoutsJson,
                                      Map<String, Object> cssVariables, String previewImageUrl) {
        if (themeRepository.existsByName(name)) {
            throw new IllegalArgumentException("Theme with name '" + name + "' already exists");
        }

        Theme theme = Theme.builder()
                .name(name)
                .description(description)
                .defaultLayoutsJson(defaultLayoutsJson)
                .cssVariables(cssVariables)
                .previewImageUrl(previewImageUrl)
                .active(true)
                .displayOrder(0)
                .build();

        return mapToResponse(themeRepository.save(theme));
    }

    @Override
    @Transactional
    public ThemeResponse updateTheme(Long themeId, String name, String description,
                                      Map<String, Object> cssVariables, String previewImageUrl) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("Theme", themeId));

        if (name != null && !name.equals(theme.getName()) && themeRepository.existsByName(name)) {
            throw new IllegalArgumentException("Theme with name '" + name + "' already exists");
        }

        if (name != null) theme.setName(name);
        if (description != null) theme.setDescription(description);
        if (cssVariables != null) theme.setCssVariables(cssVariables);
        if (previewImageUrl != null) theme.setPreviewImageUrl(previewImageUrl);

        return mapToResponse(themeRepository.save(theme));
    }

    @Override
    @Transactional
    public ThemeResponse setThemeActive(Long themeId, boolean active) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("Theme", themeId));

        theme.setActive(active);
        return mapToResponse(themeRepository.save(theme));
    }

    @Override
    @Transactional
    public void deleteTheme(Long themeId) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("Theme", themeId));

        // Check if theme is in use
        // Note: This is a simplified check. In production, you'd query StoreSettings
        // For now, we just delete
        themeRepository.delete(theme);
    }

    private ThemeResponse mapToResponse(Theme theme) {
        return ThemeResponse.builder()
                .id(theme.getId())
                .name(theme.getName())
                .description(theme.getDescription())
                .cssVariables(theme.getCssVariables())
                .previewImageUrl(theme.getPreviewImageUrl())
                .active(theme.isActive())
                .displayOrder(theme.getDisplayOrder())
                .build();
    }
}
