package com.firas.saas.app.repository;

import com.firas.saas.app.entity.App;
import com.firas.saas.app.entity.AppStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppRepository extends JpaRepository<App, Long> {

    Optional<App> findByClientId(String clientId);

    boolean existsByClientId(String clientId);

    boolean existsByName(String name);

    List<App> findAllByStatus(AppStatus status);

    Optional<App> findByIdAndStatus(Long id, AppStatus status);
}
