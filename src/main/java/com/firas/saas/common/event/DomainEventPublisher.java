package com.firas.saas.common.event;

import com.firas.saas.webhook.entity.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Publisher for domain events.
 * This is the Subject in the Observer pattern.
 *
 * Services use this to publish events without knowing who will handle them.
 *
 * Example usage:
 * <pre>
 * eventPublisher.publish(WebhookEvent.ORDER_CREATED, orderData, tenantId, tenantSlug);
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Publish a domain event.
     * All registered listeners will be notified.
     *
     * @param eventType The type of event (e.g., ORDER_CREATED)
     * @param data The event payload data
     * @param tenantId The tenant this event belongs to
     * @param tenantSlug The tenant's slug identifier
     */
    public void publish(Webhook.WebhookEvent eventType, Map<String, Object> data,
                        Long tenantId, String tenantSlug) {
        DomainEvent event = new DomainEvent(this, eventType, data, tenantId, tenantSlug);
        applicationEventPublisher.publishEvent(event);
    }
}
