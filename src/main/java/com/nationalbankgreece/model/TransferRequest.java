package com.nationalbankgreece.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    // Mass assignment — all fields bound from HTTP params including ones that shouldn't be
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String description;
    private String currency;
    // These should never come from the client
    private String status;
    private String transactionType;
    private Long userId;
    private Boolean adminOverride;
}
