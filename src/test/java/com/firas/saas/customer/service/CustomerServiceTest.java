package com.firas.saas.customer.service;

import com.firas.saas.customer.dto.CustomerRequest;
import com.firas.saas.customer.dto.CustomerResponse;
import com.firas.saas.customer.entity.Customer;
import com.firas.saas.customer.repository.CustomerRepository;
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
@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Nested
    @DisplayName("createCustomer method")
    class CreateCustomerTests {

        @Test
        @DisplayName("should successfully create customer when email is unique for tenant")
        void createCustomer_Success() {
            // Arrange
            Long tenantId = 1L;
            CustomerRequest request = new CustomerRequest("John", "Doe", "john@example.com", "123456789");
            
            when(customerRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)).thenReturn(false);
            
            Customer savedCustomer = Customer.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .active(true)
                    .build();
            savedCustomer.setId(10L);
            savedCustomer.setTenantId(tenantId);
            
            when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

            // Act
            CustomerResponse response = customerService.createCustomer(request, tenantId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(10L);
            assertThat(response.getEmail()).isEqualTo("john@example.com");
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("should throw exception when email already exists for tenant")
        void createCustomer_DuplicateEmail() {
            // Arrange
            Long tenantId = 1L;
            CustomerRequest request = new CustomerRequest("John", "Doe", "existing@example.com", "123456789");
            
            when(customerRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)).thenReturn(true);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> customerService.createCustomer(request, tenantId));
            assertThat(exception.getMessage()).contains("already exists");
            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getCustomerByEmail method")
    class GetCustomerByEmailTests {

        @Test
        @DisplayName("should return customer when email exists for tenant")
        void getCustomerByEmail_Success() {
            // Arrange
            String email = "john@example.com";
            Long tenantId = 1L;
            Customer customer = Customer.builder().firstName("John").lastName("Doe").email(email).build();
            customer.setId(10L);
            customer.setTenantId(tenantId);
            
            when(customerRepository.findByEmailAndTenantId(email, tenantId)).thenReturn(Optional.of(customer));

            // Act
            CustomerResponse response = customerService.getCustomerByEmail(email, tenantId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("should throw exception when customer not found for tenant")
        void getCustomerByEmail_NotFound() {
            // Arrange
            String email = "nonexistent@example.com";
            Long tenantId = 1L;
            
            when(customerRepository.findByEmailAndTenantId(email, tenantId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(RuntimeException.class, () -> customerService.getCustomerByEmail(email, tenantId));
        }
    }
}
