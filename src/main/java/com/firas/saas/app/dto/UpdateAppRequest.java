package com.firas.saas.app.dto;

import com.firas.saas.app.entity.AppScope;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Request DTO for updating an existing app.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAppRequest {

    @Size(min = 3, max = 100, message = "App name must be between 3 and 100 characters")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Size(min = 2, max = 100, message = "Developer name must be between 2 and 100 characters")
    private String developerName;

    @Pattern(regexp = "^https?://.*", message = "Webhook URL must be a valid HTTP(S) URL")
    private String webhookUrl;

    private Set<AppScope> declaredScopes;
}
