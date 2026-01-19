package com.firas.saas.user.service;

import com.firas.saas.tenant.entity.Tenant;
import com.firas.saas.tenant.repository.TenantRepository;
import com.firas.saas.user.dto.UserCreateRequest;
import com.firas.saas.user.dto.UserResponse;
import com.firas.saas.user.entity.Role;
import com.firas.saas.user.entity.User;
import com.firas.saas.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("createUser method")
    class CreateUserTests {

        @Test
        @DisplayName("should successfully create user when email is unique and tenant exists")
        void createUser_Success() {
            // Arrange
            UserCreateRequest request = new UserCreateRequest(
                    "test@example.com", "password123", "John Doe", Role.MERCHANT, 1L);
            
            Tenant tenant = Tenant.builder().name("Test Tenant").build();
            tenant.setId(1L);

            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
            when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
            
            User savedUser = User.builder()
                    .email(request.getEmail())
                    .password("encodedPassword")
                    .fullName(request.getFullName())
                    .role(request.getRole())
                    .tenant(tenant)
                    .enabled(true)
                    .build();
            savedUser.setId(100L);

            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // Act
            UserResponse response = userService.createUser(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(100L);
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getTenantId()).isEqualTo(1L);
            
            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("should throw exception when email already exists")
        void createUser_DuplicateEmail() {
            // Arrange
            UserCreateRequest request = new UserCreateRequest(
                    "existing@example.com", "password123", "John Doe", Role.MERCHANT, 1L);
            
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(request));
            assertThat(exception.getMessage()).isEqualTo("Email already in use");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when tenant not found")
        void createUser_TenantNotFound() {
            // Arrange
            UserCreateRequest request = new UserCreateRequest(
                    "test@example.com", "password123", "John Doe", Role.MERCHANT, 999L);
            
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(tenantRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(request));
            assertThat(exception.getMessage()).isEqualTo("Tenant not found");
        }
    }
}
