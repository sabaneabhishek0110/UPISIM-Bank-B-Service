package com.example.icici.Repository;

import com.example.icici.model.icici_accounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface AccountsRepository extends JpaRepository<icici_accounts, String> {
    Optional<icici_accounts> findByVpa(@Param("vpa") String vpa);

    @Modifying
    @Transactional
    @Query("UPDATE icici_accounts a SET a.balance = a.balance - :amount WHERE a.vpa = :vpa AND a.balance >= :amount")
    int debitBalance(@Param("vpa") String vpa,@Param("amount") double amount);

    @Modifying
    @Transactional
    @Query("UPDATE icici_accounts a SET a.balance = a.balance + :amount WHERE a.vpa = :vpa")
    int creditBalance(@Param("vpa") String vpa,@Param("amount") double amount);
}

