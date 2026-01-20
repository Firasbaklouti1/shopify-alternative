package com.firas.saas.webhook.dto;

import com.firas.saas.webhook.entity.Webhook;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard webhook event payload following the specification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEventPayload {

    private String id;              // Globally unique event ID (e.g., "evt_abc123")
    private String type;            // Event type (e.g., "order.created")
    private String apiVersion;      // Schema version (e.g., "v1")
    private String createdAt;       // ISO 8601 timestamp
    private TenantInfo tenant;      // Tenant context
    private Map<String, Object> data; // Event-specific payload

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantInfo {
        private String id;          // Tenant ID
        private String slug;        // Store slug
    }

    /**
     * Convert event enum to dot-notation string (ORDER_CREATED -> order.created)
     */
    public static String eventToString(Webhook.WebhookEvent event) {
        String name = event.name().toLowerCase();
        // Convert PRODUCT_CREATED to product.created
        int underscoreIdx = name.indexOf('_');
        if (underscoreIdx > 0) {
            return name.substring(0, underscoreIdx) + "." + name.substring(underscoreIdx + 1);
        }
        return name;
    }
}

