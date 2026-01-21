package com.firas.saas.app.entity;

import com.firas.saas.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a third-party app registered in the App Store.
 * Apps are GLOBAL entities (not tenant-scoped) - they can be installed by any merchant.
 */
@Entity
@Table(name = "apps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class App extends BaseEntity {

    /**
     * Display name of the app.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Description of what the app does.
     */
    @Column(length = 2000)
    private String description;

    /**
     * Name of the developer or organization that created the app.
     */
    @Column(nullable = false)
    private String developerName;

    /**
     * Unique public identifier for the app (UUID format).
     * Used by apps to identify themselves during installation.
     */
    @Column(nullable = false, unique = true, updatable = false)
    private String clientId;

    /**
     * Hashed client secret (BCrypt).
     * The plaintext is only shown once at creation.
     */
    @Column(nullable = false)
    private String clientSecretHash;

    /**
     * URL where the app receives webhook events.
     * Must be HTTPS in production.
     */
    @Column(nullable = false)
    private String webhookUrl;

    /**
     * Scopes that this app requests during installation.
     * Merchants can grant all or a subset of these scopes.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_declared_scopes", joinColumns = @JoinColumn(name = "app_id"))
    @Column(name = "scope")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<AppScope> declaredScopes = new HashSet<>();

    /**
     * Current status of the app in the App Store.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AppStatus status = AppStatus.DRAFT;

    /**
     * Check if the app is available for installation.
     */
    public boolean isPublished() {
        return status == AppStatus.PUBLISHED;
    }
}
