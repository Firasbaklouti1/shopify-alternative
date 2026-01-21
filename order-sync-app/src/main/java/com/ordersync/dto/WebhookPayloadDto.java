package com.ordersync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for incoming webhook payloads.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookPayloadDto {

    private String eventId;
    private String eventType;
    private Long tenantId;
    private String tenantSlug;
    private String timestamp;
    private Map<String, Object> data;
}
