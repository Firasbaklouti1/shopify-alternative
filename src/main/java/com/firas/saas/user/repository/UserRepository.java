package com.firas.saas.user.repository;

import com.firas.saas.common.base.BaseRepository;
import com.firas.saas.user.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndTenant_Id(String email, Long tenantId);
}
