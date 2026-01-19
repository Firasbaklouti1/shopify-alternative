package com.firas.saas.user.controller;

import com.firas.saas.user.dto.UserCreateRequest;
import com.firas.saas.user.dto.UserResponse;
import com.firas.saas.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request, @AuthenticationPrincipal com.firas.saas.security.service.UserPrincipal principal) {
        request.setTenantId(principal.getTenantId());
        return new ResponseEntity<>(userService.createUser(request), HttpStatus.CREATED);
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<List<UserResponse>> getTenantUsers(@PathVariable Long tenantId) {
        return ResponseEntity.ok(userService.getAllUsersByTenant(tenantId));
    }
}
