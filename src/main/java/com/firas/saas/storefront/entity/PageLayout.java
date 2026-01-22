package com.firas.saas.storefront.entity;

import com.firas.saas.common.base.TenantEntity;
import com.firas.saas.common.util.JsonMapConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

/**
 * Stores the JSON layout configuration for a specific page type.
 * Each tenant can have one layout per page type (or multiple for CUSTOM pages).
 *
 * The layoutJson follows the Server-Driven UI pattern where the entire page
 * structure is defined as JSON, which the Next.js frontend renders.
 */
@Entity
@Table(name = "page_layouts",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"tenant_id", "page_type", "handle"},
           name = "uk_page_layout_tenant_type_handle"
       ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageLayout extends TenantEntity {

    /**
     * Type of page (HOME, PRODUCT, COLLECTION, CART, CHECKOUT, CUSTOM)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "page_type", nullable = false)
    private PageType pageType;

    /**
     * URL handle for CUSTOM pages (e.g., "about-us", "contact").
     * Null for standard page types.
     */
    private String handle;

    /**
     * Display name of the page (shown in editor)
     */
    private String name;

    /**
     * The live/published layout configuration.
     * Structure matches the JSON Layout Schema defined in the plan.
     * Example:
     * {
     *   "sections": {
     *     "hero-1": { "type": "hero-banner", "settings": {...}, "blocks": {...} }
     *   },
     *   "order": ["hero-1", "featured-products"]
     * }
     */
    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "TEXT", nullable = false)
    private Map<String, Object> layoutJson;

    /**
     * Draft layout (unpublished changes being edited).
     * When merchant saves in editor, this is updated.
     * When merchant publishes, this is copied to layoutJson.
     */
    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> draftJson;

    /**
     * Whether the page has been published at least once
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean published = false;

    /**
     * Version number for optimistic locking and change tracking
     */
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 0;

    /**
     * SEO title override for this specific page
     */
    private String seoTitle;

    /**
     * SEO description override for this specific page
     */
    @Column(length = 500)
    private String seoDescription;
}
