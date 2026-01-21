package com.firas.saas.webhook.dto;

import com.firas.saas.webhook.entity.Webhook;
import com.firas.saas.webhook.entity.WebhookDelivery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookDeliveryResponse {

    private Long id;
    private String eventId;
    private Long webhookId;
    private String webhookName;
    private String webhookUrl;
    private Webhook.WebhookEvent eventType;
    private String apiVersion;
    private WebhookDelivery.DeliveryStatus status;
    private Integer responseCode;
    private String responseBody; // Truncated
    private Integer attemptNumber;
    private Integer maxAttempts;
    private Long durationMs;
    private String errorMessage;
    private LocalDateTime triggeredAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime nextRetryAt;
}
