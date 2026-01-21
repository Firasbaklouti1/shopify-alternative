package com.firas.saas.app.dto;

import com.firas.saas.app.entity.AppAccessToken;
import com.firas.saas.app.entity.AppScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for access token details (without the actual token value for security).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessTokenResponse {

    private Long tokenId;
    private Long installationId;
    private Set<AppScope> scopes;
    private LocalDateTime expiresAt;
    private boolean revoked;
    private boolean valid;
    private LocalDateTime createdAt;

    /**
     * Map from AppAccessToken entity to response DTO.
     */
    public static AccessTokenResponse fromEntity(AppAccessToken token) {
        return AccessTokenResponse.builder()
                .tokenId(token.getId())
                .installationId(token.getInstallation().getId())
                .scopes(token.getScopes())
                .expiresAt(token.getExpiresAt())
                .revoked(token.isRevoked())
                .valid(token.isValid())
                .createdAt(token.getCreatedAt())
                .build();
    }
}
