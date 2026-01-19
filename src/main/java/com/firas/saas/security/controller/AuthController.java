package com.firas.saas.security.controller;

import com.firas.saas.security.dto.JwtResponse;
import com.firas.saas.security.dto.LoginRequest;
import com.firas.saas.security.dto.MerchantSignupRequest;
import com.firas.saas.security.jwt.JwtUtils;
import com.firas.saas.tenant.dto.TenantResponse;
import com.firas.saas.tenant.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final TenantService tenantService;

    @PostMapping("/register")
    public ResponseEntity<TenantResponse> registerMerchant(@Valid @RequestBody MerchantSignupRequest signUpRequest) {
        return ResponseEntity.ok(tenantService.registerMerchant(signUpRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(userDetails);

        String role = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .findFirst()
                .orElse("ROLE_CUSTOMER");

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), role));
    }
}
