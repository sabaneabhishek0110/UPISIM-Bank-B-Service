package com.example.icici.service;

import com.example.icici.Repository.AccountsRepository;
import com.example.icici.Repository.TransactionRepository;
import com.example.icici.dto.*;
import com.example.icici.model.icici_accounts;
import com.example.icici.model.icici_transactions;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class  PaymentService {
    private final AccountsRepository accountsRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;


    public DebitResponse debit(String vpa, BigDecimal amount,String pin,String rrn,String upi_txn_id,String psp_txn_id) {
        Optional<icici_accounts> account = accountsRepository.findByVpa(vpa);
        DebitResponse response;
        if (account.isEmpty()) {
            response = new DebitResponse(
                    "FAILED",
                    "U03",
                    upi_txn_id,
                    rrn,
                    null,
                    "Account with provided Vpa does not exists"
            );
            return response;
        }
        if(account.get().getStatus().equals("INACTIVE")){
            String bank_txn_id = createTransactions(
                    account.get(),
                    "DEBIT",
                    amount,
                    psp_txn_id,
                    "FAILED",
                    "Account is Inactive"
            ).toString();
            response = new DebitResponse(
                    "FAILED",
                    "U02",
                    upi_txn_id,
                    rrn,
                    bank_txn_id,
                    "Account is Inactive"
            );
            return response;
        }
        String storedPin = account.get().getUpiPinHash();
        if (!storedPin.equals(pin)) {
            String bank_txn_id = createTransactions(
                    account.get(),
                    "DEBIT",
                    amount,
                    psp_txn_id,
                    "FAILED",
                    "Wrong pin entered"
            ).toString();
            response = new DebitResponse(
                    "FAILED",
                    "U01",
                    upi_txn_id,
                    rrn,
                    bank_txn_id,
                    "Wrong pin entered"
            );
            return response;
        }

        BigDecimal balance = account.get().getBalance();


        if(balance.compareTo(amount)<0){
            String bank_txn_id = createTransactions(
                    account.get(),
                    "DEBIT",
                    amount,
                    psp_txn_id,
                    "FAILED",
                    "BALANCE is Not Sufficient"
            ).toString();
            response = new DebitResponse(
                    "FAILED",
                    "U14",
                    upi_txn_id,
                    rrn,
                    bank_txn_id,
                    "BALANCE is Not Sufficient"
            );
            return response;
        }

        accountsRepository.debitBalance(vpa,amount);
        String bank_txn_id = createTransactions(
                account.get(),
                "DEBIT",
                amount,
                psp_txn_id,
                "SUCCESS",
                null
        ).toString();
        response = new DebitResponse(
                "SUCCESS",
                "U00",
                upi_txn_id,
                rrn,
                bank_txn_id,
                null
        );
        return response;
    }

    public CreditResponse credit(String vpa, BigDecimal amount, String rrn, String upi_txn_id,String psp_txn_id) {
        System.out.println("vpa : "+vpa);
        Optional<icici_accounts> account = accountsRepository.findByVpa(vpa);
        CreditResponse response;
        if (account.isEmpty()) {
            response = new CreditResponse(
                    "FAILED",
                    "U03",
                    upi_txn_id,
                    rrn,
                    null,
                    "Account with provided Vpa does not exists"
            );
            return response;
        }
        if(account.get().getStatus().equals("INACTIVE")){
            String bank_txn_id = createTransactions(
                    account.get(),
                    "CREDIT",
                    amount,
                    psp_txn_id,
                    "FAILED",
                    "Account is Inactive"
            ).toString();
            response = new CreditResponse(
                    "FAILED",
                    "U02",
                    upi_txn_id,
                    rrn,
                    bank_txn_id,
                    "Account is Inactive"
            );
            return response;
        }

        BigDecimal balance = account.get().getBalance();

        double balanceInDouble = balance.doubleValue();

        accountsRepository.creditBalance(vpa,amount);
        String bank_txn_id = createTransactions(
                account.get(),
                "CREDIT",
                amount,
                psp_txn_id,
                "SUCCESS",
                null
        ).toString();
        response = new CreditResponse(
                "SUCCESS",
                "U00",
                upi_txn_id,
                rrn,
                bank_txn_id,
                null
        );
        return response;
    }

    @Transactional
    public BankResponse handleDebit(DebitRequest request) {

        // 1 : Idempotency check
        Optional<icici_transactions> existing =
                transactionRepository.findByUpiTxnId(request.getUpiTxnId());

        if (existing.isPresent()) {
            return new BankResponse(
                    existing.get().getId().toString(),
                    existing.get().getStatus().name(),
                    "Already processed"
            );
        }

        // 2 : Fetch account
        icici_accounts account = accountsRepository.findByVpa(
                request.getPayerVpa()
        ).orElseThrow(() ->
                new RuntimeException("Account not found")
        );

        // 3 : PIN VALIDATION
        if (!passwordEncoder.matches(request.getPin(), account.getUpiPinHash())) {

            icici_transactions failedTxn = new icici_transactions();
            failedTxn.setUpiTxnId(request.getUpiTxnId());
            failedTxn.setPayerVpa(request.getPayerVpa());
            failedTxn.setAmount(request.getAmount());
            failedTxn.setTransactionType(icici_transactions.TransactionType.DEBIT);
            failedTxn.setStatus(icici_transactions.TransactionStatus.FAILED);

            transactionRepository.save(failedTxn);

            return new BankResponse(
                    failedTxn.getId().toString(),
                    "FAILED",
                    "Invalid PIN"
            );
        }

        // 4 : Balance check
        BigDecimal balance = account.getBalance();
        BigDecimal amount = request.getAmount();
        if (balance.compareTo(amount) < 0) {

            icici_transactions failedTxn = new icici_transactions();
            failedTxn.setUpiTxnId(request.getUpiTxnId());
            failedTxn.setPayerVpa(request.getPayerVpa());
            failedTxn.setAmount(request.getAmount());
            failedTxn.setTransactionType(icici_transactions.TransactionType.DEBIT);
            failedTxn.setStatus(icici_transactions.TransactionStatus.FAILED);

            transactionRepository.save(failedTxn);

            return new BankResponse(
                    failedTxn.getId().toString(),
                    "FAILED",
                    "Insufficient balance"
            );
        }

        // 5 : Deduct balance
        account.setBalance(
                balance.subtract(amount)
        );
        accountsRepository.save(account);

        // 6 : Save success transaction
        icici_transactions successTxn = new icici_transactions();
        successTxn.setUpiTxnId(request.getUpiTxnId());
        successTxn.setPayerVpa(request.getPayerVpa());
        successTxn.setAmount(request.getAmount());
        successTxn.setTransactionType(icici_transactions.TransactionType.DEBIT);
        successTxn.setStatus(icici_transactions.TransactionStatus.SUCCESS);

        transactionRepository.save(successTxn);

        return new BankResponse(
                successTxn.getId().toString(),
                "SUCCESS",
                "Debit successful"
        );
    }

    @Transactional
    public BankResponse handleCredit(CreditRequest request) {

        // 1 : Idempotency check
        Optional<icici_transactions> existing =
                transactionRepository.findByUpiTxnId(request.getUpiTxnId());

        if (existing.isPresent()) {
            return new BankResponse(
                    existing.get().getId().toString(),
                    existing.get().getStatus().name(),
                    "Already processed"
            );
        }

        // 2 : Fetch account
        icici_accounts account = accountsRepository.findByVpa(
                request.getPayeeVpa()
        ).orElseThrow(() ->
                new RuntimeException("Account not found")
        );

        BigDecimal balance = account.getBalance();
        BigDecimal amount = request.getAmount();

        // 3 : Add balance
        account.setBalance(
                balance.add(amount)
        );
        accountsRepository.save(account);

        // 4 : Save success transaction
        icici_transactions successTxn = new icici_transactions();
        successTxn.setUpiTxnId(request.getUpiTxnId());
        successTxn.setPayerVpa(request.getPayeeVpa());
        successTxn.setAmount(request.getAmount());
        successTxn.setTransactionType(icici_transactions.TransactionType.CREDIT);
        successTxn.setStatus(icici_transactions.TransactionStatus.SUCCESS);

        transactionRepository.save(successTxn);

        return new BankResponse(
                successTxn.getId().toString(),
                "SUCCESS",
                "Credit successful"
        );
    }

    @Transactional
    public BankResponse handleReversal(ReversalRequest request) {

        // 1 : Check original debit exists
        Optional<icici_transactions> originalTxnOpt =
                transactionRepository.findByUpiTxnId(request.getUpiTxnId());

        if (originalTxnOpt.isEmpty()) {
            return new BankResponse(
                    null,
                    "FAILED",
                    "Original transaction not found"
            );
        }

        icici_transactions originalTxn = originalTxnOpt.get();

        // 2 : Ensure original txn was SUCCESS debit
        if (originalTxn.getTransactionType() != icici_transactions.TransactionType.DEBIT
                || originalTxn.getStatus() != icici_transactions.TransactionStatus.SUCCESS) {

            return new BankResponse(
                    originalTxn.getId().toString(),
                    "FAILED",
                    "Reversal not allowed"
            );
        }

        // 3 : Check if reversal already done (idempotency)
        Optional<icici_transactions> existingReversal =
                transactionRepository.findByUpiTxnId(
                        request.getUpiTxnId()
                );

        if (existingReversal.isPresent() &&
                existingReversal.get().getStatus() == icici_transactions.TransactionStatus.REVERSED
        ) {
            return new BankResponse(
                    existingReversal.get().getId().toString(),
                    existingReversal.get().getStatus().name(),
                    "Already reversed"
            );
        }

        // 4 : Fetch account
        icici_accounts account = accountsRepository.findByVpa(
                request.getPayerVpa()
        ).orElseThrow(() ->
                new RuntimeException("Account not found")
        );

        // 5 : Add money back
        account.setBalance(
                account.getBalance().add(request.getAmount())
        );
        accountsRepository.save(account);

        // 6 : Save reversal transaction
        icici_transactions reversalTxn = new icici_transactions();
        reversalTxn.setUpiTxnId(request.getUpiTxnId());
        reversalTxn.setPayerVpa(request.getPayerVpa());
        reversalTxn.setAmount(request.getAmount());
        reversalTxn.setTransactionType(icici_transactions.TransactionType.REVERSED);
        reversalTxn.setStatus(icici_transactions.TransactionStatus.REVERSED);

        transactionRepository.save(reversalTxn);

        return new BankResponse(
                reversalTxn.getId().toString(),
                "SUCCESS",
                "Reversal successful"
        );
    }


    @Transactional
    public UUID createTransactions(
            icici_accounts account,
            String txnType,
            BigDecimal amount,
            String PspTxnId,
            String status,
            String failureReason
    ){
        icici_transactions transaction = new icici_transactions();
        transaction.setAccountId(account);
        transaction.setTransactionType(Enum.valueOf(icici_transactions.TransactionType.class, txnType));
        transaction.setAmount(amount);
        transaction.setPspTxnId(PspTxnId);
        transaction.setStatus(Enum.valueOf(icici_transactions.TransactionStatus.class, status));
        transaction.setFailure_reason(failureReason);
        transactionRepository.save(transaction);
        return transaction.getId();
    }


    public boolean authenticateUser(String vpa,String pin){
        icici_accounts account = accountsRepository.findByVpa(vpa).get();
        if(account != null && account.getUpiPinHash().equals(pin)){
            return true;
        }
        return false;
    }

    public BigDecimal getAccountBalance(String vpa){
        Optional<icici_accounts> account = accountsRepository.findByVpa(vpa);
        return account.get().getBalance();
    }
}

/*
Code	Meaning
00	Success
U01	PIN incorrect
U02	Account blocked
U03	Account not found
U14	Insufficient balance
U28	Debit timeout
U30	Transaction declined
91	Bank system down
96	System error
 */
