package com.firas.saas.storefront.service;

import com.firas.saas.storefront.dto.ThemeResponse;
import com.firas.saas.storefront.entity.Theme;

import java.util.List;
import java.util.Map;

/**
 * Service for managing themes (platform-level, ADMIN operations)
 */
public interface ThemeService {

    /**
     * Get all active themes available for selection
     */
    List<ThemeResponse> getActiveThemes();

    /**
     * Get theme by ID
     */
    ThemeResponse getThemeById(Long themeId);

    /**
     * Get theme entity by ID (internal use)
     */
    Theme getThemeEntityById(Long themeId);

    /**
     * Create a new theme (ADMIN only)
     */
    ThemeResponse createTheme(String name, String description, Map<String, Object> defaultLayoutsJson,
                               Map<String, Object> cssVariables, String previewImageUrl);

    /**
     * Update theme (ADMIN only)
     */
    ThemeResponse updateTheme(Long themeId, String name, String description,
                               Map<String, Object> cssVariables, String previewImageUrl);

    /**
     * Activate/deactivate theme (ADMIN only)
     */
    ThemeResponse setThemeActive(Long themeId, boolean active);

    /**
     * Delete theme (ADMIN only, fails if in use)
     */
    void deleteTheme(Long themeId);
}
