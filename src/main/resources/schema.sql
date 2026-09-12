CREATE TABLE users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    email       VARCHAR(100) NOT NULL,
    full_name   VARCHAR(100) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    phone       VARCHAR(20),
    address     VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active   BOOLEAN DEFAULT TRUE
);

CREATE TABLE accounts (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number  VARCHAR(20)  NOT NULL UNIQUE,
    user_id         BIGINT       NOT NULL,
    account_type    VARCHAR(20)  NOT NULL,
    balance         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    currency        VARCHAR(3)   NOT NULL DEFAULT 'EUR',
    iban            VARCHAR(34),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE transactions (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_account    VARCHAR(20),
    to_account      VARCHAR(20),
    amount          DECIMAL(15,2) NOT NULL,
    description     VARCHAR(500),
    transaction_type VARCHAR(20) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'COMPLETED',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE messages (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    subject     VARCHAR(255),
    body        TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
