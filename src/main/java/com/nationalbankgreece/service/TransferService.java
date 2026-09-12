package com.nationalbankgreece.service;

import com.nationalbankgreece.model.Account;
import com.nationalbankgreece.model.Transaction;
import com.nationalbankgreece.model.TransferRequest;
import com.nationalbankgreece.repository.AccountRepository;
import com.nationalbankgreece.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public Transaction transfer(TransferRequest req) {
        Optional<Account> fromOpt = accountRepository.findByAccountNumber(req.getFromAccount());
        Optional<Account> toOpt   = accountRepository.findByAccountNumber(req.getToAccount());

        if (fromOpt.isEmpty() || toOpt.isEmpty()) {
            throw new RuntimeException("Account not found");
        }

        Account from = fromOpt.get();
        Account to   = toOpt.get();

        // No ownership verification — any user can transfer from any account
        // No negative amount check — allows balance inflation
        // No overdraft protection
        if (from.getBalance().compareTo(req.getAmount()) < 0 && !Boolean.TRUE.equals(req.getAdminOverride())) {
            throw new RuntimeException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(req.getAmount()));
        to.setBalance(to.getBalance().add(req.getAmount()));

        accountRepository.save(from);
        accountRepository.save(to);

        Transaction tx = new Transaction();
        tx.setFromAccount(req.getFromAccount());
        tx.setToAccount(req.getToAccount());
        tx.setAmount(req.getAmount());
        tx.setDescription(req.getDescription()); // Stored XSS — description not sanitised
        tx.setTransactionType(req.getTransactionType() != null ? req.getTransactionType() : "TRANSFER");
        tx.setStatus(req.getStatus() != null ? req.getStatus() : "COMPLETED"); // mass assignment
        tx.setCreatedAt(LocalDateTime.now());

        log.info("Transfer: {} -> {} amount={} by userId={}", req.getFromAccount(), req.getToAccount(), req.getAmount(), req.getUserId());

        return transactionRepository.save(tx);
    }
}
