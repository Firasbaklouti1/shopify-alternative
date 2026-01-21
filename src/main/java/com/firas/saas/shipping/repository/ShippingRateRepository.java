package com.firas.saas.shipping.repository;

import com.firas.saas.common.base.BaseRepository;
import com.firas.saas.shipping.entity.ShippingRate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingRateRepository extends BaseRepository<ShippingRate> {

    List<ShippingRate> findAllByTenantId(Long tenantId);

    List<ShippingRate> findAllByZoneIdAndTenantId(Long zoneId, Long tenantId);

    List<ShippingRate> findAllByZoneIdAndTenantIdAndActiveTrue(Long zoneId, Long tenantId);

    Optional<ShippingRate> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByNameAndZoneIdAndTenantId(String name, Long zoneId, Long tenantId);
}

