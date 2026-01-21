package com.ordersync.repository;

import com.ordersync.entity.SyncedOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SyncedOrderRepository extends JpaRepository<SyncedOrder, Long> {

    Optional<SyncedOrder> findByPlatformOrderId(Long platformOrderId);

    boolean existsByPlatformOrderId(Long platformOrderId);

    List<SyncedOrder> findByTenantId(Long tenantId);

    List<SyncedOrder> findBySyncStatus(SyncedOrder.SyncStatus status);

    List<SyncedOrder> findByTenantIdAndSyncStatus(Long tenantId, SyncedOrder.SyncStatus status);
}
