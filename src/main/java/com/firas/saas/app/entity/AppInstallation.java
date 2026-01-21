package com.firas.saas.app.entity;

import com.firas.saas.common.base.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an app installed in a specific tenant's store.
 * One app can be installed in many tenants. One tenant can have many apps.
 */
@Entity
@Table(name = "app_installations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"app_id", "tenant_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppInstallation extends TenantEntity {

    /**
     * The app that is installed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", nullable = false)
    private App app;

    /**
     * Scopes granted by the merchant during installation.
     * Must be a subset of app.declaredScopes.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_installation_scopes", joinColumns = @JoinColumn(name = "installation_id"))
    @Column(name = "scope")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<AppScope> grantedScopes = new HashSet<>();

    /**
     * Current status of the installation.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InstallationStatus status = InstallationStatus.ACTIVE;

    /**
     * User ID of the merchant who installed the app.
     */
    @Column(nullable = false)
    private Long installedByUserId;

    /**
     * Check if this installation is active.
     */
    public boolean isActive() {
        return status == InstallationStatus.ACTIVE;
    }

    /**
     * Check if the installation has a specific scope.
     */
    public boolean hasScope(AppScope scope) {
        return grantedScopes.contains(scope);
    }
}
