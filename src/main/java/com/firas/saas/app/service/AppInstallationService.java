package com.firas.saas.app.service;

import com.firas.saas.app.dto.*;

import java.util.List;

/**
 * Service for managing app installations per tenant.
 * MERCHANT operations.
 */
public interface AppInstallationService {

    /**
     * Install an app for the given tenant.
     * Validates clientId + clientSecret, creates installation, generates token,
     * and triggers APP_INSTALLED webhook.
     */
    InstallationWithTokenResponse installApp(InstallAppRequest request, Long tenantId, Long userId);

    /**
     * Uninstall an app.
     * Revokes all tokens, sets status to REVOKED, triggers APP_UNINSTALLED webhook.
     */
    void uninstallApp(Long installationId, Long tenantId);

    /**
     * Get all installed apps for a tenant.
     */
    List<AppInstallationResponse> getInstalledApps(Long tenantId);

    /**
     * Get all active installed apps for a tenant.
     */
    List<AppInstallationResponse> getActiveInstalledApps(Long tenantId);

    /**
     * Get installation by ID.
     */
    AppInstallationResponse getInstallationById(Long installationId, Long tenantId);

    /**
     * Rotate access token for an installation.
     * Revokes old tokens and generates a new one.
     */
    RotateTokenResponse rotateToken(Long installationId, Long tenantId);

    /**
     * Get token info for an installation (without the actual token value).
     */
    List<AccessTokenResponse> getTokensForInstallation(Long installationId, Long tenantId);
}
