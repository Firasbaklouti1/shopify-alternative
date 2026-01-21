package com.firas.saas.common.event;

import com.firas.saas.webhook.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener that handles domain events and triggers webhooks.
 * This is an Observer in the Observer pattern.
 *
 * Benefits:
 * - Decoupled from the services that produce events
 * - Can add more listeners without modifying existing code
 * - Async processing doesn't block the main business logic
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookEventListener {

    private final WebhookService webhookService;

    /**
     * Listen for all domain events and trigger webhooks.
     *
     * @Async ensures this runs in a separate thread,
     * so webhook delivery doesn't block the main transaction.
     */
    @EventListener
    @Async
    public void handleDomainEvent(DomainEvent event) {
        log.info("Received domain event: {} for tenant {}",
                event.getEventType(), event.getTenantId());

        try {
            webhookService.triggerEvent(
                    event.getEventType(),
                    event.getData(),
                    event.getTenantId(),
                    event.getTenantSlug()
            );
        } catch (Exception e) {
            log.error("Failed to process webhook for event {}: {}",
                    event.getEventType(), e.getMessage());
        }
    }
}
