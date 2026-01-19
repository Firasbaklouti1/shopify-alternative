package com.firas.saas.customer.service;

import com.firas.saas.customer.dto.CustomerRequest;
import com.firas.saas.customer.dto.CustomerResponse;

import java.util.List;

public interface CustomerService {
    CustomerResponse createCustomer(CustomerRequest request, Long tenantId);
    CustomerResponse updateCustomer(Long id, CustomerRequest request, Long tenantId);
    CustomerResponse getCustomerByEmail(String email, Long tenantId);
    List<CustomerResponse> getAllCustomers(Long tenantId);
    void deleteCustomer(Long id, Long tenantId);
}
