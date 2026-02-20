package com.example.icici.Repository;

import com.example.icici.model.icici_transactions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<icici_transactions, UUID> {

}
