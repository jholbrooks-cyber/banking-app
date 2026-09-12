package com.nationalbankgreece.service;

import com.nationalbankgreece.model.User;
import com.nationalbankgreece.repository.UserRepository;
import com.nationalbankgreece.util.CryptoUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // SQL Injection — username concatenated directly into query
    @SuppressWarnings("unchecked")
    public User loginWithSqlInjection(String username, String password) {
        String hashedPassword = CryptoUtil.hashPassword(password);

        // Vulnerable query — attacker can bypass with: admin' OR '1'='1' --
        String sql = "SELECT * FROM users WHERE username = '" + username
                + "' AND password = '" + hashedPassword + "'";

        log.debug("Executing login query: {}", sql);  // logs full query including creds

        List<User> results = entityManager.createNativeQuery(sql, User.class).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User save(User user) {
        // Password stored as MD5, no salting
        user.setPassword(CryptoUtil.hashPassword(user.getPassword()));
        return userRepository.save(user);
    }

    // Password reset with no rate limiting and answer in plaintext
    public boolean resetPasswordBySecret(String username, String answer, String newPassword) {
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) return false;

        User user = opt.get();
        // Case-insensitive plaintext compare — no hashing of answer
        if (user.getSecretAnswer() != null && user.getSecretAnswer().equalsIgnoreCase(answer)) {
            user.setPassword(CryptoUtil.hashPassword(newPassword));
            userRepository.save(user);
            log.info("Password reset for user: {} new_pass: {}", username, newPassword); // logs new password
            return true;
        }
        return false;
    }

    // Search users — reflected in UI without encoding (enables reflected XSS via admin search)
    @SuppressWarnings("unchecked")
    public List<User> searchUsers(String query) {
        String sql = "SELECT * FROM users WHERE full_name LIKE '%" + query + "%' OR email LIKE '%" + query + "%'";
        return entityManager.createNativeQuery(sql, User.class).getResultList();
    }
}
