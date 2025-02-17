package com.fatihkoprucu.loaner.service;

import com.fatihkoprucu.loaner.dto.CustomerResponse;

public interface CustomerService {
    CustomerResponse getCustomerById(Long customerId);
} 