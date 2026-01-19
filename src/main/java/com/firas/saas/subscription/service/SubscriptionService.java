package com.firas.saas.subscription.service;

import com.firas.saas.subscription.dto.SubscribeRequest;
import com.firas.saas.subscription.dto.SubscriptionPlanResponse;
import com.firas.saas.subscription.dto.SubscriptionResponse;

import java.util.List;

public interface SubscriptionService {
    List<SubscriptionPlanResponse> getAllPlans();
    SubscriptionResponse getCurrentSubscription(Long tenantId);
    SubscriptionResponse subscribe(Long tenantId, SubscribeRequest request);
    void cancelSubscription(Long tenantId);
    
    // Admin methods
    SubscriptionPlanResponse createPlan(com.firas.saas.subscription.dto.SubscriptionPlanRequest request);
    SubscriptionPlanResponse updatePlan(Long id, com.firas.saas.subscription.dto.SubscriptionPlanRequest request);
    void deletePlan(Long id);

    // Initial seeding helper (keep or remove if fully replaced by DTO version)
    void createPlan(String name, String slug, double price, String interval, String features);
}
