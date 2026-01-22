package com.firas.saas.storefront.repository;

import com.firas.saas.storefront.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    /**
     * Find all active themes ordered by display order
     */
    List<Theme> findByActiveTrueOrderByDisplayOrderAsc();

    /**
     * Find theme by name
     */
    Optional<Theme> findByName(String name);

    /**
     * Check if theme name exists
     */
    boolean existsByName(String name);
}
