package com.fatihkoprucu.loaner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private int paidInstallments;
    private BigDecimal totalSpent;
    private boolean loanFullyPaid;
}