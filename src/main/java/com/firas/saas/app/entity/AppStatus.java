package com.firas.saas.app.entity;

/**
 * Status of an app in the App Store.
 */
public enum AppStatus {
    /**
     * App is being developed and not visible to merchants.
     */
    DRAFT,

    /**
     * App is published and available for installation.
     */
    PUBLISHED,

    /**
     * App has been suspended by admin (security issue, policy violation, etc.).
     */
    SUSPENDED
}
