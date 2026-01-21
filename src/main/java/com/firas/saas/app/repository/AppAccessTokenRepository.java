package com.firas.saas.app.repository;

import com.firas.saas.app.entity.AppAccessToken;
import com.firas.saas.common.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppAccessTokenRepository extends BaseRepository<AppAccessToken> {

    /**
     * Find token by value with eagerly fetched installation and app.
     * This is critical for the AppTokenAuthFilter to avoid LazyInitializationException.
     */
    @Query("SELECT t FROM AppAccessToken t " +
           "LEFT JOIN FETCH t.installation i " +
           "LEFT JOIN FETCH i.app " +
           "WHERE t.tokenValue = :tokenValue")
    Optional<AppAccessToken> findByTokenValue(@Param("tokenValue") String tokenValue);

    @Query("SELECT t FROM AppAccessToken t " +
           "LEFT JOIN FETCH t.installation i " +
           "LEFT JOIN FETCH i.app " +
           "WHERE t.tokenValue = :tokenValue AND t.revoked = false")
    Optional<AppAccessToken> findByTokenValueAndRevokedFalse(@Param("tokenValue") String tokenValue);

    List<AppAccessToken> findAllByInstallationId(Long installationId);

    List<AppAccessToken> findAllByInstallationIdAndRevokedFalse(Long installationId);

    @Modifying
    @Query("UPDATE AppAccessToken t SET t.revoked = true WHERE t.installation.id = :installationId")
    void revokeAllByInstallationId(@Param("installationId") Long installationId);

    Optional<AppAccessToken> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByTokenValue(String tokenValue);
}
