package com.fatihkoprucu.loaner.validator;

import com.fatihkoprucu.loaner.dto.LoanRequest;
import com.fatihkoprucu.loaner.entity.User;
import com.fatihkoprucu.loaner.exception.LoanValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class LoanRequestValidator {
    private static final List<Integer> VALID_INSTALLMENTS = List.of(6, 9, 12, 24);
    private static final double MIN_INTEREST_RATE = 0.1;
    private static final double MAX_INTEREST_RATE = 0.5;

    public void validate(LoanRequest request, User customer) {
        validateInstallments(request.getNumberOfInstallments());
        validateInterestRate(request.getInterestRate());
        validateLoanAmount(request.getAmount(), request.getInterestRate(), customer);
    }

    private void validateInstallments(int numberOfInstallments) {
        if (!VALID_INSTALLMENTS.contains(numberOfInstallments)) {
            throw new LoanValidationException("Number of installments must be one of: " + VALID_INSTALLMENTS);
        }
    }

    private void validateInterestRate(Double interestRate) {
        if (interestRate == null || interestRate < MIN_INTEREST_RATE || interestRate > MAX_INTEREST_RATE) {
            throw new LoanValidationException(
                String.format("Interest rate must be between %.1f and %.1f", MIN_INTEREST_RATE, MAX_INTEREST_RATE)
            );
        }
    }

    private void validateLoanAmount(BigDecimal amount, Double interestRate, User customer) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new LoanValidationException("Loan amount must be greater than zero");
        }

        BigDecimal totalAmount = amount.multiply(BigDecimal.valueOf(1 + interestRate));
        BigDecimal availableCredit = customer.getCreditLimit().subtract(customer.getUsedCreditLimit());
        
        if (totalAmount.compareTo(availableCredit) > 0) {
            throw new LoanValidationException(
                String.format("Total loan amount (%.2f) exceeds available credit limit (%.2f)", 
                    totalAmount, availableCredit)
            );
        }
    }
} 