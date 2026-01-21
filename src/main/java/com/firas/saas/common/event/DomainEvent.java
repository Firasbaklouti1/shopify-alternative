package com.firas.saas.common.event;

import com.firas.saas.webhook.entity.Webhook;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * Base class for all domain events in the system.
 * Uses Spring's ApplicationEvent for Observer pattern implementation.
 */
@Getter
public class DomainEvent extends ApplicationEvent {

    private final Webhook.WebhookEvent eventType;
    private final Map<String, Object> data;
    private final Long tenantId;
    private final String tenantSlug;

    public DomainEvent(Object source, Webhook.WebhookEvent eventType,
                       Map<String, Object> data, Long tenantId, String tenantSlug) {
        super(source);
        this.eventType = eventType;
        this.data = data;
        this.tenantId = tenantId;
        this.tenantSlug = tenantSlug;
    }
}
