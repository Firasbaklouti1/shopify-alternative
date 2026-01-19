package com.firas.saas.subscription.repository;

import com.firas.saas.common.base.BaseRepository;
import com.firas.saas.subscription.entity.SubscriptionPlan;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends BaseRepository<SubscriptionPlan> {
    Optional<SubscriptionPlan> findBySlug(String slug);
}
