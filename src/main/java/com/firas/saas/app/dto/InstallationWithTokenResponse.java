package com.firas.saas.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for app installation with access token.
 * This is returned after successful installation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstallationWithTokenResponse {

    private AppInstallationResponse installation;

    /**
     * The access token to use for API calls.
     */
    private String accessToken;

    /**
     * When the token expires.
     */
    private LocalDateTime tokenExpiresAt;

    /**
     * Info message for the developer.
     */
    @Builder.Default
    private String info = "Use this accessToken in the Authorization header: Bearer <token>";
}
