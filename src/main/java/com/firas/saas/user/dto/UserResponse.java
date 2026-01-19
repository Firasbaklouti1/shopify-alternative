package com.firas.saas.user.dto;

import com.firas.saas.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private Long tenantId;
    private String tenantName;
    private boolean enabled;
    private LocalDateTime createdAt;
}
