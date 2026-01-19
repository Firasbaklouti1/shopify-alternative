package com.firas.saas.subscription.repository;

import com.firas.saas.common.base.BaseRepository;
import com.firas.saas.subscription.entity.Subscription;
import com.firas.saas.subscription.entity.SubscriptionStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends BaseRepository<Subscription> {
    Optional<Subscription> findByTenantIdAndStatus(Long tenantId, SubscriptionStatus status);
    List<Subscription> findAllByTenantId(Long tenantId);
    
    // For background jobs later (finding expired subscriptions)
    List<Subscription> findByStatusAndEndDateBefore(SubscriptionStatus status, LocalDateTime date);
}
