package com.nationalbankgreece.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_account")
    private String fromAccount;

    @Column(name = "to_account")
    private String toAccount;

    private BigDecimal amount;

    private String description;

    @Column(name = "transaction_type")
    private String transactionType;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
