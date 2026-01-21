package com.firas.saas.app.dto;

import com.firas.saas.app.entity.AppScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Request DTO for installing an app.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstallAppRequest {

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotBlank(message = "Client secret is required")
    private String clientSecret;

    @NotEmpty(message = "At least one scope must be granted")
    private Set<AppScope> grantedScopes;
}
