package com.fatihkoprucu.loaner.controller;

import com.fatihkoprucu.loaner.dto.LoanInstallmentResponse;
import com.fatihkoprucu.loaner.service.LoanInstallmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/installments")
@RequiredArgsConstructor
public class LoanInstallmentController {

    private final LoanInstallmentService loanInstallmentService;

    @GetMapping("/{loanId}")
    public ResponseEntity<List<LoanInstallmentResponse>> getInstallmentsByLoanId(
            @PathVariable Long loanId) {
        List<LoanInstallmentResponse> response = loanInstallmentService.getInstallmentsByLoanId(loanId);
        return ResponseEntity.ok(response);
    }
}