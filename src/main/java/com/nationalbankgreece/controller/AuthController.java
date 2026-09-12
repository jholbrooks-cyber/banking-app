package com.nationalbankgreece.controller;

import com.nationalbankgreece.model.User;
import com.nationalbankgreece.service.UserService;
import com.nationalbankgreece.util.JwtUtil;
import com.nationalbankgreece.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/")
    public String index(HttpServletRequest request) {
        if (SessionUtil.isLoggedIn(request)) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String redirect,
                            Model model) {
        model.addAttribute("error", error);
        // Open redirect — redirect param not validated
        model.addAttribute("redirect", redirect);
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam(required = false) String redirect,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        Model model) {
        // No rate limiting — brute force allowed
        User user = userService.loginWithSqlInjection(username, password);

        if (user == null) {
            // Verbose error — distinguishes "wrong password" from "user not found"
            boolean exists = userService.findByUsername(username).isPresent();
            model.addAttribute("error", exists ? "Incorrect password" : "User not found: " + username);
            return "login";
        }

        SessionUtil.createSession(user.getId(), response);

        // JWT also issued — dual-token anti-pattern
        String jwt = jwtUtil.generateToken(user.getUsername(), user.getRole());
        response.setHeader("X-Auth-Token", jwt); // token in response header, not httpOnly cookie

        // Open redirect — attacker can redirect to phishing site
        if (redirect != null && !redirect.isEmpty()) {
            return "redirect:" + redirect;
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        SessionUtil.invalidateSession(request, response);
        return "redirect:/login";
    }

    // Password reset — no token, just secret answer in a GET request
    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String username,
                                @RequestParam String answer,
                                @RequestParam String newPassword,
                                Model model) {
        boolean ok = userService.resetPasswordBySecret(username, answer, newPassword);
        if (ok) {
            model.addAttribute("message", "Password reset successful for " + username);
        } else {
            model.addAttribute("error", "Invalid answer for user: " + username);
        }
        return "reset-password";
    }

    // Registration — no email verification, no CAPTCHA
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user,
                           HttpServletResponse response,
                           Model model) {
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }
        // Role is taken directly from form — privilege escalation via mass assignment
        User saved = userService.save(user);
        SessionUtil.createSession(saved.getId(), response);
        return "redirect:/dashboard";
    }
}
