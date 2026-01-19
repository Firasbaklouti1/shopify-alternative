package com.firas.saas.customer.repository;

import com.firas.saas.common.base.BaseRepository;
import com.firas.saas.customer.entity.Customer;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends BaseRepository<Customer> {
    List<Customer> findAllByTenantId(Long tenantId);
    Optional<Customer> findByEmailAndTenantId(String email, Long tenantId);
    boolean existsByEmailAndTenantId(String email, Long tenantId);
}
