package com.firas.saas.storefront.entity;

import com.firas.saas.common.base.BaseEntity;
import com.firas.saas.common.util.JsonMapConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

/**
 * Platform-provided themes that merchants can select as a starting point.
 * Themes are global (not tenant-scoped) and define default layouts and styling.
 */
@Entity
@Table(name = "themes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Theme extends BaseEntity {

    /**
     * Display name of the theme (e.g., "Minimal", "Bold", "Dawn")
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Human-readable description of the theme's style
     */
    @Column(length = 500)
    private String description;

    /**
     * Default page layouts for all page types.
     * Structure: { "HOME": {...layout...}, "PRODUCT": {...layout...}, ... }
     */
    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> defaultLayoutsJson;

    /**
     * CSS custom properties (variables) for the theme.
     * Structure: { "primaryColor": "#000", "fontFamily": "Inter", ... }
     */
    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> cssVariables;

    /**
     * URL to a preview image of the theme
     */
    private String previewImageUrl;

    /**
     * Whether this theme is available for merchants to select
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * Display order in theme selection UI
     */
    @Column(nullable = false)
    @Builder.Default
    private int displayOrder = 0;
}
