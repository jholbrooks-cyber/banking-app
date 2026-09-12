package com.nationalbankgreece.controller;

import com.nationalbankgreece.model.Account;
import com.nationalbankgreece.service.AccountService;
import com.nationalbankgreece.service.UserService;
import com.nationalbankgreece.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final AccountService accountService;

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        Long userId = SessionUtil.getUserIdFromRequest(request);
        if (userId == null) return "redirect:/login";

        userService.findById(userId).ifPresent(u -> model.addAttribute("user", u));
        List<Account> accounts = accountService.findByUserId(userId);
        BigDecimal totalBalance = accounts.stream()
            .map(Account::getBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("accounts", accounts);
        model.addAttribute("totalBalance", totalBalance);
        return "dashboard";
    }
}
