package com.nationalbankgreece.controller;

import com.nationalbankgreece.model.Message;
import com.nationalbankgreece.model.User;
import com.nationalbankgreece.repository.MessageRepository;
import com.nationalbankgreece.service.UserService;
import com.nationalbankgreece.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final MessageRepository messageRepository;

    @GetMapping
    public String profilePage(HttpServletRequest request, Model model) {
        Long userId = SessionUtil.getUserIdFromRequest(request);
        if (userId == null) return "redirect:/login";

        model.addAttribute("user", userService.findById(userId).orElse(null));
        model.addAttribute("messages", messageRepository.findByUserId(userId));
        return "profile";
    }

    // Profile update — accepts all fields including role (privilege escalation)
    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User updatedUser,
                                HttpServletRequest request,
                                Model model) {
        Long userId = SessionUtil.getUserIdFromRequest(request);
        if (userId == null) return "redirect:/login";

        userService.findById(userId).ifPresent(user -> {
            user.setFullName(updatedUser.getFullName());
            user.setEmail(updatedUser.getEmail());
            user.setPhone(updatedUser.getPhone());
            user.setAddress(updatedUser.getAddress());
            // Role taken from form — allows self-promotion to ADMIN
            if (updatedUser.getRole() != null) {
                user.setRole(updatedUser.getRole());
            }
            userService.findAll(); // placeholder; real impl saves
        });

        model.addAttribute("success", "Profile updated");
        return "redirect:/profile";
    }

    // Send message — stored XSS: body stored and rendered without sanitisation
    @PostMapping("/message")
    public String sendMessage(@RequestParam String subject,
                              @RequestParam String body,
                              HttpServletRequest request) {
        Long userId = SessionUtil.getUserIdFromRequest(request);
        if (userId == null) return "redirect:/login";

        Message msg = new Message();
        msg.setUserId(userId);
        msg.setSubject(subject);
        msg.setBody(body);   // Raw HTML stored — renders as markup in admin view
        msg.setCreatedAt(LocalDateTime.now());
        messageRepository.save(msg);

        return "redirect:/profile";
    }

    // Reflected XSS — name param echoed without encoding
    @GetMapping("/search")
    public String searchProfile(@RequestParam(required = false) String name,
                                Model model) {
        model.addAttribute("searchQuery", name);  // rendered via th:utext in template
        if (name != null && !name.isEmpty()) {
            model.addAttribute("results", userService.searchUsers(name));
        }
        return "profile-search";
    }

    // Unvalidated redirect — destination not checked
    @GetMapping("/redirect")
    public String externalRedirect(@RequestParam String url) {
        return "redirect:" + url;
    }
}
