package com.firas.saas.shipping.repository;

import com.firas.saas.common.base.BaseRepository;
import com.firas.saas.shipping.entity.ShippingZone;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingZoneRepository extends BaseRepository<ShippingZone> {

    List<ShippingZone> findAllByTenantId(Long tenantId);

    List<ShippingZone> findAllByTenantIdAndActiveTrue(Long tenantId);

    Optional<ShippingZone> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByNameAndTenantId(String name, Long tenantId);
}

