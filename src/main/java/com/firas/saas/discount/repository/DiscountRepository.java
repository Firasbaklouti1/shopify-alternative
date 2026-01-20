package com.firas.saas.discount.repository;

import com.firas.saas.common.base.BaseRepository;
import com.firas.saas.discount.entity.Discount;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends BaseRepository<Discount> {

    List<Discount> findAllByTenantId(Long tenantId);

    List<Discount> findAllByTenantIdAndActiveTrue(Long tenantId);

    Optional<Discount> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Discount> findByCodeAndTenantId(String code, Long tenantId);

    boolean existsByCodeAndTenantId(String code, Long tenantId);

    List<Discount> findByTenantIdAndActiveTrueAndExpiresAtAfter(Long tenantId, LocalDateTime now);
}

