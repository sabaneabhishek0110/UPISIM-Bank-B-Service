package com.example.icici.controller;

import com.example.icici.dto.*;
import com.example.icici.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.sql.results.graph.collection.internal.BagInitializer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/bank")
public class PaymentController {
    private PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

//    @PostMapping("/debit")
//    public ResponseEntity<DebitResponse> debit(@RequestBody DebitRequest requestBody, HttpServletRequest request) {
//        String PayerVpa = requestBody.getPayerVpa();
//        BigDecimal amount = requestBody.getAmount();
//        String pin = requestBody.getPin();
//        String rrn = requestBody.getRrn();
//        String upiTxnId = requestBody.getUpiTxnId();
//        String pspTxnId = requestBody.getPspTxnId();
//        DebitResponse response = paymentService.debit(PayerVpa,amount,pin,rrn,upiTxnId,pspTxnId);
//
//        System.out.println("ICICI Debit called");
//        System.out.println("Response : "+ response.getStatus());
//
//        return ResponseEntity.ok(response);
//    }
//
//    @PostMapping("/credit")
//    public ResponseEntity<CreditResponse> credit(@RequestBody CreditRequest requestBody, HttpServletRequest request) {
//        String PayeeVpa = requestBody.getPayeeVpa();
//        BigDecimal amount = requestBody.getAmount();
//        String rrn = requestBody.getRrn();
//        String upiTxnId = requestBody.getUpiTxnId();
//        String pspTxnId = requestBody.getPspTxnId();
//        System.out.println("ICICI Credit called for PayeeVpa: " + PayeeVpa + ", amount: " + amount);
//        CreditResponse response = paymentService.credit(PayeeVpa, amount,rrn,upiTxnId,pspTxnId);
//
//        System.out.println("ICICI Credit called");
//        System.out.println("Response : "+ response.getStatus());
//
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/debit")
    public ResponseEntity<BankResponse> debit(
            @RequestBody DebitRequest request) {

        BankResponse response = paymentService.handleDebit(request);
        return ResponseEntity.ok(response); // always 200
    }

    @PostMapping("/credit")
    public ResponseEntity<BankResponse> credit(
            @RequestBody CreditRequest request) {

        BankResponse response = paymentService.handleCredit(request);
        return ResponseEntity.ok(response); // always 200
    }

    @PostMapping("/reversal")
    public ResponseEntity<BankResponse> reverse(
            @RequestBody ReversalRequest request) {

        BankResponse response = paymentService.handleReversal(request);
        return ResponseEntity.ok(response);
    }
}
