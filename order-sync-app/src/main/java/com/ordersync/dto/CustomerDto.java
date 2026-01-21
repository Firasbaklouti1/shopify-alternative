package com.ordersync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO matching the platform's customer response structure.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {

    private Long id;
    private Long tenantId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String createdAt;
    private String updatedAt;
}
