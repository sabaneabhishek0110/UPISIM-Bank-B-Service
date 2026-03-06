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
public class CreditRequest {
    private String PayerVpa;
    private String PayeeVpa;
    private BigDecimal amount;
    private String upiTxnId;
    private String rrn;
    private String pspTxnId;

}
