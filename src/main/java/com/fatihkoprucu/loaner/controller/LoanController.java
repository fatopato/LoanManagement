package com.fatihkoprucu.loaner.controller;

import com.fatihkoprucu.loaner.dto.GetLoanRequest;
import com.fatihkoprucu.loaner.dto.LoanRequest;
import com.fatihkoprucu.loaner.dto.LoanResponse;
import com.fatihkoprucu.loaner.dto.PaymentResponse;
import com.fatihkoprucu.loaner.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;

    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponse> createLoan(@RequestBody LoanRequest loanRequest) {
        LoanResponse loanResponse = loanService.createLoan(loanRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(loanResponse);
    }

    @GetMapping("/{loanId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable Long loanId) {
        LoanResponse loanResponse = loanService.getLoanById(loanId);
        return ResponseEntity.ok(loanResponse);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.principal.id")
    public ResponseEntity<List<LoanResponse>> getLoans(
        @PathVariable Long customerId,
        @RequestParam(required = false) Boolean isPaid,
        @RequestParam(required = false) Integer numberOfInstallments
    ) {
        GetLoanRequest request = new GetLoanRequest(customerId, isPaid, numberOfInstallments);
        List<LoanResponse> loans = loanService.getLoans(request);
        return ResponseEntity.ok(loans);
    }

    @PostMapping("/{loanId}/pay")
    @PreAuthorize("hasRole('ADMIN') or @loanSecurityService.isCustomerLoan(#loanId, authentication.principal.id)")
    public ResponseEntity<PaymentResponse> payLoan(
            @PathVariable Long loanId,
            @RequestParam BigDecimal amount) {
        PaymentResponse response = loanService.payLoan(loanId, amount);
        return ResponseEntity.ok(response);
    }
}
