package com.firas.saas.app.dto;

import com.firas.saas.app.entity.AppInstallation;
import com.firas.saas.app.entity.AppScope;
import com.firas.saas.app.entity.InstallationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for app installation details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppInstallationResponse {

    private Long installationId;
    private Long appId;
    private String appName;
    private String appClientId;
    private Set<AppScope> grantedScopes;
    private InstallationStatus status;
    private Long installedByUserId;
    private LocalDateTime installedAt;

    /**
     * Map from AppInstallation entity to response DTO.
     */
    public static AppInstallationResponse fromEntity(AppInstallation installation) {
        return AppInstallationResponse.builder()
                .installationId(installation.getId())
                .appId(installation.getApp().getId())
                .appName(installation.getApp().getName())
                .appClientId(installation.getApp().getClientId())
                .grantedScopes(installation.getGrantedScopes())
                .status(installation.getStatus())
                .installedByUserId(installation.getInstalledByUserId())
                .installedAt(installation.getCreatedAt())
                .build();
    }
}
