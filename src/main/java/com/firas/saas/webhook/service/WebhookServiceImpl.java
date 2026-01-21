package com.firas.saas.webhook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firas.saas.app.service.AppWebhookService;
import com.firas.saas.common.exception.ResourceNotFoundException;
import com.firas.saas.webhook.dto.*;
import com.firas.saas.webhook.entity.Webhook;
import com.firas.saas.webhook.entity.WebhookDelivery;
import com.firas.saas.webhook.repository.WebhookDeliveryRepository;
import com.firas.saas.webhook.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WebhookServiceImpl implements WebhookService {

    private final WebhookRepository webhookRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final ObjectMapper objectMapper;
    private final AppWebhookService appWebhookService;
    private final RestTemplate restTemplate = new RestTemplate();

    public WebhookServiceImpl(
            WebhookRepository webhookRepository,
            WebhookDeliveryRepository deliveryRepository,
            ObjectMapper objectMapper,
            @Lazy AppWebhookService appWebhookService) {
        this.webhookRepository = webhookRepository;
        this.deliveryRepository = deliveryRepository;
        this.objectMapper = objectMapper;
        this.appWebhookService = appWebhookService;
    }

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int MAX_RESPONSE_BODY_LENGTH = 1000;
    private static final int RETRY_WINDOW_HOURS = 72; // 3 days max retry window

    // ==================== SUBSCRIPTION MANAGEMENT ====================

    @Override
    @Transactional
    public WebhookResponse createWebhook(WebhookRequest request, Long tenantId) {
        if (webhookRepository.existsByUrlAndEventAndTenantId(request.getUrl(), request.getEvent(), tenantId)) {
            throw new RuntimeException("Webhook for this URL and event already exists");
        }

        String secret = generateSecret();

        Webhook webhook = Webhook.builder()
                .name(request.getName())
                .url(request.getUrl())
                .event(request.getEvent())
                .secret(secret)
                .apiVersion(request.getApiVersion() != null ? request.getApiVersion() : "v1")
                .maxRetries(request.getMaxRetries() != null ? request.getMaxRetries() : 5)
                .headers(request.getHeaders())
                .active(request.isActive())
                .paused(false)
                .build();
        webhook.setTenantId(tenantId);

        return mapToResponse(webhookRepository.save(webhook), tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebhookResponse> getAllWebhooks(Long tenantId) {
        return webhookRepository.findAllByTenantId(tenantId).stream()
                .map(w -> mapToResponse(w, tenantId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WebhookResponse getWebhookById(Long id, Long tenantId) {
        return webhookRepository.findByIdAndTenantId(id, tenantId)
                .map(w -> mapToResponse(w, tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Webhook", id));
    }

    @Override
    @Transactional
    public WebhookResponse updateWebhook(Long id, WebhookRequest request, Long tenantId) {
        Webhook webhook = webhookRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook", id));

        webhook.setName(request.getName());
        webhook.setUrl(request.getUrl());
        webhook.setEvent(request.getEvent());
        webhook.setApiVersion(request.getApiVersion() != null ? request.getApiVersion() : webhook.getApiVersion());
        webhook.setMaxRetries(request.getMaxRetries() != null ? request.getMaxRetries() : webhook.getMaxRetries());
        webhook.setHeaders(request.getHeaders());
        webhook.setActive(request.isActive());

        return mapToResponse(webhookRepository.save(webhook), tenantId);
    }

    @Override
    @Transactional
    public void deleteWebhook(Long id, Long tenantId) {
        Webhook webhook = webhookRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook", id));
        webhookRepository.delete(webhook);
    }

    @Override
    @Transactional
    public WebhookResponse pauseWebhook(Long id, Long tenantId) {
        Webhook webhook = webhookRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook", id));
        webhook.setPaused(true);
        return mapToResponse(webhookRepository.save(webhook), tenantId);
    }

    @Override
    @Transactional
    public WebhookResponse resumeWebhook(Long id, Long tenantId) {
        Webhook webhook = webhookRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook", id));
        webhook.setPaused(false);
        return mapToResponse(webhookRepository.save(webhook), tenantId);
    }

    @Override
    @Transactional
    public WebhookResponse regenerateSecret(Long id, Long tenantId) {
        Webhook webhook = webhookRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook", id));
        webhook.setSecret(generateSecret());
        return mapToResponse(webhookRepository.save(webhook), tenantId);
    }

    // ==================== EVENT TRIGGERING ====================

    @Override
    @Async
    @Transactional
    public void triggerEvent(Webhook.WebhookEvent event, Map<String, Object> data, Long tenantId, String tenantSlug) {
        // Deliver to installed apps (based on their scopes)
        try {
            appWebhookService.deliverToApps(event, data, tenantId);
        } catch (Exception e) {
            log.error("Failed to deliver event {} to apps: {}", event, e.getMessage());
        }

        // Deliver to merchant-registered webhooks
        List<Webhook> webhooks = webhookRepository.findAllByTenantIdAndEventAndActiveTrue(tenantId, event)
                .stream()
                .filter(w -> !w.isPaused())
                .collect(Collectors.toList());

        if (webhooks.isEmpty()) {
            log.debug("No active webhooks found for event {} in tenant {}", event, tenantId);
            return;
        }

        // Generate unique event ID
        String eventId = "evt_" + UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();

        for (Webhook webhook : webhooks) {
            try {
                // Build the standard payload
                WebhookEventPayload payload = WebhookEventPayload.builder()
                        .id(eventId)
                        .type(WebhookEventPayload.eventToString(event))
                        .apiVersion(webhook.getApiVersion())
                        .createdAt(now.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .tenant(WebhookEventPayload.TenantInfo.builder()
                                .id(String.valueOf(tenantId))
                                .slug(tenantSlug)
                                .build())
                        .data(data)
                        .build();

                deliverWebhook(webhook, eventId, event, payload, tenantId, 1);
            } catch (Exception e) {
                log.error("Failed to trigger webhook {} for event {}: {}",
                        webhook.getId(), event, e.getMessage());
            }
        }
    }

    // ==================== DELIVERY MANAGEMENT ====================

    @Override
    @Transactional(readOnly = true)
    public List<WebhookDeliveryResponse> getDeliveryHistory(Long webhookId, Long tenantId) {
        // Verify webhook belongs to tenant
        webhookRepository.findByIdAndTenantId(webhookId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook", webhookId));

        return deliveryRepository.findAllByWebhookIdAndTenantId(webhookId, tenantId).stream()
                .map(this::mapToDeliveryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebhookDeliveryResponse> getRecentDeliveries(Long tenantId) {
        return deliveryRepository.findTop100ByTenantIdOrderByTriggeredAtDesc(tenantId).stream()
                .map(this::mapToDeliveryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WebhookDeliveryResponse getDeliveryById(Long id, Long tenantId) {
        return deliveryRepository.findByIdAndTenantId(id, tenantId)
                .map(this::mapToDeliveryResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook delivery", id));
    }

    @Override
    @Transactional(readOnly = true)
    public WebhookDeliveryResponse getDeliveryByEventId(String eventId, Long tenantId) {
        return deliveryRepository.findByEventIdAndTenantId(eventId, tenantId)
                .map(this::mapToDeliveryResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook delivery", eventId));
    }

    @Override
    @Transactional
    public WebhookDeliveryResponse retryDelivery(Long deliveryId, Long tenantId) {
        WebhookDelivery delivery = deliveryRepository.findByIdAndTenantId(deliveryId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook delivery", deliveryId));

        if (delivery.getStatus() == WebhookDelivery.DeliveryStatus.SUCCESS) {
            throw new RuntimeException("Cannot retry successful delivery");
        }

        // Reset for retry
        delivery.setStatus(WebhookDelivery.DeliveryStatus.PENDING);
        delivery.setNextRetryAt(LocalDateTime.now());
        deliveryRepository.save(delivery);

        // Attempt delivery
        return performDelivery(delivery);
    }

    // ==================== TESTING ====================

    @Override
    @Transactional
    public WebhookDeliveryResponse testWebhook(Long id, Long tenantId) {
        Webhook webhook = webhookRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook", id));

        String eventId = "evt_test_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        LocalDateTime now = LocalDateTime.now();

        WebhookEventPayload testPayload = WebhookEventPayload.builder()
                .id(eventId)
                .type("test")
                .apiVersion(webhook.getApiVersion())
                .createdAt(now.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .tenant(WebhookEventPayload.TenantInfo.builder()
                        .id(String.valueOf(tenantId))
                        .slug("test-store")
                        .build())
                .data(Map.of(
                        "message", "This is a test webhook delivery",
                        "timestamp", now.toString()
                ))
                .build();

        return deliverWebhook(webhook, eventId, webhook.getEvent(), testPayload, tenantId, 1);
    }

    @Override
    public List<Webhook.WebhookEvent> getAvailableEvents() {
        return Arrays.asList(Webhook.WebhookEvent.values());
    }

    // ==================== DELIVERY LOGIC ====================

    private WebhookDeliveryResponse deliverWebhook(Webhook webhook, String eventId,
                                                    Webhook.WebhookEvent eventType,
                                                    WebhookEventPayload payload,
                                                    Long tenantId, int attemptNumber) {
        LocalDateTime now = LocalDateTime.now();

        WebhookDelivery delivery = WebhookDelivery.builder()
                .webhook(webhook)
                .eventId(eventId)
                .eventType(eventType)
                .apiVersion(webhook.getApiVersion())
                .status(WebhookDelivery.DeliveryStatus.SENDING)
                .attemptNumber(attemptNumber)
                .maxAttempts(webhook.getMaxRetries())
                .triggeredAt(now)
                .build();
        delivery.setTenantId(tenantId);

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            delivery.setPayload(jsonPayload);

            return performDelivery(delivery);

        } catch (Exception e) {
            log.error("Failed to serialize webhook payload: {}", e.getMessage());
            delivery.setStatus(WebhookDelivery.DeliveryStatus.FAILED);
            delivery.setErrorMessage("Failed to serialize payload: " + e.getMessage());
            return mapToDeliveryResponse(deliveryRepository.save(delivery));
        }
    }

    private WebhookDeliveryResponse performDelivery(WebhookDelivery delivery) {
        Webhook webhook = delivery.getWebhook();
        long startTime = System.currentTimeMillis();
        long timestamp = System.currentTimeMillis() / 1000;

        try {
            // Create signature
            String signature = createSignature(delivery.getPayload(), webhook.getSecret(), timestamp);

            // Build headers per specification
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Webhook-Signature", signature);
            headers.set("X-Webhook-Event", WebhookEventPayload.eventToString(delivery.getEventType()));
            headers.set("X-Webhook-Timestamp", String.valueOf(timestamp));
            headers.set("X-Webhook-Id", delivery.getEventId());

            // Add custom headers if any
            if (webhook.getHeaders() != null && !webhook.getHeaders().isEmpty()) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, String> customHeaders = objectMapper.readValue(
                            webhook.getHeaders(), Map.class);
                    customHeaders.forEach(headers::set);
                } catch (Exception e) {
                    log.warn("Failed to parse custom headers: {}", e.getMessage());
                }
            }

            HttpEntity<String> request = new HttpEntity<>(delivery.getPayload(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    webhook.getUrl(),
                    HttpMethod.POST,
                    request,
                    String.class
            );

            delivery.setResponseCode(response.getStatusCode().value());
            delivery.setResponseBody(truncateResponse(response.getBody()));
            delivery.setDurationMs(System.currentTimeMillis() - startTime);

            if (response.getStatusCode().is2xxSuccessful()) {
                delivery.setStatus(WebhookDelivery.DeliveryStatus.SUCCESS);
                delivery.setDeliveredAt(LocalDateTime.now());
            } else if (is4xxPermanentFailure(response.getStatusCode().value())) {
                delivery.setStatus(WebhookDelivery.DeliveryStatus.FAILED);
                delivery.setErrorMessage("Permanent failure: " + response.getStatusCode());
            } else {
                scheduleRetry(delivery);
            }

        } catch (Exception e) {
            log.error("Webhook delivery failed: {}", e.getMessage());
            delivery.setDurationMs(System.currentTimeMillis() - startTime);
            delivery.setErrorMessage(e.getMessage());
            scheduleRetry(delivery);
        }

        return mapToDeliveryResponse(deliveryRepository.save(delivery));
    }

    private void scheduleRetry(WebhookDelivery delivery) {
        if (delivery.getAttemptNumber() >= delivery.getMaxAttempts()) {
            delivery.setStatus(WebhookDelivery.DeliveryStatus.EXHAUSTED);
            delivery.setErrorMessage("All retry attempts exhausted");
        } else {
            delivery.setStatus(WebhookDelivery.DeliveryStatus.RETRYING);
            // Exponential backoff: 2^attempt minutes (2, 4, 8, 16, 32...)
            long delayMinutes = (long) Math.pow(2, delivery.getAttemptNumber());
            delivery.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
            delivery.setAttemptNumber(delivery.getAttemptNumber() + 1);
        }
    }

    private boolean is4xxPermanentFailure(int statusCode) {
        // 429 (Too Many Requests) should be retried
        return statusCode >= 400 && statusCode < 500 && statusCode != 429;
    }

    // ==================== HELPER METHODS ====================

    private String generateSecret() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String createSignature(String payload, String secret, long timestamp) {
        try {
            String signaturePayload = timestamp + "." + payload;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(signaturePayload.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create signature", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String truncateResponse(String response) {
        if (response == null) return null;
        if (response.length() <= MAX_RESPONSE_BODY_LENGTH) return response;
        return response.substring(0, MAX_RESPONSE_BODY_LENGTH) + "... (truncated)";
    }

    private WebhookResponse mapToResponse(Webhook webhook, Long tenantId) {
        long totalDeliveries = deliveryRepository.countByWebhookIdAndTenantId(webhook.getId(), tenantId);
        long successCount = deliveryRepository.countByWebhookIdAndStatusAndTenantId(
                webhook.getId(), WebhookDelivery.DeliveryStatus.SUCCESS, tenantId);
        long failureCount = deliveryRepository.countByWebhookIdAndStatusAndTenantId(
                webhook.getId(), WebhookDelivery.DeliveryStatus.FAILED, tenantId) +
                deliveryRepository.countByWebhookIdAndStatusAndTenantId(
                        webhook.getId(), WebhookDelivery.DeliveryStatus.EXHAUSTED, tenantId);

        LocalDateTime lastDeliveryAt = deliveryRepository
                .findTopByWebhookIdAndTenantIdOrderByTriggeredAtDesc(webhook.getId(), tenantId)
                .map(WebhookDelivery::getTriggeredAt)
                .orElse(null);

        return WebhookResponse.builder()
                .id(webhook.getId())
                .name(webhook.getName())
                .url(webhook.getUrl())
                .event(webhook.getEvent())
                .apiVersion(webhook.getApiVersion())
                .active(webhook.isActive())
                .paused(webhook.isPaused())
                .maxRetries(webhook.getMaxRetries())
                .headers(webhook.getHeaders())
                .createdAt(webhook.getCreatedAt())
                .totalDeliveries(totalDeliveries)
                .successCount(successCount)
                .failureCount(failureCount)
                .lastDeliveryAt(lastDeliveryAt)
                .build();
    }

    private WebhookDeliveryResponse mapToDeliveryResponse(WebhookDelivery delivery) {
        return WebhookDeliveryResponse.builder()
                .id(delivery.getId())
                .eventId(delivery.getEventId())
                .webhookId(delivery.getWebhook().getId())
                .webhookName(delivery.getWebhook().getName())
                .webhookUrl(delivery.getWebhook().getUrl())
                .eventType(delivery.getEventType())
                .apiVersion(delivery.getApiVersion())
                .status(delivery.getStatus())
                .responseCode(delivery.getResponseCode())
                .responseBody(delivery.getResponseBody())
                .attemptNumber(delivery.getAttemptNumber())
                .maxAttempts(delivery.getMaxAttempts())
                .durationMs(delivery.getDurationMs())
                .errorMessage(delivery.getErrorMessage())
                .triggeredAt(delivery.getTriggeredAt())
                .deliveredAt(delivery.getDeliveredAt())
                .nextRetryAt(delivery.getNextRetryAt())
                .build();
    }
}
