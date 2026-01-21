package com.firas.saas.webhook.repository;

import com.firas.saas.common.base.BaseRepository;
import com.firas.saas.webhook.entity.Webhook;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookRepository extends BaseRepository<Webhook> {

    List<Webhook> findAllByTenantId(Long tenantId);

    List<Webhook> findAllByTenantIdAndActiveTrue(Long tenantId);

    List<Webhook> findAllByTenantIdAndEventAndActiveTrue(Long tenantId, Webhook.WebhookEvent event);

    Optional<Webhook> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByUrlAndEventAndTenantId(String url, Webhook.WebhookEvent event, Long tenantId);
}

