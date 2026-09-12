# National Bank of Greece — Architecture Overview

## Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Template engine | Thymeleaf 3 |
| Persistence | Spring Data JPA + H2 in-memory |
| Build | Maven 3.9 |
| Security | **None** — spring-security intentionally excluded |

---

## Project Structure

```
banking-app/
├── pom.xml
├── README.md
├── docs/
│   ├── architecture.md          (this file)
│   ├── api-endpoints.md
│   └── vulnerability-guide.md
├── plugins/
│   └── checkstyle.xml
└── src/
    ├── main/
    │   ├── java/com/nationalbankgreece/
    │   │   ├── BankingApplication.java
    │   │   ├── config/
    │   │   │   └── WebConfig.java
    │   │   ├── controller/
    │   │   │   ├── AccountController.java
    │   │   │   ├── AdminController.java
    │   │   │   ├── AuthController.java
    │   │   │   ├── DashboardController.java
    │   │   │   ├── ProfileController.java
    │   │   │   ├── TransferController.java
    │   │   │   └── XxeController.java
    │   │   ├── model/
    │   │   │   ├── Account.java
    │   │   │   ├── Message.java
    │   │   │   ├── Transaction.java
    │   │   │   ├── TransferRequest.java
    │   │   │   └── User.java
    │   │   ├── repository/
    │   │   │   ├── AccountRepository.java
    │   │   │   ├── MessageRepository.java
    │   │   │   ├── TransactionRepository.java
    │   │   │   └── UserRepository.java
    │   │   ├── service/
    │   │   │   ├── AccountService.java
    │   │   │   ├── TransferService.java
    │   │   │   └── UserService.java
    │   │   └── util/
    │   │       ├── CryptoUtil.java
    │   │       ├── JwtUtil.java
    │   │       └── SessionUtil.java
    │   └── resources/
    │       ├── application.properties
    │       ├── data.sql
    │       ├── schema.sql
    │       └── templates/
    │           ├── account-detail.html
    │           ├── admin.html
    │           ├── dashboard.html
    │           ├── error.html
    │           ├── login.html
    │           ├── profile.html
    │           ├── profile-search.html
    │           ├── register.html
    │           ├── reset-password.html
    │           └── transfer.html
    └── test/
        └── java/com/nationalbankgreece/
            └── BankingApplicationTests.java
```

---

## Request Flow

```
Browser
  │
  ▼
Spring DispatcherServlet
  │
  ├── No Spring Security filter chain
  │
  ├── Controller receives request
  │   └── Manually reads "NBGSESSION" cookie
  │       └── SessionUtil.getUserIdFromRequest()
  │           └── Returns null if missing (no redirect, controller decides)
  │
  ├── Service layer
  │   └── UserService — raw JDBC via EntityManager.createNativeQuery()
  │   └── AccountService — JPA + no ownership checks
  │   └── TransferService — JPA + accepts client-supplied status fields
  │
  └── Thymeleaf template rendered
      └── th:utext used throughout — HTML not escaped
```

---

## Database Schema

```sql
users (
  id BIGINT PK,
  username VARCHAR UNIQUE,
  password VARCHAR,        -- MD5, no salt
  full_name VARCHAR,
  email VARCHAR,
  phone VARCHAR,
  address VARCHAR,
  role VARCHAR,            -- 'USER' or 'ADMIN'
  secret_question VARCHAR,
  secret_answer VARCHAR    -- plaintext
)

accounts (
  id BIGINT PK,
  account_number VARCHAR UNIQUE,
  user_id BIGINT,          -- FK to users, but no enforcement in app layer
  account_type VARCHAR,
  balance DECIMAL,
  iban VARCHAR,
  currency VARCHAR
)

transactions (
  id BIGINT PK,
  from_account VARCHAR,
  to_account VARCHAR,
  amount DECIMAL,
  transaction_type VARCHAR,
  description VARCHAR,     -- stored raw, rendered with th:utext
  status VARCHAR,
  created_at TIMESTAMP
)

messages (
  id BIGINT PK,
  user_id BIGINT,
  subject VARCHAR,         -- stored raw, rendered with th:utext
  body CLOB,               -- stored raw, rendered with th:utext
  created_at TIMESTAMP
)
```

---

## Cryptography Inventory

| Usage | Algorithm | Issue |
|---|---|---|
| Password hashing | MD5, no salt | Preimage tables, rainbow tables, offline crack |
| AES encryption util | AES/ECB/PKCS5Padding, hardcoded key `NBGBankKey123456` | ECB mode leaks patterns; key in source code |
| JWT signing | HS256, secret `NBGSecretKey2024` | Dictionary attack; no algorithm pinning |
| Session tokens | Sequential integer (`AtomicLong` from 1000) | Enumerable — no cryptographic randomness |

---

## Intentionally Excluded Security Controls

| Control | Why Excluded |
|---|---|
| Spring Security | No dependency in `pom.xml` |
| CSRF tokens | No `CsrfTokenRepository` wired |
| Input validation | No `@Valid` or `BindingResult` used |
| Output encoding | `th:utext` used instead of `th:text` |
| Parameterized queries | Raw string concatenation in `UserService` |
| Ownership checks | `AccountService.findById()` returns any account |
| Role-based access | `AdminController` checks `userId != null` only |
| Secure cookie flags | `SessionUtil` sets no `HttpOnly`/`Secure`/`SameSite` |
| Rate limiting | No `RateLimiter`, no lockout on login or reset |
| XML entity restrictions | `DocumentBuilderFactory` defaults (entities enabled) |
| URL validation on fetch | No allowlist or scheme check in `XxeController.fetchUrl()` |
| Deserialization filter | `ObjectInputStream` with no `ObjectInputFilter` |
| Password complexity | No regex or length validation on register |
| Email verification | Registration completes without any verification step |
