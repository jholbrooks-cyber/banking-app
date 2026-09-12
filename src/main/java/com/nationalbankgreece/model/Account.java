package com.nationalbankgreece.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "account_type")
    private String accountType;

    private BigDecimal balance;

    private String currency;

    private String iban;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
