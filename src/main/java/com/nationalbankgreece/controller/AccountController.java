package com.nationalbankgreece.controller;

import com.nationalbankgreece.model.Account;
import com.nationalbankgreece.model.Transaction;
import com.nationalbankgreece.repository.TransactionRepository;
import com.nationalbankgreece.service.AccountService;
import com.nationalbankgreece.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    @GetMapping
    public String listAccounts(HttpServletRequest request, Model model) {
        Long userId = SessionUtil.getUserIdFromRequest(request);
        if (userId == null) return "redirect:/login";

        List<Account> accounts = accountService.findByUserId(userId);
        model.addAttribute("accounts", accounts);
        return "accounts";
    }

    // IDOR — account ID taken from URL, no ownership check
    @GetMapping("/{id}")
    public String viewAccount(@PathVariable Long id,
                              HttpServletRequest request,
                              Model model) {
        Long userId = SessionUtil.getUserIdFromRequest(request);
        if (userId == null) return "redirect:/login";

        // No check: account.getUserId().equals(userId)
        Optional<Account> account = accountService.findById(id);
        if (account.isEmpty()) {
            model.addAttribute("error", "Account not found: " + id);
            return "error";
        }

        List<Transaction> txs = transactionRepository
            .findByFromAccountOrToAccountOrderByCreatedAtDesc(
                account.get().getAccountNumber(),
                account.get().getAccountNumber()
            );

        model.addAttribute("account", account.get());
        model.addAttribute("transactions", txs);
        return "account-detail";
    }

    // Account statement — full data exposed in JSON without auth check on account ownership
    @GetMapping("/{id}/statement")
    @ResponseBody
    public Account getStatement(@PathVariable Long id, HttpServletRequest request) {
        if (!SessionUtil.isLoggedIn(request)) {
            throw new RuntimeException("Not authenticated");
        }
        // Returns full account object including balance, IBAN — no ownership check
        return accountService.findById(id)
            .orElseThrow(() -> new RuntimeException("Account " + id + " not found"));
    }

    // Balance check via account number — enumerable, no ownership verification
    @GetMapping("/balance")
    @ResponseBody
    public String getBalance(@RequestParam String accountNumber) {
        return accountService.findByAccountNumber(accountNumber)
            .map(a -> "{\"balance\": " + a.getBalance() + ", \"iban\": \"" + a.getIban() + "\"}")
            .orElse("{\"error\": \"not found\"}");
    }
}
