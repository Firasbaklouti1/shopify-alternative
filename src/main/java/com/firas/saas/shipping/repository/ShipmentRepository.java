package com.firas.saas.shipping.repository;

import com.firas.saas.common.base.BaseRepository;
import com.firas.saas.shipping.entity.Shipment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends BaseRepository<Shipment> {

    List<Shipment> findAllByTenantId(Long tenantId);

    List<Shipment> findAllByOrderIdAndTenantId(Long orderId, Long tenantId);

    Optional<Shipment> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Shipment> findByTrackingNumberAndTenantId(String trackingNumber, Long tenantId);

    List<Shipment> findAllByStatusAndTenantId(Shipment.ShipmentStatus status, Long tenantId);
}

