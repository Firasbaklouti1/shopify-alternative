package com.firas.saas.tenant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {
    private Long id;
    private String name;
    private String slug;
    private String ownerEmail;
    private boolean active;
    private LocalDateTime createdAt;
}
