package com.firas.saas.app.entity;

import com.firas.saas.webhook.entity.Webhook;

import java.util.Set;

/**
 * Defines the permissions/scopes that an app can request.
 * Each scope maps to specific webhook events that the app can receive.
 */
public enum AppScope {
    READ_ORDERS,
    WRITE_ORDERS,
    READ_PRODUCTS,
    WRITE_PRODUCTS,
    READ_CUSTOMERS,
    MANAGE_WEBHOOKS;

    /**
     * Returns the webhook events that require this scope.
     * Apps only receive webhooks for events they have scope for.
     */
    public Set<Webhook.WebhookEvent> getAllowedEvents() {
        return switch (this) {
            case READ_ORDERS, WRITE_ORDERS -> Set.of(
                    Webhook.WebhookEvent.ORDER_CREATED,
                    Webhook.WebhookEvent.ORDER_UPDATED,
                    Webhook.WebhookEvent.ORDER_PAID,
                    Webhook.WebhookEvent.ORDER_FULFILLED,
                    Webhook.WebhookEvent.ORDER_CANCELLED
            );
            case READ_PRODUCTS, WRITE_PRODUCTS -> Set.of(
                    Webhook.WebhookEvent.PRODUCT_CREATED,
                    Webhook.WebhookEvent.PRODUCT_UPDATED,
                    Webhook.WebhookEvent.PRODUCT_DELETED,
                    Webhook.WebhookEvent.INVENTORY_UPDATED,
                    Webhook.WebhookEvent.INVENTORY_LOW
            );
            case READ_CUSTOMERS -> Set.of(
                    Webhook.WebhookEvent.CUSTOMER_CREATED,
                    Webhook.WebhookEvent.CUSTOMER_UPDATED
            );
            case MANAGE_WEBHOOKS -> Set.of(); // No automatic events for this scope
        };
    }

    /**
     * Check if this scope allows receiving a specific webhook event.
     */
    public boolean allowsEvent(Webhook.WebhookEvent event) {
        return getAllowedEvents().contains(event);
    }

    /**
     * Get the required scope for a given webhook event.
     * Returns null if the event doesn't require any scope (e.g., APP_INSTALLED).
     */
    public static AppScope getRequiredScopeForEvent(Webhook.WebhookEvent event) {
        return switch (event) {
            case ORDER_CREATED, ORDER_UPDATED, ORDER_PAID, ORDER_FULFILLED, ORDER_CANCELLED -> READ_ORDERS;
            case PRODUCT_CREATED, PRODUCT_UPDATED, PRODUCT_DELETED, INVENTORY_UPDATED, INVENTORY_LOW -> READ_PRODUCTS;
            case CUSTOMER_CREATED, CUSTOMER_UPDATED -> READ_CUSTOMERS;
            // These events are always delivered regardless of scope
            case APP_INSTALLED, APP_UNINSTALLED -> null;
            // Payment and store events - no scope restriction for now
            default -> null;
        };
    }
}
