package com.firas.saas.product.repository;

import com.firas.saas.common.base.BaseRepository;
import com.firas.saas.product.entity.Category;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends BaseRepository<Category> {
    List<Category> findAllByTenantId(Long tenantId);
    Optional<Category> findBySlugAndTenantId(String slug, Long tenantId);
    boolean existsByNameAndTenantId(String name, Long tenantId);
}
