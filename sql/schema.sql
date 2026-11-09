-- Tables for the Kharcha expense tracker (MySQL 8)
CREATE TABLE IF NOT EXISTS expenses (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount          DECIMAL(12,2) NOT NULL,
    spent_at        DATETIME      NOT NULL,
    merchant        VARCHAR(80)   NOT NULL,
    category        VARCHAR(40)   NOT NULL,
    payment_method  VARCHAR(30)   NOT NULL,
    note            VARCHAR(255),
    created_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_expenses_spent_at (spent_at),
    INDEX idx_expenses_merchant (merchant)
);

CREATE TABLE IF NOT EXISTS income (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount       DECIMAL(12,2) NOT NULL,
    received_on  DATE          NOT NULL,
    source       VARCHAR(80)   NOT NULL
);

CREATE TABLE IF NOT EXISTS settings (
    setting_key    VARCHAR(50)  PRIMARY KEY,
    setting_value  VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS merchant_rules (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword   VARCHAR(60) NOT NULL UNIQUE,
    merchant  VARCHAR(80) NOT NULL,
    category  VARCHAR(40) NOT NULL
);
