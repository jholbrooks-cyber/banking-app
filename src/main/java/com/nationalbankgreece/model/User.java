package com.nationalbankgreece.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    // Stored as MD5, no salt
    private String password;

    private String email;

    @Column(name = "full_name")
    private String fullName;

    private String role;

    private String phone;

    private String address;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_active")
    private Boolean isActive;

    // Secret question/answer stored in plaintext
    private String secretQuestion;
    private String secretAnswer;
}
