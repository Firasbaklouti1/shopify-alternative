package com.firas.saas.common.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class TenantEntity extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
}
