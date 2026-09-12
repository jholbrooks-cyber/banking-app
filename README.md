# National Bank of Greece — Online Banking Portal

A Spring Boot web banking application used as a **security practice target**.

> This application is **intentionally vulnerable**. Do not deploy in production.

## Stack
- Java 17 + Spring Boot 3.2
- H2 in-memory database
- Thymeleaf templates
- JWT (weak configuration)

## Run
```bash
mvn spring-boot:run
```
Open `http://localhost:8080`

## Default Accounts
| Username | Password | Role |
|---|---|---|
| admin | admin123 | ADMIN |
| john.doe | password | USER |
| jane.smith | 123456 | USER |

## Practice Areas
- SQL Injection
- Broken Authentication
- IDOR
- XSS (Stored + Reflected)
- CSRF
- Sensitive Data Exposure
- Broken Access Control
- Insecure Deserialization
- XXE
- JWT Weaknesses
