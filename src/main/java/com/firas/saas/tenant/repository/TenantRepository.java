package com.firas.saas.tenant.repository;

import com.firas.saas.common.base.BaseRepository;
import com.firas.saas.tenant.entity.Tenant;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends BaseRepository<Tenant> {
    Optional<Tenant> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByName(String name);
}
