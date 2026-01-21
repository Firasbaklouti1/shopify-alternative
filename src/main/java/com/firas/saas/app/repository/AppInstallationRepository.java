package com.firas.saas.app.repository;

import com.firas.saas.app.entity.AppInstallation;
import com.firas.saas.app.entity.InstallationStatus;
import com.firas.saas.common.base.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppInstallationRepository extends BaseRepository<AppInstallation> {

    Optional<AppInstallation> findByAppIdAndTenantId(Long appId, Long tenantId);

    Optional<AppInstallation> findByAppIdAndTenantIdAndStatus(Long appId, Long tenantId, InstallationStatus status);

    List<AppInstallation> findAllByTenantId(Long tenantId);

    List<AppInstallation> findAllByTenantIdAndStatus(Long tenantId, InstallationStatus status);

    List<AppInstallation> findAllByAppId(Long appId);

    boolean existsByAppIdAndTenantIdAndStatus(Long appId, Long tenantId, InstallationStatus status);

    Optional<AppInstallation> findByIdAndTenantId(Long id, Long tenantId);
}
