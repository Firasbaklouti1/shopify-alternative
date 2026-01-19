package com.firas.saas.subscription.service;

import com.firas.saas.billing.service.PaymentService;
import com.firas.saas.billing.dto.PaymentRequest;
import com.firas.saas.subscription.dto.SubscribeRequest;
import com.firas.saas.subscription.dto.SubscriptionPlanResponse;
import com.firas.saas.subscription.dto.SubscriptionResponse;
import com.firas.saas.subscription.entity.Subscription;
import com.firas.saas.subscription.entity.SubscriptionPlan;
import com.firas.saas.subscription.entity.SubscriptionStatus;
import com.firas.saas.subscription.repository.SubscriptionPlanRepository;
import com.firas.saas.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.firas.saas.billing.entity.Invoice;
import com.firas.saas.billing.repository.InvoiceRepository;

// ... other imports

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PaymentService paymentService;
    private final InvoiceRepository invoiceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getAllPlans() {
        return subscriptionPlanRepository.findAll().stream()
                .filter(SubscriptionPlan::isActive)
                .map(this::mapToPlanResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getCurrentSubscription(Long tenantId) {
        return subscriptionRepository.findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE)
                .map(this::mapToResponse)
                .orElse(null); // Or throw exception if strictly required
    }

    @Override
    @Transactional
    public SubscriptionResponse subscribe(Long tenantId, SubscribeRequest request) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        // Process Payment if price > 0
        if (plan.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .amount(plan.getPrice())
                    .currency("USD") // Assume USD for now
                    .paymentMethod(request.getPaymentMethod())
                    .description("Subscription to " + plan.getName())
                    .build();

            boolean paymentSuccess = paymentService.processPayment(paymentRequest);
            if (!paymentSuccess) {
                throw new RuntimeException("Payment failed");
            }
            
            // Generate Invoice
            invoiceRepository.save(Invoice.builder()
                    .tenantId(tenantId)
                    .amount(plan.getPrice())
                    .currency("USD")
                    .status("PAID")
                    .description("Subscription to " + plan.getName())
                    .issuedAt(LocalDateTime.now())
                    .build());
        }

        // Cancel existing active subscription if any
        subscriptionRepository.findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE)
                .ifPresent(existing -> {
                    existing.setStatus(SubscriptionStatus.CANCELED);
                    subscriptionRepository.save(existing);
                });

        Subscription subscription = Subscription.builder()
                .tenantId(tenantId)
                .plan(plan)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1)) // Assuming monthly for now
                .status(SubscriptionStatus.ACTIVE)
                .autoRenew(true)
                .build();

        return mapToResponse(subscriptionRepository.save(subscription));
    }

    @Override
    @Transactional
    public void cancelSubscription(Long tenantId) {
        Subscription subscription = subscriptionRepository.findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active subscription found"));
        
        subscription.setAutoRenew(false);
        // Optionally set status to CANCELED immediately or wait until end date
        // For simplicity, we keep it ACTIVE but no renew, or mark CANCELED. 
        // Let's set autoRenew to false, effective cancellation at end of term.
        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public SubscriptionPlanResponse createPlan(com.firas.saas.subscription.dto.SubscriptionPlanRequest request) {
        if (subscriptionPlanRepository.findBySlug(request.getSlug()).isPresent()) {
             throw new RuntimeException("Plan with slug already exists");
        }
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .price(request.getPrice())
                .billingInterval(request.getBillingInterval())
                .features(request.getFeatures())
                .active(true)
                .build();
        return mapToPlanResponse(subscriptionPlanRepository.save(plan));
    }

    @Override
    @Transactional
    public SubscriptionPlanResponse updatePlan(Long id, com.firas.saas.subscription.dto.SubscriptionPlanRequest request) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        
        plan.setName(request.getName());
        // slug update might be restricted if it breaks URLs, but allowing for now with check
        if (!plan.getSlug().equals(request.getSlug()) && subscriptionPlanRepository.findBySlug(request.getSlug()).isPresent()) {
             throw new RuntimeException("Slug already taken");
        }
        plan.setSlug(request.getSlug());
        plan.setPrice(request.getPrice());
        plan.setBillingInterval(request.getBillingInterval());
        plan.setFeatures(request.getFeatures());
        
        return mapToPlanResponse(subscriptionPlanRepository.save(plan));
    }

    @Override
    @Transactional
    public void deletePlan(Long id) {
        // Soft delete is safer if subscriptions exist
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        plan.setActive(false);
        subscriptionPlanRepository.save(plan);
    }

    @Override
    @Transactional
    public void createPlan(String name, String slug, double price, String interval, String features) {
        if (subscriptionPlanRepository.findBySlug(slug).isPresent()) {
            return;
        }
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(name)
                .slug(slug)
                .price(BigDecimal.valueOf(price))
                .billingInterval(interval)
                .features(features)
                .active(true)
                .build();
        subscriptionPlanRepository.save(plan);
    }

    private SubscriptionPlanResponse mapToPlanResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .slug(plan.getSlug())
                .price(plan.getPrice())
                .billingInterval(plan.getBillingInterval())
                .features(plan.getFeatures())
                .build();
    }

    private SubscriptionResponse mapToResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .plan(mapToPlanResponse(subscription.getPlan()))
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .status(subscription.getStatus())
                .autoRenew(subscription.isAutoRenew())
                .build();
    }
}
