package com.fatihkoprucu.loaner.service;

import com.fatihkoprucu.loaner.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanSecurityService {
    
    private final LoanRepository loanRepository;
    
    public boolean isCustomerLoan(Long loanId, Long userId) {
        return loanRepository.findById(loanId)
                .map(loan -> loan.getUser().getId().equals(userId))
                .orElse(false);
    }
} 