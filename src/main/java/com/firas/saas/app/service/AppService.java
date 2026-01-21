package com.firas.saas.app.service;

import com.firas.saas.app.dto.*;
import com.firas.saas.app.entity.AppScope;

import java.util.List;

/**
 * Service for managing apps in the App Store.
 * ADMIN-only operations.
 */
public interface AppService {

    /**
     * Create a new app. Returns the app details and plaintext client secret (shown once).
     */
    AppCreatedResponse createApp(CreateAppRequest request);

    /**
     * Get all apps (ADMIN sees all, merchants see only published).
     */
    List<AppResponse> getAllApps(boolean includeNonPublished);

    /**
     * Get app by ID.
     */
    AppResponse getAppById(Long appId);

    /**
     * Get app by client ID.
     */
    AppResponse getAppByClientId(String clientId);

    /**
     * Update an app.
     */
    AppResponse updateApp(Long appId, UpdateAppRequest request);

    /**
     * Publish an app (make it available for installation).
     */
    AppResponse publishApp(Long appId);

    /**
     * Suspend an app (disable all installations).
     */
    AppResponse suspendApp(Long appId);

    /**
     * Unpublish an app (set to DRAFT).
     */
    AppResponse unpublishApp(Long appId);

    /**
     * Regenerate client secret. Returns the new plaintext secret (shown once).
     */
    AppCreatedResponse regenerateClientSecret(Long appId);

    /**
     * Delete an app. Only allowed if no active installations exist.
     */
    void deleteApp(Long appId);

    /**
     * Get all available scopes.
     */
    List<AppScope> getAvailableScopes();
}
