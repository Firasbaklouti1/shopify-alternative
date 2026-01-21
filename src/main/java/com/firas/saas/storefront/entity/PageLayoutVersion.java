package com.firas.saas.storefront.entity;

import com.firas.saas.common.base.TenantEntity;
import com.firas.saas.common.util.JsonMapConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

/**
 * Stores historical versions of page layouts for undo/rollback functionality.
 * Limited to last 10 versions per page to prevent database bloat.
 */
@Entity
@Table(name = "page_layout_versions",
       indexes = @Index(columnList = "page_layout_id, version_number DESC"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageLayoutVersion extends TenantEntity {

    /**
     * Reference to the parent PageLayout
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_layout_id", nullable = false)
    private PageLayout pageLayout;

    /**
     * Sequential version number (1, 2, 3, ...)
     */
    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    /**
     * Snapshot of the layout at this version
     */
    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "TEXT", nullable = false)
    private Map<String, Object> layoutSnapshot;

    /**
     * Optional description of what changed in this version
     */
    private String changeDescription;

    /**
     * Email of the user who made this change
     */
    private String changedBy;
}
