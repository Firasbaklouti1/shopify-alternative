package com.firas.saas.app.entity;

/**
 * Status of an app installation for a specific tenant.
 */
public enum InstallationStatus {
    /**
     * App is actively installed and functional.
     */
    ACTIVE,

    /**
     * App has been uninstalled - all tokens revoked, webhooks stopped.
     */
    REVOKED
}
