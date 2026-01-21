package com.firas.saas.webhook.dto;

import com.firas.saas.webhook.entity.Webhook;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResponse {

    private Long id;
    private String name;
    private String url;
    private Webhook.WebhookEvent event;
    private String apiVersion;
    private boolean active;
    private boolean paused;
    private Integer maxRetries;
    private String headers;
    private LocalDateTime createdAt;

    // Stats
    private Long totalDeliveries;
    private Long successCount;
    private Long failureCount;
    private LocalDateTime lastDeliveryAt;
}
