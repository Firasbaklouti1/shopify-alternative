package com.firas.saas.app.security;

import com.firas.saas.app.entity.AppAccessToken;
import com.firas.saas.app.entity.AppScope;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Principal representing an authenticated app (not a user).
 * Used when apps call the platform APIs with their access tokens.
 */
@Getter
public class AppPrincipal implements UserDetails {

    private final Long appId;
    private final Long installationId;
    private final Long tenantId;
    private final String clientId;
    private final Set<AppScope> scopes;
    private final String tokenValue;
    private final Collection<? extends GrantedAuthority> authorities;

    public AppPrincipal(AppAccessToken token) {
        this.appId = token.getInstallation().getApp().getId();
        this.installationId = token.getInstallation().getId();
        this.tenantId = token.getTenantId();
        this.clientId = token.getInstallation().getApp().getClientId();
        this.scopes = token.getScopes();
        this.tokenValue = token.getTokenValue();

        // Convert scopes to authorities: SCOPE_READ_ORDERS, SCOPE_WRITE_ORDERS, etc.
        this.authorities = scopes.stream()
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope.name()))
                .collect(Collectors.toList());
    }

    /**
     * Check if this app has a specific scope.
     */
    public boolean hasScope(AppScope scope) {
        return scopes.contains(scope);
    }

    /**
     * Check if this app has any of the specified scopes.
     */
    public boolean hasAnyScope(AppScope... requiredScopes) {
        for (AppScope scope : requiredScopes) {
            if (scopes.contains(scope)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if this app has all of the specified scopes.
     */
    public boolean hasAllScopes(AppScope... requiredScopes) {
        for (AppScope scope : requiredScopes) {
            if (!scopes.contains(scope)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // Apps don't have passwords
    }

    @Override
    public String getUsername() {
        return clientId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
