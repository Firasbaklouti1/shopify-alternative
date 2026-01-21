package com.firas.saas.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firas.saas.app.entity.*;
import com.firas.saas.app.repository.AppInstallationRepository;
import com.firas.saas.webhook.entity.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service to deliver webhook events to installed apps based on their granted scopes.
 * This complements the existing WebhookService by handling app-specific delivery.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppWebhookService {

    private final AppInstallationRepository installationRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Deliver a webhook event to all apps installed in the tenant that have the required scope.
     * APP_INSTALLED and APP_UNINSTALLED are delivered regardless of scope.
     */
    @Async
    public void deliverToApps(Webhook.WebhookEvent event, Map<String, Object> data, Long tenantId) {
        // Get required scope for this event
        AppScope requiredScope = AppScope.getRequiredScopeForEvent(event);

        // Get all active installations for this tenant
        List<AppInstallation> installations = installationRepository
                .findAllByTenantIdAndStatus(tenantId, InstallationStatus.ACTIVE);

        if (installations.isEmpty()) {
            log.debug("No active app installations for tenant {} to receive event {}", tenantId, event);
            return;
        }

        String eventId = "app_evt_" + UUID.randomUUID().toString().replace("-", "");

        for (AppInstallation installation : installations) {
            try {
                // Check if app should receive this event
                if (shouldDeliverToApp(installation, event, requiredScope)) {
                    deliverToApp(installation, event, eventId, data, tenantId);
                } else {
                    log.debug("App {} does not have scope for event {} in tenant {}",
                            installation.getApp().getName(), event, tenantId);
                }
            } catch (Exception e) {
                log.error("Failed to deliver event {} to app {}: {}",
                        event, installation.getApp().getName(), e.getMessage());
            }
        }
    }

    /**
     * Check if an app installation should receive a specific event.
     */
    private boolean shouldDeliverToApp(AppInstallation installation, Webhook.WebhookEvent event, AppScope requiredScope) {
        // Always deliver app lifecycle events
        if (event == Webhook.WebhookEvent.APP_INSTALLED || event == Webhook.WebhookEvent.APP_UNINSTALLED) {
            return true;
        }

        // No scope required for this event
        if (requiredScope == null) {
            return true;
        }

        // Check if app has the required scope
        return installation.hasScope(requiredScope);
    }

    /**
     * Deliver webhook to a specific app installation.
     */
    private void deliverToApp(AppInstallation installation, Webhook.WebhookEvent event,
                               String eventId, Map<String, Object> data, Long tenantId) {
        App app = installation.getApp();
        String webhookUrl = app.getWebhookUrl();

        log.info("Delivering {} to app {} at {}", event, app.getName(), webhookUrl);

        try {
            // Build payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", eventId);
            payload.put("type", event.name().toLowerCase());
            payload.put("createdAt", LocalDateTime.now().atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            payload.put("appId", app.getId());
            payload.put("clientId", app.getClientId());
            payload.put("installationId", installation.getId());
            payload.put("tenantId", tenantId);
            payload.put("data", data);

            String payloadJson = objectMapper.writeValueAsString(payload);

            // Generate signature using app's client secret hash as the key
            // In production, you'd use a separate webhook secret
            long timestamp = System.currentTimeMillis() / 1000;
            String signaturePayload = timestamp + "." + payloadJson;
            String signature = generateSignature(signaturePayload, app.getClientSecretHash());

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Webhook-Event", event.name());
            headers.set("X-Webhook-Signature", "t=" + timestamp + ",v1=" + signature);
            headers.set("X-Webhook-Event-Id", eventId);
            headers.set("X-App-Client-Id", app.getClientId());
            headers.set("User-Agent", "SaaS-Platform-Webhook/1.0");

            HttpEntity<String> request = new HttpEntity<>(payloadJson, headers);

            // Send webhook
            ResponseEntity<String> response = restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.POST,
                    request,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully delivered {} to app {} (status: {})",
                        event, app.getName(), response.getStatusCode());
            } else {
                log.warn("Unexpected response from app {} for event {}: {}",
                        app.getName(), event, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to deliver {} to app {}: {}", event, app.getName(), e.getMessage());
            // In production, you'd queue for retry
        }
    }

    /**
     * Generate HMAC-SHA256 signature for webhook payload.
     */
    private String generateSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Failed to generate webhook signature: {}", e.getMessage());
            return "";
        }
    }
}
