package com.fatihkoprucu.loaner.service;

import com.fatihkoprucu.loaner.dto.*;
import com.fatihkoprucu.loaner.entity.Loan;
import com.fatihkoprucu.loaner.entity.LoanInstallment;
import com.fatihkoprucu.loaner.entity.User;
import com.fatihkoprucu.loaner.exception.LoanValidationException;
import com.fatihkoprucu.loaner.repository.LoanInstallmentRepository;
import com.fatihkoprucu.loaner.repository.LoanRepository;
import com.fatihkoprucu.loaner.repository.UserRepository;
import com.fatihkoprucu.loaner.validator.LoanRequestValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LoanServiceImpl implements LoanService {
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;
    private final LoanRequestValidator loanRequestValidator;

    @Override
    public LoanResponse createLoan(LoanRequest request) {
        User user = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        // Validate the loan request
        loanRequestValidator.validate(request, user);

        BigDecimal totalLoanAmount = calculateTotalAmount(request.getAmount(), request.getInterestRate());
        
        Loan loan = new Loan();
        loan.setUser(user);
        loan.setLoanAmount(request.getAmount());
        loan.setNumberOfInstallments(request.getNumberOfInstallments());
        loan.setCreateDate(LocalDate.now());
        loan.setPaid(false);

        loan = loanRepository.save(loan);

        List<LoanInstallment> installments = createInstallments(loan, totalLoanAmount, request.getNumberOfInstallments());
        
        user.setUsedCreditLimit(user.getUsedCreditLimit().add(totalLoanAmount));
        userRepository.save(user);

        return mapToLoanResponse(loan, installments);
    }

    @Override
    public LoanResponse getLoanById(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found with id: " + loanId));
        
        List<LoanInstallment> installments = loanInstallmentRepository.findByLoanIdOrderByDueDateAsc(loanId);
        
        return mapToLoanResponse(loan, installments);
    }

    private LoanResponse mapToLoanResponse(Loan loan, List<LoanInstallment> installments) {
        return new LoanResponse(
                loan.getId(),
                loan.getUser().getId(),
                loan.getUser().getName() + " " + loan.getUser().getSurname(),
                loan.getLoanAmount(),
                calculateTotalInstallmentAmount(installments),
                loan.getNumberOfInstallments(),
                loan.getCreateDate(),
                loan.isPaid(),
                installments.stream()
                        .map(this::mapToInstallmentResponse)
                        .collect(Collectors.toList())
        );
    }

    private LoanInstallmentResponse mapToInstallmentResponse(LoanInstallment installment) {
        return new LoanInstallmentResponse(
                installment.getId(),
                installment.getLoan().getId(),
                installment.getAmount(),
                installment.getPaidAmount(),
                installment.getDueDate(),
                installment.getPaymentDate(),
                installment.isPaid()
        );
    }

    private BigDecimal calculateTotalAmount(BigDecimal principal, Double interestRate) {
        return principal.multiply(BigDecimal.valueOf(1 + interestRate));
    }

    private BigDecimal calculateTotalInstallmentAmount(List<LoanInstallment> installments) {
        return installments.stream()
                .map(LoanInstallment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateLoanAmount(User customer, BigDecimal loanAmount) {
        BigDecimal availableCredit = customer.getCreditLimit().subtract(customer.getUsedCreditLimit());
        if (loanAmount.compareTo(availableCredit) > 0) {
            throw new IllegalArgumentException("Loan amount exceeds available credit limit");
        }
    }


    public List<LoanInstallment> createInstallments(Loan loan, BigDecimal totalAmount, int installments) {
        BigDecimal installmentAmount = totalAmount.divide(BigDecimal.valueOf(installments), RoundingMode.HALF_UP);
        LocalDate firstInstallmentDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        List<LoanInstallment> loanInstallments = new ArrayList<>();
        for (int i = 0; i < installments; i++) {
            LoanInstallment installment = new LoanInstallment();
            installment.setLoan(loan);
            installment.setAmount(installmentAmount);
            installment.setPaidAmount(BigDecimal.ZERO);
            installment.setDueDate(firstInstallmentDate.plusMonths(i));
            installment.setPaid(false);
            LoanInstallment saved = loanInstallmentRepository.save(installment);
            loanInstallments.add(saved);
        }
        return loanInstallments;
    }

    @Override
    public List<LoanResponse> getLoans(GetLoanRequest request) {
        // Verify customer exists
        if (!userRepository.existsById(request.getCustomerId())) {
            throw new EntityNotFoundException("Customer not found with id: " + request.getCustomerId());
        }

        // Get base loans for customer
        List<Loan> loans = loanRepository.findByUserId(request.getCustomerId());

        // Apply filters
        List<Loan> filteredLoans = loans.stream()
            .filter(loan -> request.getIsPaid() == null || loan.isPaid() == request.getIsPaid())
            .filter(loan -> request.getNumberOfInstallments() == null || 
                    loan.getNumberOfInstallments() == request.getNumberOfInstallments())
            .collect(Collectors.toList());

        // Map to response objects
        return filteredLoans.stream()
            .map(loan -> {
                List<LoanInstallment> installments = 
                    loanInstallmentRepository.findByLoanIdOrderByDueDateAsc(loan.getId());
                return mapToLoanResponse(loan, installments);
            })
            .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse payLoan(Long loanId, BigDecimal paymentAmount) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new EntityNotFoundException("Loan not found with id: " + loanId));

        if (loan.isPaid()) {
            throw new LoanValidationException("Loan is already fully paid");
        }

        List<LoanInstallment> installments = loanInstallmentRepository
            .findByLoanIdOrderByDueDateAsc(loanId);

        if (installments.isEmpty()) {
            throw new LoanValidationException("No installments found for this loan");
        }

        LocalDate today = LocalDate.now();
        LocalDate maxPayableDate = today.plusMonths(3).withDayOfMonth(1);

        // Filter unpaid installments that are within the 3-month window
        List<LoanInstallment> payableInstallments = installments.stream()
            .filter(installment -> !installment.isPaid())
            .filter(installment -> !installment.getDueDate().isAfter(maxPayableDate))
            .collect(Collectors.toList());

        if (payableInstallments.isEmpty()) {
            throw new LoanValidationException("No payable installments found within the allowed time window");
        }

        BigDecimal remainingAmount = paymentAmount;
        int paidInstallments = 0;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (LoanInstallment installment : payableInstallments) {
            BigDecimal requiredAmount = calculateRequiredAmount(installment, today);

            if (remainingAmount.compareTo(requiredAmount) >= 0) {
                // Pay this installment
                installment.setPaid(true);
                installment.setPaidAmount(requiredAmount);
                installment.setPaymentDate(today);
                loanInstallmentRepository.save(installment);

                remainingAmount = remainingAmount.subtract(requiredAmount);
                totalSpent = totalSpent.add(requiredAmount);
                paidInstallments++;
            } else {
                // Not enough money to pay this installment
                break;
            }
        }

        if (paidInstallments == 0) {
            throw new LoanValidationException("Payment amount is insufficient to pay any complete installment");
        }

        // Check if all installments are paid
        boolean allPaid = installments.stream().allMatch(LoanInstallment::isPaid);
        if (allPaid) {
            loan.setPaid(true);
            loanRepository.save(loan);

            // Update customer's used credit limit
            User user = loan.getUser();
            user.setUsedCreditLimit(user.getUsedCreditLimit().subtract(loan.getLoanAmount()));
            userRepository.save(user);
        }

        return new PaymentResponse(paidInstallments, totalSpent, allPaid);
    }

    private BigDecimal calculateRequiredAmount(LoanInstallment installment, LocalDate paymentDate) {
        BigDecimal amount = installment.getAmount();

        // Apply early payment discount
        if (paymentDate.isBefore(installment.getDueDate())) {
            long daysEarly = ChronoUnit.DAYS.between(paymentDate, installment.getDueDate());
            BigDecimal discount = amount.multiply(
                BigDecimal.valueOf(0.001).multiply(BigDecimal.valueOf(daysEarly))
            );
            amount = amount.subtract(discount);
        }

        // Apply late payment penalty
        if (paymentDate.isAfter(installment.getDueDate())) {
            long daysLate = ChronoUnit.DAYS.between(installment.getDueDate(), paymentDate);
            BigDecimal penalty = amount.multiply(
                BigDecimal.valueOf(0.001).multiply(BigDecimal.valueOf(daysLate))
            );
            amount = amount.add(penalty);
        }

        return amount;
    }
}
