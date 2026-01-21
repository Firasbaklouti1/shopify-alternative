package com.firas.saas.storefront.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for theme (public and merchant API)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeResponse {

    private Long id;
    private String name;
    private String description;
    private Map<String, Object> cssVariables;
    private String previewImageUrl;
    private boolean active;
    private int displayOrder;
}
