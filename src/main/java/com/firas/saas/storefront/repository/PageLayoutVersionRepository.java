package com.firas.saas.storefront.repository;

import com.firas.saas.storefront.entity.PageLayout;
import com.firas.saas.storefront.entity.PageLayoutVersion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageLayoutVersionRepository extends JpaRepository<PageLayoutVersion, Long> {

    /**
     * Find all versions for a page layout, ordered by version number descending
     */
    List<PageLayoutVersion> findByPageLayoutOrderByVersionNumberDesc(PageLayout pageLayout);

    /**
     * Find limited versions for a page layout (for UI display)
     */
    List<PageLayoutVersion> findByPageLayoutOrderByVersionNumberDesc(PageLayout pageLayout, Pageable pageable);

    /**
     * Find specific version
     */
    Optional<PageLayoutVersion> findByPageLayoutAndVersionNumber(PageLayout pageLayout, Integer versionNumber);

    /**
     * Get the latest version number for a page layout
     */
    @Query("SELECT MAX(v.versionNumber) FROM PageLayoutVersion v WHERE v.pageLayout = :pageLayout")
    Optional<Integer> findMaxVersionNumber(@Param("pageLayout") PageLayout pageLayout);

    /**
     * Delete old versions, keeping only the most recent N versions
     * Used for cleanup to prevent database bloat
     */
    @Modifying
    @Query("DELETE FROM PageLayoutVersion v WHERE v.pageLayout = :pageLayout AND v.versionNumber NOT IN " +
           "(SELECT v2.versionNumber FROM PageLayoutVersion v2 WHERE v2.pageLayout = :pageLayout ORDER BY v2.versionNumber DESC LIMIT :keepCount)")
    void deleteOldVersions(@Param("pageLayout") PageLayout pageLayout, @Param("keepCount") int keepCount);

    /**
     * Count versions for a page layout
     */
    long countByPageLayout(PageLayout pageLayout);
}
