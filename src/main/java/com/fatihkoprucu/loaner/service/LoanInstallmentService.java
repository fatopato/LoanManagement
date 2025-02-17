package com.fatihkoprucu.loaner.service;

import com.fatihkoprucu.loaner.dto.LoanInstallmentResponse;

import java.util.List;

public interface LoanInstallmentService {
    List<LoanInstallmentResponse> getInstallmentsByLoanId(Long loanId);
}
