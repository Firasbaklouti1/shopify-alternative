package com.firas.saas.app.dto;

import com.firas.saas.app.entity.App;
import com.firas.saas.app.entity.AppScope;
import com.firas.saas.app.entity.AppStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for app details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppResponse {

    private Long id;
    private String name;
    private String description;
    private String developerName;
    private String clientId;
    private String webhookUrl;
    private Set<AppScope> declaredScopes;
    private AppStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Map from App entity to response DTO.
     */
    public static AppResponse fromEntity(App app) {
        return AppResponse.builder()
                .id(app.getId())
                .name(app.getName())
                .description(app.getDescription())
                .developerName(app.getDeveloperName())
                .clientId(app.getClientId())
                .webhookUrl(app.getWebhookUrl())
                .declaredScopes(app.getDeclaredScopes())
                .status(app.getStatus())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}
