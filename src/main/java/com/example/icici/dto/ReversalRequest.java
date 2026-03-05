package com.example.icici.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReversalRequest {
    private String upiTxnId;
    private String payerVpa;
    private BigDecimal amount;
}