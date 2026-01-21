package com.firas.saas.webhook.repository;

import com.firas.saas.common.base.BaseRepository;
import com.firas.saas.webhook.entity.WebhookDelivery;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookDeliveryRepository extends BaseRepository<WebhookDelivery> {

    List<WebhookDelivery> findAllByWebhookIdAndTenantId(Long webhookId, Long tenantId);

    List<WebhookDelivery> findAllByTenantIdOrderByTriggeredAtDesc(Long tenantId, Pageable pageable);

    List<WebhookDelivery> findTop100ByTenantIdOrderByTriggeredAtDesc(Long tenantId);

    Optional<WebhookDelivery> findByEventIdAndTenantId(String eventId, Long tenantId);

    Optional<WebhookDelivery> findByIdAndTenantId(Long id, Long tenantId);

    List<WebhookDelivery> findByStatusAndNextRetryAtBefore(
            WebhookDelivery.DeliveryStatus status, LocalDateTime now);

    List<WebhookDelivery> findByStatusInAndNextRetryAtBefore(
            List<WebhookDelivery.DeliveryStatus> statuses, LocalDateTime now);

    long countByWebhookIdAndTenantId(Long webhookId, Long tenantId);

    long countByWebhookIdAndStatusAndTenantId(Long webhookId,
                                               WebhookDelivery.DeliveryStatus status,
                                               Long tenantId);

    Optional<WebhookDelivery> findTopByWebhookIdAndTenantIdOrderByTriggeredAtDesc(
            Long webhookId, Long tenantId);
}
