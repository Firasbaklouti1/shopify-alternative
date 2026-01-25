package com.firas.saas.security.controller;

import com.firas.saas.common.exception.ResourceNotFoundException;
import com.firas.saas.customer.entity.Customer;
import com.firas.saas.customer.repository.CustomerRepository;
import com.firas.saas.security.dto.CustomerSignupRequest;
import com.firas.saas.security.dto.JwtResponse;
import com.firas.saas.security.dto.LoginRequest;
import com.firas.saas.security.dto.MerchantSignupRequest;
import com.firas.saas.security.jwt.JwtUtils;
import com.firas.saas.security.service.UserPrincipal;
import com.firas.saas.tenant.dto.TenantResponse;
import com.firas.saas.tenant.entity.Tenant;
import com.firas.saas.tenant.repository.TenantRepository;
import com.firas.saas.tenant.service.TenantService;
import com.firas.saas.user.entity.Role;
import com.firas.saas.user.entity.User;
import com.firas.saas.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final TenantService tenantService;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<TenantResponse> registerMerchant(@Valid @RequestBody MerchantSignupRequest signUpRequest) {
        return ResponseEntity.ok(tenantService.registerMerchant(signUpRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(userPrincipal);

        String role = userPrincipal.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .findFirst()
                .orElse("ROLE_CUSTOMER");

        return ResponseEntity.ok(new JwtResponse(jwt, userPrincipal.getUsername(), role, userPrincipal.getTenantId()));
    }

    /**
     * Customer self-registration for a specific store.
     * Creates both a User (for authentication) and a Customer (CRM record).
     *
     * @param storeSlug The store slug where the customer is registering
     * @param request Customer signup details
     * @return JWT token for immediate login
     */
    @PostMapping("/customer/{storeSlug}/register")
    @Transactional
    public ResponseEntity<JwtResponse> registerCustomer(
            @PathVariable String storeSlug,
            @Valid @RequestBody CustomerSignupRequest request) {

        // Find the store by slug
        Tenant tenant = tenantRepository.findBySlug(storeSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeSlug));

        // Check if email already exists for this tenant
        if (userRepository.existsByEmailAndTenant_Id(request.getEmail(), tenant.getId())) {
            throw new RuntimeException("An account with this email already exists");
        }

        // Create User for authentication
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFirstName() + " " + request.getLastName())
                .role(Role.CUSTOMER)
                .tenant(tenant)
                .enabled(true)
                .build();

        userRepository.save(user);

        // Create Customer CRM record
        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .active(true)
                .build();
        customer.setTenantId(tenant.getId());

        customerRepository.save(customer);

        // Auto-login: Generate JWT token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(userPrincipal);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new JwtResponse(jwt, userPrincipal.getUsername(), "ROLE_CUSTOMER", tenant.getId()));
    }
}
