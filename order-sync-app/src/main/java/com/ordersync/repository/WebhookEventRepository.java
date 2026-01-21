package com.ordersync.repository;

import com.ordersync.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    Optional<WebhookEvent> findByEventId(String eventId);

    boolean existsByEventId(String eventId);

    List<WebhookEvent> findByEventType(String eventType);

    List<WebhookEvent> findByTenantId(Long tenantId);

    List<WebhookEvent> findByStatus(WebhookEvent.ProcessingStatus status);

    List<WebhookEvent> findAllByOrderByReceivedAtDesc();
}
