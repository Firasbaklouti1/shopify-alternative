package com.firas.saas.common.context;

import com.firas.saas.security.service.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class to access the current tenant context from the security principal.
 *
 * Usage:
 *   Long tenantId = TenantContext.getCurrentTenantId();
 *
 * This eliminates the need to manually extract tenant ID in every controller/service.
 */
public final class TenantContext {

    private TenantContext() {
        // Utility class - prevent instantiation
    }

    /**
     * Gets the current tenant ID from the authenticated user's security context.
     *
     * @return the tenant ID of the currently authenticated user
     * @throws IllegalStateException if no tenant context is available (user not authenticated)
     */
    public static Long getCurrentTenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user - cannot determine tenant context");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getTenantId();
        }

        throw new IllegalStateException("Principal is not a UserPrincipal - cannot determine tenant context");
    }

    /**
     * Gets the current tenant ID, or null if not in an authenticated context.
     * Useful for optional tenant-aware operations.
     *
     * @return the tenant ID or null
     */
    public static Long getCurrentTenantIdOrNull() {
        try {
            return getCurrentTenantId();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * Checks if there is a valid tenant context available.
     *
     * @return true if a tenant context exists
     */
    public static boolean hasTenantContext() {
        return getCurrentTenantIdOrNull() != null;
    }
}

