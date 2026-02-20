package com.example.icici.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "icici_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class icici_accounts {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "ifsc_code", nullable = false)
    private String ifscCode;

    @Column(nullable = false, unique = true)
    private String vpa;

    @Column(name = "account_holder", nullable = false)
    private String accountHolder;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "account_type")
    private String accountType;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(name = "upi_pin_hash", nullable = false)
    private String upiPinHash;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}




/*

this is the trigger create in postgresql for updated_at
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = CURRENT_TIMESTAMP;
   RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_bank_accounts
BEFORE UPDATE ON hdfc_accounts
FOR EACH ROW
EXECUTE FUNCTION update_updated_at();

*/