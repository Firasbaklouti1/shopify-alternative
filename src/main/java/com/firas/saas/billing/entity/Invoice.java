package com.firas.saas.billing.entity;

import com.firas.saas.common.base.TenantEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Column;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice extends TenantEntity {

    // tenantId is inherited from TenantEntity

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String status; // PAID, PENDING, FAILED

    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime issuedAt;
}
