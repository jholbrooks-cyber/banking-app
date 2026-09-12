package com.nationalbankgreece.controller;

import com.nationalbankgreece.model.Transaction;
import com.nationalbankgreece.model.TransferRequest;
import com.nationalbankgreece.service.AccountService;
import com.nationalbankgreece.service.TransferService;
import com.nationalbankgreece.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final AccountService accountService;

    @GetMapping
    public String transferPage(HttpServletRequest request, Model model) {
        Long userId = SessionUtil.getUserIdFromRequest(request);
        if (userId == null) return "redirect:/login";

        model.addAttribute("accounts", accountService.findByUserId(userId));
        return "transfer";
    }

    // No CSRF token — any site can submit this form on behalf of the logged-in user
    @PostMapping
    public String doTransfer(@ModelAttribute TransferRequest req,
                             HttpServletRequest request,
                             Model model) {
        Long userId = SessionUtil.getUserIdFromRequest(request);
        if (userId == null) return "redirect:/login";

        try {
            // Mass assignment: req.status, req.transactionType, req.adminOverride
            // all come from the client — not overridden server-side
            Transaction tx = transferService.transfer(req);
            model.addAttribute("success", "Transfer of €" + req.getAmount() + " completed. Ref: TXN-" + tx.getId());
        } catch (Exception e) {
            // Full stack trace in model — exposed to user
            model.addAttribute("error", e.getMessage());
            model.addAttribute("stackTrace", e);
        }

        model.addAttribute("accounts", accountService.findByUserId(userId));
        return "transfer";
    }

    // REST API endpoint — no CSRF protection, no auth check, accepts JSON
    @PostMapping("/api")
    @ResponseBody
    public String apiTransfer(@RequestBody TransferRequest req) {
        // No session check — completely unauthenticated API endpoint
        Transaction tx = transferService.transfer(req);
        return "{\"status\":\"ok\",\"txnId\":" + tx.getId() + "}";
    }
}
