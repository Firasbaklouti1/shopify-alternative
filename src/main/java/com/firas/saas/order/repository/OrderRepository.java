package com.firas.saas.order.repository;

import com.firas.saas.common.base.BaseRepository;
import com.firas.saas.order.entity.Order;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends BaseRepository<Order> {
    List<Order> findAllByTenantId(Long tenantId);
    List<Order> findAllByCustomerEmailAndTenantId(String email, Long tenantId);
    Optional<Order> findByOrderNumberAndTenantId(String orderNumber, Long tenantId);
    Optional<Order> findByIdAndTenantId(Long id, Long tenantId);
}
