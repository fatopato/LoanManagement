package com.fatihkoprucu.loaner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetLoanRequest {
    private Long customerId;
    private Boolean isPaid;
    private Integer numberOfInstallments;
} 