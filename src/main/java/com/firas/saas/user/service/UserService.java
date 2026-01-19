package com.firas.saas.user.service;

import com.firas.saas.user.dto.UserCreateRequest;
import com.firas.saas.user.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);
    UserResponse getUserByEmail(String email);
    List<UserResponse> getAllUsersByTenant(Long tenantId);
}
