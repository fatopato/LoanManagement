package com.fatihkoprucu.loaner.service;

import com.fatihkoprucu.loaner.dto.LoanInstallmentResponse;
import com.fatihkoprucu.loaner.entity.LoanInstallment;
import com.fatihkoprucu.loaner.repository.LoanInstallmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LoanInstallmentServiceImpl implements LoanInstallmentService {

    private final LoanInstallmentRepository loanInstallmentRepository;
    @Override
    public List<LoanInstallmentResponse> getInstallmentsByLoanId(Long loanId) {
        List<LoanInstallment> installments = loanInstallmentRepository.findByLoanIdOrderByDueDateAsc(loanId);

        if (installments.isEmpty()) {
            throw new EntityNotFoundException("No installments found for the given loan ID.");
        }

        return installments.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private LoanInstallmentResponse mapToResponse(LoanInstallment installment) {
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
}
