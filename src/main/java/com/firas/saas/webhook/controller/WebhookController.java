package com.firas.saas.webhook.controller;

import com.firas.saas.security.service.UserPrincipal;
import com.firas.saas.webhook.dto.WebhookDeliveryResponse;
import com.firas.saas.webhook.dto.WebhookRequest;
import com.firas.saas.webhook.dto.WebhookResponse;
import com.firas.saas.webhook.entity.Webhook;
import com.firas.saas.webhook.service.WebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    // ==================== SUBSCRIPTION MANAGEMENT ====================

    @PostMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<WebhookResponse> createWebhook(
            @Valid @RequestBody WebhookRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return new ResponseEntity<>(webhookService.createWebhook(request, principal.getTenantId()), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<List<WebhookResponse>> getAllWebhooks(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(webhookService.getAllWebhooks(principal.getTenantId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<WebhookResponse> getWebhookById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(webhookService.getWebhookById(id, principal.getTenantId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<WebhookResponse> updateWebhook(
            @PathVariable Long id,
            @Valid @RequestBody WebhookRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(webhookService.updateWebhook(id, request, principal.getTenantId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Void> deleteWebhook(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        webhookService.deleteWebhook(id, principal.getTenantId());
        return ResponseEntity.noContent().build();
    }

    // ==================== PAUSE/RESUME ====================

    @PatchMapping("/{id}/pause")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<WebhookResponse> pauseWebhook(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(webhookService.pauseWebhook(id, principal.getTenantId()));
    }

    @PatchMapping("/{id}/resume")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<WebhookResponse> resumeWebhook(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(webhookService.resumeWebhook(id, principal.getTenantId()));
    }

    // ==================== SECRET MANAGEMENT ====================

    @PostMapping("/{id}/regenerate-secret")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<WebhookResponse> regenerateSecret(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(webhookService.regenerateSecret(id, principal.getTenantId()));
    }

    // ==================== DELIVERY HISTORY ====================

    @GetMapping("/{id}/deliveries")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<List<WebhookDeliveryResponse>> getDeliveryHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(webhookService.getDeliveryHistory(id, principal.getTenantId()));
    }

    @GetMapping("/deliveries/recent")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<List<WebhookDeliveryResponse>> getRecentDeliveries(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(webhookService.getRecentDeliveries(principal.getTenantId()));
    }

    @GetMapping("/deliveries/{id}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<WebhookDeliveryResponse> getDeliveryById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(webhookService.getDeliveryById(id, principal.getTenantId()));
    }

    @GetMapping("/deliveries/event/{eventId}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<WebhookDeliveryResponse> getDeliveryByEventId(
            @PathVariable String eventId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(webhookService.getDeliveryByEventId(eventId, principal.getTenantId()));
    }

    // ==================== MANUAL RETRY ====================

    @PostMapping("/deliveries/{id}/retry")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<WebhookDeliveryResponse> retryDelivery(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(webhookService.retryDelivery(id, principal.getTenantId()));
    }

    // ==================== TESTING ====================

    @PostMapping("/{id}/test")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<WebhookDeliveryResponse> testWebhook(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(webhookService.testWebhook(id, principal.getTenantId()));
    }

    // ==================== AVAILABLE EVENTS ====================

    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('MERCHANT', 'STAFF')")
    public ResponseEntity<List<Webhook.WebhookEvent>> getAvailableEvents() {
        return ResponseEntity.ok(webhookService.getAvailableEvents());
    }
}
