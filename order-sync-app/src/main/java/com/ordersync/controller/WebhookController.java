package com.ordersync.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordersync.entity.WebhookEvent;
import com.ordersync.repository.WebhookEventRepository;
import com.ordersync.service.AppTokenManager;
import com.ordersync.service.OrderSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

/**
 * Controller to receive webhook events from the platform.
 * This is the endpoint the platform sends events to (http://localhost:8081/webhooks).
 */
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookEventRepository webhookEventRepository;
    private final OrderSyncService orderSyncService;
    private final AppTokenManager tokenManager;
    private final ObjectMapper objectMapper;

    @Value("${webhook.signature.tolerance-seconds:300}")
    private int signatureToleranceSeconds;

    /**
     * Receive webhook events from the platform.
     * 
     * Headers:
     * - X-Webhook-Event: The event type (e.g., ORDER_CREATED)
     * - X-Webhook-Signature: HMAC signature for verification
     * - X-Webhook-Event-Id: Unique event ID for idempotency
     * - X-App-Client-Id: The app's client ID
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestHeader(value = "X-Webhook-Event", required = false) String eventType,
            @RequestHeader(value = "X-Webhook-Signature", required = false) String signature,
            @RequestHeader(value = "X-Webhook-Event-Id", required = false) String eventId,
            @RequestHeader(value = "X-App-Client-Id", required = false) String clientId,
            @RequestBody String rawPayload) {

        log.info("=== WEBHOOK RECEIVED ===");
        log.info("Event Type: {}", eventType);
        log.info("Event ID: {}", eventId);
        log.info("Client ID: {}", clientId);
        log.debug("Payload: {}", rawPayload);

        // Generate event ID if not provided
        if (eventId == null || eventId.isEmpty()) {
            eventId = "generated_" + System.currentTimeMillis();
        }

        // Check for duplicate (idempotency)
        if (webhookEventRepository.existsByEventId(eventId)) {
            log.info("Duplicate webhook received, ignoring: {}", eventId);
            return ResponseEntity.ok(Map.of(
                    "status", "ignored",
                    "reason", "duplicate",
                    "eventId", eventId
            ));
        }

        // Parse payload
        Map<String, Object> payload;
        Long tenantId = null;
        try {
            payload = objectMapper.readValue(rawPayload, new TypeReference<Map<String, Object>>() {});
            tenantId = extractTenantId(payload);
        } catch (Exception e) {
            log.error("Failed to parse webhook payload: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Invalid JSON payload"
            ));
        }

        // Create webhook event record
        WebhookEvent webhookEvent = WebhookEvent.builder()
                .eventId(eventId)
                .eventType(eventType != null ? eventType : "UNKNOWN")
                .tenantId(tenantId)
                .payload(rawPayload)
                .signature(signature)
                .signatureValid(true) // For testing, we're not validating signature strictly
                .status(WebhookEvent.ProcessingStatus.RECEIVED)
                .build();

        webhookEventRepository.save(webhookEvent);
        log.info("Stored webhook event: {} (type: {})", eventId, eventType);

        // Process the event
        try {
            processWebhookEvent(webhookEvent, payload, tenantId);
            webhookEvent.setStatus(WebhookEvent.ProcessingStatus.PROCESSED);
            webhookEvent.setProcessedAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to process webhook: {}", e.getMessage(), e);
            webhookEvent.setStatus(WebhookEvent.ProcessingStatus.FAILED);
            webhookEvent.setErrorMessage(e.getMessage());
        }

        webhookEventRepository.save(webhookEvent);

        return ResponseEntity.ok(Map.of(
                "status", "received",
                "eventId", eventId,
                "eventType", eventType != null ? eventType : "UNKNOWN"
        ));
    }

    /**
     * Process webhook based on event type.
     */
    private void processWebhookEvent(WebhookEvent webhookEvent, Map<String, Object> payload, Long tenantId) {
        String eventType = webhookEvent.getEventType();

        switch (eventType) {
            case "ORDER_CREATED":
            case "ORDER_PAID":
                log.info("Processing order event: {}", eventType);
                Map<String, Object> data = extractData(payload);
                if (data != null && tenantId != null) {
                    orderSyncService.processOrderEvent(eventType, data, tenantId);
                }
                break;

            case "ORDER_CANCELLED":
                log.info("Order cancelled event received - would cancel fulfillment");
                // In a real app, we'd cancel the fulfillment
                break;

            case "APP_INSTALLED":
                log.info("App installed event received");
                handleAppInstalled(payload, tenantId);
                break;

            case "APP_UNINSTALLED":
                log.info("App uninstalled event received");
                handleAppUninstalled(tenantId);
                break;

            default:
                log.info("Unhandled event type: {}", eventType);
                webhookEvent.setStatus(WebhookEvent.ProcessingStatus.IGNORED);
        }
    }

    /**
     * Handle APP_INSTALLED event - store the access token.
     */
    private void handleAppInstalled(Map<String, Object> payload, Long tenantId) {
        if (tenantId == null) {
            log.warn("No tenant ID in APP_INSTALLED event");
            return;
        }

        // The access token should be passed during installation
        // For now, log that we received the event
        log.info("App installed for tenant {}. Configure access token via /config endpoint.", tenantId);
    }

    /**
     * Handle APP_UNINSTALLED event - revoke access.
     */
    private void handleAppUninstalled(Long tenantId) {
        if (tenantId != null) {
            tokenManager.removeAccessToken(tenantId);
            log.info("Removed access token for tenant {} due to uninstall", tenantId);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractData(Map<String, Object> payload) {
        Object data = payload.get("data");
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        // If no nested data, treat the whole payload as data
        return payload;
    }

    private Long extractTenantId(Map<String, Object> payload) {
        Object tenantIdObj = payload.get("tenantId");
        if (tenantIdObj instanceof Number) {
            return ((Number) tenantIdObj).longValue();
        }
        if (tenantIdObj instanceof String) {
            try {
                return Long.parseLong((String) tenantIdObj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Verify HMAC signature (optional, for production use).
     */
    private boolean verifySignature(String signature, String payload, String secret) {
        if (signature == null || signature.isEmpty()) {
            return false;
        }

        try {
            // Parse signature format: t=timestamp,v1=hash
            String[] parts = signature.split(",");
            String timestamp = null;
            String hash = null;

            for (String part : parts) {
                if (part.startsWith("t=")) {
                    timestamp = part.substring(2);
                } else if (part.startsWith("v1=")) {
                    hash = part.substring(3);
                }
            }

            if (timestamp == null || hash == null) {
                return false;
            }

            // Check timestamp tolerance
            long timestampLong = Long.parseLong(timestamp);
            long now = Instant.now().getEpochSecond();
            if (Math.abs(now - timestampLong) > signatureToleranceSeconds) {
                log.warn("Webhook signature timestamp too old");
                return false;
            }

            // Compute expected signature
            String signedPayload = timestamp + "." + payload;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expectedHash = mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
            String expectedHashHex = bytesToHex(expectedHash);

            return expectedHashHex.equals(hash);
        } catch (Exception e) {
            log.error("Error verifying webhook signature: {}", e.getMessage());
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
