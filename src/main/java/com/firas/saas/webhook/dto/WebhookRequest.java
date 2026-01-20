package com.firas.saas.webhook.dto;

import com.firas.saas.webhook.entity.Webhook;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRequest {

    @NotBlank(message = "Webhook name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    private String name;

    @NotBlank(message = "URL is required")
    @Pattern(regexp = "^https://.*", message = "URL must use HTTPS")
    private String url;

    @NotNull(message = "Event type is required")
    private Webhook.WebhookEvent event;

    @Builder.Default
    private String apiVersion = "v1";

    @Min(value = 1, message = "Max retries must be at least 1")
    @Max(value = 10, message = "Max retries cannot exceed 10")
    @Builder.Default
    private Integer maxRetries = 5;

    private String headers; // JSON string of custom headers

    @Builder.Default
    private boolean active = true;
}
