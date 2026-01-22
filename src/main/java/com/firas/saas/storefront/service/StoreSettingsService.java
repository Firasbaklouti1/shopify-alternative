package com.firas.saas.storefront.service;

import com.firas.saas.storefront.dto.StoreSettingsResponse;
import com.firas.saas.storefront.dto.UpdateStoreSettingsRequest;
import com.firas.saas.storefront.entity.StoreSettings;

/**
 * Service for managing store settings (per-tenant)
 */
public interface StoreSettingsService {

    /**
     * Get store settings for a tenant (creates default if doesn't exist)
     */
    StoreSettingsResponse getStoreSettings(Long tenantId);

    /**
     * Get store settings by tenant slug (for public storefront API)
     */
    StoreSettingsResponse getStoreSettingsBySlug(String slug);

    /**
     * Get store settings entity (internal use)
     */
    StoreSettings getOrCreateStoreSettings(Long tenantId);

    /**
     * Update store settings
     */
    StoreSettingsResponse updateStoreSettings(Long tenantId, UpdateStoreSettingsRequest request);

    /**
     * Publish store (make it publicly accessible)
     */
    StoreSettingsResponse publishStore(Long tenantId);

    /**
     * Unpublish store (make it private)
     */
    StoreSettingsResponse unpublishStore(Long tenantId);

    /**
     * Check if store is published
     */
    boolean isStorePublished(Long tenantId);

    /**
     * Resolve tenant ID from custom domain
     */
    Long resolveTenantIdByDomain(String domain);
}
