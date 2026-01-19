package com.firas.saas.order.repository;

import com.firas.saas.common.base.BaseRepository;
import com.firas.saas.order.entity.Cart;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends BaseRepository<Cart> {
    Optional<Cart> findByCustomerEmailAndTenantId(String email, Long tenantId);
}
