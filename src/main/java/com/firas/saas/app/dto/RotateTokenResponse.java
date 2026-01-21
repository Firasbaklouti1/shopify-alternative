package com.firas.saas.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for token rotation - includes the new access token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RotateTokenResponse {

    /**
     * The new access token to use for API calls.
     */
    private String accessToken;

    /**
     * When the new token expires.
     */
    private LocalDateTime expiresAt;

    /**
     * Info message.
     */
    @Builder.Default
    private String info = "Old token has been revoked. Use this new token for API calls.";
}
