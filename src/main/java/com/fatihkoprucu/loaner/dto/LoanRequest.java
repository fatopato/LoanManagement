package com.fatihkoprucu.loaner.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequest {
    @NotNull
    private Long customerId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    @Positive
    private Double interestRate;

    @NotNull
    @Min(1)
    private Integer numberOfInstallments;
} 