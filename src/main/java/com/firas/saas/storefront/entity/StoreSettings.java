package com.firas.saas.storefront.entity;

import com.firas.saas.common.base.TenantEntity;
import com.firas.saas.common.util.JsonMapConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

/**
 * Store-level settings for a tenant's storefront.
 * Controls branding, checkout behavior, SEO defaults, and theme selection.
 * One StoreSettings per tenant.
 */
@Entity
@Table(name = "store_settings",
       uniqueConstraints = @UniqueConstraint(columnNames = "tenant_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreSettings extends TenantEntity {

    /**
     * Selected theme (nullable - uses system default if not set)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    /**
     * Checkout mode: GUEST_ONLY, ACCOUNT_ONLY, or BOTH
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CheckoutMode checkoutMode = CheckoutMode.BOTH;

    /**
     * Global styles that override theme defaults.
     * Structure: { "primaryColor": "#FF5733", "secondaryColor": "#333",
     *              "fontFamily": "Inter", "logo": "/uploads/logo.png",
     *              "favicon": "/uploads/favicon.ico" }
     */
    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> globalStyles;

    /**
     * SEO default settings for the store.
     * Structure: { "titleTemplate": "{{page_title}} | {{store_name}}",
     *              "defaultDescription": "...", "ogImage": "/uploads/og.jpg" }
     */
    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> seoDefaults;

    /**
     * Social media links for the store.
     * Structure: { "facebook": "...", "instagram": "...", "twitter": "...", "tiktok": "..." }
     */
    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> socialLinks;

    /**
     * Contact email displayed on the storefront
     */
    private String contactEmail;

    /**
     * Custom domain (e.g., "mystore.com") - null means using platform subdomain
     */
    private String customDomain;

    /**
     * Whether the storefront is published and accessible to customers
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean published = false;

    /**
     * Store announcement bar text (nullable)
     */
    private String announcementText;

    /**
     * Whether the announcement bar is visible
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean announcementEnabled = false;
}
