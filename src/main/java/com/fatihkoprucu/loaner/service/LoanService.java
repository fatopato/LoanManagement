package com.fatihkoprucu.loaner.service;

import com.fatihkoprucu.loaner.dto.GetLoanRequest;
import com.fatihkoprucu.loaner.dto.LoanRequest;
import com.fatihkoprucu.loaner.dto.LoanResponse;
import com.fatihkoprucu.loaner.dto.PaymentResponse;

import java.math.BigDecimal;
import java.util.List;

public interface LoanService {
    LoanResponse createLoan(LoanRequest request);
    
    LoanResponse getLoanById(Long loanId);
    
    List<LoanResponse> getLoans(GetLoanRequest request);
    
    PaymentResponse payLoan(Long loanId, BigDecimal paymentAmount);
}
