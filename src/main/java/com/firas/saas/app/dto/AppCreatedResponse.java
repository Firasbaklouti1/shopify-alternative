package com.firas.saas.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for app creation - includes the plaintext client secret (shown only once).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppCreatedResponse {

    private AppResponse app;

    /**
     * The plaintext client secret - shown only once at creation.
     * Store this securely; it cannot be retrieved again.
     */
    private String clientSecret;

    /**
     * Warning message for the developer.
     */
    @Builder.Default
    private String warning = "Save the clientSecret now. It will not be shown again.";
}
