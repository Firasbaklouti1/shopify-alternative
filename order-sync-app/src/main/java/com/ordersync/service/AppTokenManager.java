package com.ordersync.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages app access tokens per tenant.
 * In a real app, these would be stored securely (e.g., encrypted in database).
 * For this test app, we use in-memory storage and allow runtime configuration.
 */
@Service
@Slf4j
public class AppTokenManager {

    // In-memory storage of tokens per tenant
    private final ConcurrentHashMap<Long, String> tenantTokens = new ConcurrentHashMap<>();

    // Default token from config (if set)
    @Value("${app.access-token:}")
    private String defaultAccessToken;

    /**
     * Get the access token for a tenant.
     */
    public String getAccessToken(Long tenantId) {
        String token = tenantTokens.get(tenantId);
        if (token != null) {
            return token;
        }
        // Fall back to default if set
        if (defaultAccessToken != null && !defaultAccessToken.isEmpty()) {
            return defaultAccessToken;
        }
        return null;
    }

    /**
     * Set the access token for a tenant.
     * Called when the app is installed in a new store.
     */
    public void setAccessToken(Long tenantId, String accessToken) {
        tenantTokens.put(tenantId, accessToken);
        log.info("Stored access token for tenant {}", tenantId);
    }

    /**
     * Remove the access token for a tenant.
     * Called when the app is uninstalled.
     */
    public void removeAccessToken(Long tenantId) {
        tenantTokens.remove(tenantId);
        log.info("Removed access token for tenant {}", tenantId);
    }

    /**
     * Check if we have a token for a tenant.
     */
    public boolean hasToken(Long tenantId) {
        return tenantTokens.containsKey(tenantId) || 
               (defaultAccessToken != null && !defaultAccessToken.isEmpty());
    }

    /**
     * Get all configured tenant IDs.
     */
    public java.util.Set<Long> getConfiguredTenants() {
        return tenantTokens.keySet();
    }
}
