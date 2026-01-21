package com.firas.saas.storefront.service;

import com.firas.saas.storefront.dto.CreatePageRequest;
import com.firas.saas.storefront.dto.LayoutResponse;
import com.firas.saas.storefront.dto.UpdateLayoutRequest;
import com.firas.saas.storefront.entity.PageType;

import java.util.List;
import java.util.Map;

/**
 * Service for managing page layouts (per-tenant)
 */
public interface PageLayoutService {

    /**
     * Get all page layouts for a tenant
     */
    List<LayoutResponse> getAllLayouts(Long tenantId);

    /**
     * Get layout for a specific page type
     */
    LayoutResponse getLayout(Long tenantId, PageType pageType);

    /**
     * Get layout for a custom page by handle
     */
    LayoutResponse getCustomPageLayout(Long tenantId, String handle);

    /**
     * Get published layout JSON for storefront rendering (public API)
     */
    Map<String, Object> getPublishedLayoutJson(Long tenantId, PageType pageType);

    /**
     * Get published custom page layout JSON for storefront rendering (public API)
     */
    Map<String, Object> getPublishedCustomPageLayoutJson(Long tenantId, String handle);

    /**
     * Get draft layout JSON for editor preview
     */
    Map<String, Object> getDraftLayoutJson(Long tenantId, PageType pageType);

    /**
     * Create a new custom page
     */
    LayoutResponse createCustomPage(Long tenantId, CreatePageRequest request);

    /**
     * Update layout (saves to draft)
     */
    LayoutResponse updateLayout(Long tenantId, PageType pageType, UpdateLayoutRequest request, String userEmail);

    /**
     * Update custom page layout (saves to draft)
     */
    LayoutResponse updateCustomPageLayout(Long tenantId, String handle, UpdateLayoutRequest request, String userEmail);

    /**
     * Publish draft to live
     */
    LayoutResponse publishLayout(Long tenantId, PageType pageType, String userEmail);

    /**
     * Publish custom page draft to live
     */
    LayoutResponse publishCustomPageLayout(Long tenantId, String handle, String userEmail);

    /**
     * Discard draft changes
     */
    LayoutResponse discardDraft(Long tenantId, PageType pageType);

    /**
     * Delete custom page
     */
    void deleteCustomPage(Long tenantId, String handle);

    /**
     * Initialize default layouts for a new tenant (called during onboarding)
     */
    void initializeDefaultLayouts(Long tenantId, Long themeId);

    /**
     * Rollback to a previous version
     */
    LayoutResponse rollbackToVersion(Long tenantId, PageType pageType, Integer versionNumber, String userEmail);

    /**
     * Get version history for a page layout
     */
    List<Map<String, Object>> getVersionHistory(Long tenantId, PageType pageType);
}
