package com.nationalbankgreece.controller;

import com.nationalbankgreece.model.User;
import com.nationalbankgreece.service.AccountService;
import com.nationalbankgreece.service.UserService;
import com.nationalbankgreece.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AccountService accountService;

    // No role check — any logged-in user (or even unauthenticated) can access admin
    @GetMapping
    public String adminDashboard(HttpServletRequest request, Model model) {
        // Only checks login, not ADMIN role
        Long userId = SessionUtil.getUserIdFromRequest(request);
        if (userId == null) return "redirect:/login";

        model.addAttribute("users", userService.findAll());
        model.addAttribute("accounts", accountService.findAll());
        return "admin";
    }

    // Exposes all user data including hashed passwords
    @GetMapping("/users")
    @ResponseBody
    public List<User> getAllUsers() {
        // No authentication check at all
        return userService.findAll();
    }

    // Search with SQL injection
    @GetMapping("/search")
    public String searchUsers(@RequestParam String q,
                              HttpServletRequest request,
                              Model model) {
        Long userId = SessionUtil.getUserIdFromRequest(request);
        if (userId == null) return "redirect:/login";

        List<User> results = userService.searchUsers(q);
        // q is reflected directly into the model and rendered unescaped
        model.addAttribute("query", q);
        model.addAttribute("results", results);
        model.addAttribute("users", userService.findAll());
        model.addAttribute("accounts", accountService.findAll());
        return "admin";
    }

    // Promote any user to admin — no verification
    @PostMapping("/promote/{id}")
    @ResponseBody
    public String promoteUser(@PathVariable Long id) {
        return userService.findById(id).map(u -> {
            u.setRole("ADMIN");
            userService.findAll(); // triggers save indirectly through dirty checking demo
            return "User " + id + " promoted to ADMIN";
        }).orElse("User not found");
    }

    // Delete user — no CSRF, no confirmation
    @GetMapping("/delete/{id}")
    @ResponseBody
    public String deleteUser(@PathVariable Long id) {
        userService.findById(id).ifPresent(u -> {
            // In a real app this would call userRepository.delete(u)
        });
        return "Deleted user " + id;
    }
}
