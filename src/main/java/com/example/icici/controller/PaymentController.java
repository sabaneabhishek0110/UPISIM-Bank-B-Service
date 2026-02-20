package com.example.icici.controller;

import com.example.icici.dto.CreditRequest;
import com.example.icici.dto.CreditResponse;
import com.example.icici.dto.DebitRequest;
import com.example.icici.dto.DebitResponse;
import com.example.icici.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bank")
public class PaymentController {
    private PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/debit")
    public ResponseEntity<DebitResponse> debit(@RequestBody DebitRequest requestBody, HttpServletRequest request) {
        String PayerVpa = requestBody.getPayerVpa();
        Double amount = requestBody.getAmount();
        String pin = requestBody.getPin();
        String rrn = requestBody.getRrn();
        String upi_txn_id = requestBody.getUpi_txn_id();
        String psp_txn_id = requestBody.getPsp_txn_id();
        DebitResponse response = paymentService.debit(PayerVpa,amount,pin,rrn,upi_txn_id,psp_txn_id);

        System.out.println("ICICI Debit called");
        System.out.println("Response : "+ response.getStatus());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/credit")
    public ResponseEntity<CreditResponse> credit(@RequestBody CreditRequest requestBody, HttpServletRequest request) {
        String PayeeVpa = requestBody.getPayeeVpa();
        Double amount = requestBody.getAmount();
        String rrn = requestBody.getRrn();
        String upi_txn_id = requestBody.getUpi_txn_id();
        String psp_txn_id = requestBody.getPsp_txn_id();
        System.out.println("ICICI Credit called for PayeeVpa: " + PayeeVpa + ", amount: " + amount);
        CreditResponse response = paymentService.credit(PayeeVpa, amount,rrn,upi_txn_id,psp_txn_id);

        System.out.println("ICICI Credit called");
        System.out.println("Response : "+ response.getStatus());

        return ResponseEntity.ok(response);
    }
}
