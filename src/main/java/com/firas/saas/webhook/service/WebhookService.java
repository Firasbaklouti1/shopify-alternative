package com.firas.saas.webhook.service;

import com.firas.saas.webhook.dto.WebhookDeliveryResponse;
import com.firas.saas.webhook.dto.WebhookRequest;
import com.firas.saas.webhook.dto.WebhookResponse;
import com.firas.saas.webhook.entity.Webhook;

import java.util.List;
import java.util.Map;

public interface WebhookService {

    // ==================== SUBSCRIPTION MANAGEMENT ====================

    WebhookResponse createWebhook(WebhookRequest request, Long tenantId);
    List<WebhookResponse> getAllWebhooks(Long tenantId);
    WebhookResponse getWebhookById(Long id, Long tenantId);
    WebhookResponse updateWebhook(Long id, WebhookRequest request, Long tenantId);
    void deleteWebhook(Long id, Long tenantId);

    // Pause/Resume
    WebhookResponse pauseWebhook(Long id, Long tenantId);
    WebhookResponse resumeWebhook(Long id, Long tenantId);

    // Secret management
    WebhookResponse regenerateSecret(Long id, Long tenantId);

    // ==================== EVENT TRIGGERING ====================

    /**
     * Trigger webhook event - called by domain services when events occur.
     * This is async and non-blocking.
     */
    void triggerEvent(Webhook.WebhookEvent event, Map<String, Object> data, Long tenantId, String tenantSlug);

    // ==================== DELIVERY MANAGEMENT ====================

    List<WebhookDeliveryResponse> getDeliveryHistory(Long webhookId, Long tenantId);
    List<WebhookDeliveryResponse> getRecentDeliveries(Long tenantId);
    WebhookDeliveryResponse getDeliveryById(Long id, Long tenantId);
    WebhookDeliveryResponse getDeliveryByEventId(String eventId, Long tenantId);

    // Manual retry
    WebhookDeliveryResponse retryDelivery(Long deliveryId, Long tenantId);

    // ==================== TESTING ====================

    WebhookDeliveryResponse testWebhook(Long id, Long tenantId);

    // ==================== AVAILABLE EVENTS ====================

    List<Webhook.WebhookEvent> getAvailableEvents();
}
