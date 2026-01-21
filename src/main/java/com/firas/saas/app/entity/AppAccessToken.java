package com.firas.saas.app.entity;

import com.firas.saas.common.base.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Access token issued to an app after installation.
 * Used by the app to authenticate API calls on behalf of a tenant.
 */
@Entity
@Table(name = "app_access_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppAccessToken extends TenantEntity {

    /**
     * The token value (UUID format).
     * This is sent by the app in the Authorization header.
     */
    @Column(nullable = false, unique = true, updatable = false)
    private String tokenValue;

    /**
     * The installation this token belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installation_id", nullable = false)
    private AppInstallation installation;

    /**
     * Scopes granted to this token (copied from installation at creation time).
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_token_scopes", joinColumns = @JoinColumn(name = "token_id"))
    @Column(name = "scope")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<AppScope> scopes = new HashSet<>();

    /**
     * When this token expires.
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Whether this token has been revoked.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    /**
     * Check if this token is valid (not expired and not revoked).
     */
    public boolean isValid() {
        return !revoked && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Check if this token has a specific scope.
     */
    public boolean hasScope(AppScope scope) {
        return scopes.contains(scope);
    }

    /**
     * Revoke this token.
     */
    public void revoke() {
        this.revoked = true;
    }
}
