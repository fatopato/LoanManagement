package com.fatihkoprucu.loaner.controller;

import com.fatihkoprucu.loaner.dto.CustomerResponse;
import com.fatihkoprucu.loaner.service.CustomerService;
import com.fatihkoprucu.loaner.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final UserService userService;

    // Customer can only view their own profile
    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('ROLE_CUSTOMER') and #customerId == principal.id")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long customerId) {
        CustomerResponse customerResponse = userService.getCustomerById(customerId);
        return ResponseEntity.ok(customerResponse);
    }
}
