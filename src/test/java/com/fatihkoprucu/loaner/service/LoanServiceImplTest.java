package com.fatihkoprucu.loaner.service;

import com.fatihkoprucu.loaner.dto.GetLoanRequest;
import com.fatihkoprucu.loaner.dto.LoanRequest;
import com.fatihkoprucu.loaner.dto.LoanResponse;
import com.fatihkoprucu.loaner.dto.PaymentResponse;
import com.fatihkoprucu.loaner.entity.Customer;
import com.fatihkoprucu.loaner.entity.Loan;
import com.fatihkoprucu.loaner.entity.LoanInstallment;
import com.fatihkoprucu.loaner.exception.LoanValidationException;
import com.fatihkoprucu.loaner.repository.CustomerRepository;
import com.fatihkoprucu.loaner.repository.LoanInstallmentRepository;
import com.fatihkoprucu.loaner.repository.LoanRepository;
import com.fatihkoprucu.loaner.validator.LoanRequestValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;
    @Mock
    private LoanRequestValidator loanRequestValidator;

    @InjectMocks
    private LoanServiceImpl loanService;

    private Customer customer;
    private Loan loan;
    private LoanRequest loanRequest;
    private List<LoanInstallment> installments;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John");
        customer.setSurname("Doe");
        customer.setCreditLimit(BigDecimal.valueOf(10000));
        customer.setUsedCreditLimit(BigDecimal.ZERO);

        loan = new Loan();
        loan.setId(1L);
        loan.setCustomer(customer);
        loan.setLoanAmount(BigDecimal.valueOf(1000));
        loan.setNumberOfInstallments(12);
        loan.setCreateDate(LocalDate.now());
        loan.setPaid(false);

        loanRequest = new LoanRequest(
            1L,
            BigDecimal.valueOf(1000),
            0.3,
            12
        );

        installments = new ArrayList<>();
        BigDecimal installmentAmount = BigDecimal.valueOf(108.30).setScale(2, RoundingMode.HALF_UP);
        for (int i = 0; i < 12; i++) {
            LoanInstallment installment = new LoanInstallment();
            installment.setId((long) (i + 1));
            installment.setLoan(loan);
            installment.setAmount(installmentAmount);
            installment.setPaidAmount(BigDecimal.ZERO);
            installment.setDueDate(LocalDate.now().plusMonths(i + 1).withDayOfMonth(1));
            installment.setPaid(false);
            installment.setPaymentDate(null);
            installments.add(installment);
        }
    }

    @Test
    void getLoanById_Success() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanIdOrderByDueDateAsc(1L))
            .thenReturn(installments);

        LoanResponse response = loanService.getLoanById(1L);

        assertNotNull(response);
        assertEquals(loan.getId(), response.getId());
        assertEquals(customer.getId(), response.getCustomerId());
        assertEquals(loan.getLoanAmount(), response.getLoanAmount());
        
        verify(loanRepository).findById(1L);
        verify(loanInstallmentRepository).findByLoanIdOrderByDueDateAsc(1L);
    }

    @Test
    void getLoans_Success() {
        GetLoanRequest request = new GetLoanRequest(1L, false, 12);
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(loanRepository.findByCustomerId(1L)).thenReturn(List.of(loan));
        when(loanInstallmentRepository.findByLoanIdOrderByDueDateAsc(1L))
            .thenReturn(installments);

        List<LoanResponse> response = loanService.getLoans(request);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(loan.getId(), response.get(0).getId());
        
        verify(customerRepository).existsById(1L);
        verify(loanRepository).findByCustomerId(1L);
    }

    @Test
    void payLoan_Success() {
        BigDecimal paymentAmount = BigDecimal.valueOf(220);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanIdOrderByDueDateAsc(1L))
            .thenReturn(installments);
        when(loanInstallmentRepository.save(any(LoanInstallment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = loanService.payLoan(1L, paymentAmount);

        assertNotNull(response);
        assertTrue(response.getPaidInstallments() > 0);
        assertTrue(response.getTotalSpent().compareTo(BigDecimal.ZERO) > 0);
        assertFalse(response.isLoanFullyPaid());
        
        verify(loanRepository).findById(1L);
        verify(loanInstallmentRepository).findByLoanIdOrderByDueDateAsc(1L);
    }

    @Test
    void payLoan_InsufficientAmount() {
        BigDecimal paymentAmount = BigDecimal.valueOf(50);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanIdOrderByDueDateAsc(1L))
            .thenReturn(installments);

        assertThrows(LoanValidationException.class, () ->
            loanService.payLoan(1L, paymentAmount)
        );
    }

    @Test
    void payLoan_AlreadyPaid() {
        loan.setPaid(true);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThrows(LoanValidationException.class, () ->
            loanService.payLoan(1L, BigDecimal.valueOf(200))
        );
    }

    @Test
    void getLoans_CustomerNotFound() {
        GetLoanRequest request = new GetLoanRequest(1L, null, null);
        when(customerRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () ->
            loanService.getLoans(request)
        );
    }

    @Test
    void getLoanById_NotFound() {
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            loanService.getLoanById(1L)
        );
    }
} 