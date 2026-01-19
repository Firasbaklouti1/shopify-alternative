package com.firas.saas.tenant.service;

import com.firas.saas.tenant.dto.TenantCreateRequest;
import com.firas.saas.tenant.dto.TenantResponse;
import com.firas.saas.tenant.entity.Tenant;
import com.firas.saas.tenant.exception.TenantNotFoundException;
import com.firas.saas.tenant.repository.TenantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantService Unit Tests")
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantServiceImpl tenantService;

    @Nested
    @DisplayName("createTenant method")
    class CreateTenantTests {

        @Test
        @DisplayName("should successfully create tenant when name and slug are unique")
        void createTenant_Success() {
            // Arrange
            TenantCreateRequest request = new TenantCreateRequest("My Store", "my-store", "owner@example.com");
            when(tenantRepository.existsBySlug(request.getSlug())).thenReturn(false);
            when(tenantRepository.existsByName(request.getName())).thenReturn(false);
            
            Tenant savedTenant = Tenant.builder()
                    .name(request.getName())
                    .slug(request.getSlug())
                    .ownerEmail(request.getOwnerEmail())
                    .active(true)
                    .build();
            savedTenant.setId(1L);
            
            when(tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);

            // Act
            TenantResponse response = tenantService.createTenant(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getSlug()).isEqualTo("my-store");
            verify(tenantRepository).save(any(Tenant.class));
        }

        @Test
        @DisplayName("should throw exception when slug already exists")
        void createTenant_DuplicateSlug() {
            // Arrange
            TenantCreateRequest request = new TenantCreateRequest("My Store", "existing-slug", "owner@example.com");
            when(tenantRepository.existsBySlug(request.getSlug())).thenReturn(true);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> tenantService.createTenant(request));
            assertThat(exception.getMessage()).contains("already exists");
            verify(tenantRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getTenantBySlug method")
    class GetTenantBySlugTests {

        @Test
        @DisplayName("should return tenant when slug exists")
        void getTenantBySlug_Success() {
            // Arrange
            String slug = "my-store";
            Tenant tenant = Tenant.builder().name("My Store").slug(slug).ownerEmail("owner@example.com").build();
            tenant.setId(1L);
            when(tenantRepository.findBySlug(slug)).thenReturn(Optional.of(tenant));

            // Act
            TenantResponse response = tenantService.getTenantBySlug(slug);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getSlug()).isEqualTo(slug);
        }

        @Test
        @DisplayName("should throw TenantNotFoundException when slug does not exist")
        void getTenantBySlug_NotFound() {
            // Arrange
            String slug = "non-existent";
            when(tenantRepository.findBySlug(slug)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(TenantNotFoundException.class, () -> tenantService.getTenantBySlug(slug));
        }
    }
}
