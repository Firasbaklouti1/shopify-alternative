package com.firas.saas.storefront.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Defines a section component's schema.
 * Used for validation, editor UI, and AI generation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionSchema {

    /**
     * Unique type identifier (e.g., "hero-banner", "product-grid")
     */
    private String type;

    /**
     * Display name in editor
     */
    private String name;

    /**
     * Description for editor tooltip
     */
    private String description;

    /**
     * Icon identifier for editor UI
     */
    private String icon;

    /**
     * Which page types this section can be used on
     */
    private List<String> allowedPageTypes;

    /**
     * Settings this section accepts
     */
    private List<SettingSchema> settings;

    /**
     * Block types this section can contain (if any)
     */
    private List<BlockSchema> blocks;

    /**
     * Maximum number of this section per page (0 = unlimited)
     */
    private int maxPerPage;

    /**
     * Preview image URL for editor
     */
    private String previewImageUrl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettingSchema {
        private String id;
        private String type; // text, textarea, number, color, image, select, checkbox, range, url
        private String label;
        private String description;
        private Object defaultValue;
        private boolean required;
        private Map<String, Object> validation; // min, max, pattern, options (for select)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlockSchema {
        private String type;
        private String name;
        private String icon;
        private List<SettingSchema> settings;
        private int maxPerSection; // 0 = unlimited
    }
}
