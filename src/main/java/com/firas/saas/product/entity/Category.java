package com.firas.saas.product.entity;

import com.firas.saas.common.base.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends TenantEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String slug;

    private String description;
}
