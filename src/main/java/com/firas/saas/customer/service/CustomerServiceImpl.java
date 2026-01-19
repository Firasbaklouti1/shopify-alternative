package com.firas.saas.customer.service;

import com.firas.saas.customer.dto.CustomerRequest;
import com.firas.saas.customer.dto.CustomerResponse;
import com.firas.saas.customer.entity.Customer;
import com.firas.saas.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request, Long tenantId) {
        if (customerRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
            throw new RuntimeException("Customer with this email already exists for this tenant");
        }

        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .active(true)
                .build();
        customer.setTenantId(tenantId);

        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByEmail(String email, Long tenantId) {
        return customerRepository.findByEmailAndTenantId(email, tenantId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers(Long tenantId) {
        return customerRepository.findAllByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request, Long tenantId) {
        Customer customer = customerRepository.findById(id)
                .filter(c -> c.getTenantId().equals(tenantId))
                .orElseThrow(() -> new RuntimeException("Customer not found or access denied"));
        
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        
        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id, Long tenantId) {
        Customer customer = customerRepository.findById(id)
                .filter(c -> c.getTenantId().equals(tenantId))
                .orElseThrow(() -> new RuntimeException("Customer not found or access denied"));
        customerRepository.delete(customer);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .active(customer.isActive())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
