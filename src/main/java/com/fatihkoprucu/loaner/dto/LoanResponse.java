package com.fatihkoprucu.loaner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private BigDecimal loanAmount;
    private BigDecimal totalAmount;
    private int numberOfInstallments;
    private LocalDate createDate;
    private boolean isPaid;
    private List<LoanInstallmentResponse> installments;
} 