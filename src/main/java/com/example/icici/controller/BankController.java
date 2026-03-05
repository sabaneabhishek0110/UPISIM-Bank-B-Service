package com.example.icici.controller;


import com.example.icici.Repository.AccountsRepository;
import com.example.icici.model.icici_accounts;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
public class BankController {

    private final AccountsRepository repository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/account/create")
    public ResponseEntity<?> createAccount(@RequestBody icici_accounts account) {

        String hashedPin = passwordEncoder.encode(account.getUpiPinHash());
        account.setUpiPinHash(hashedPin);
        icici_accounts savedAccount = repository.save(account);

        Map<String,Object> response = new HashMap<>();
        response.put("message","Account created successfully");
        response.put("account",savedAccount);

        return ResponseEntity.ok(response);
    }
}