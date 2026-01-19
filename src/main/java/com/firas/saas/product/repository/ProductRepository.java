package com.firas.saas.product.repository;

import com.firas.saas.common.base.BaseRepository;
import com.firas.saas.product.entity.Product;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends BaseRepository<Product> {
    List<Product> findAllByTenantId(Long tenantId);
    List<Product> findByCategoryIdAndTenantId(Long categoryId, Long tenantId);
    Optional<Product> findBySlugAndTenantId(String slug, Long tenantId);
    boolean existsBySlugAndTenantId(String slug, Long tenantId);
}
