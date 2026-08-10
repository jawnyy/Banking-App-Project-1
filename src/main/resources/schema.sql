-- schema.sql
-- PostgreSQL schema for banking application

CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- CUSTOMERS
CREATE TABLE customers (
                           id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           first_name      VARCHAR(50)  NOT NULL,
                           last_name       VARCHAR(50)  NOT NULL,
                           username        VARCHAR(30)  NOT NULL UNIQUE,
                           hashed_password VARCHAR(255) NOT NULL,
                           created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);


-- ACCOUNTS
CREATE TABLE accounts (
                          id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          customer_id  UUID NOT NULL REFERENCES customers(id),
                          type         VARCHAR(10) NOT NULL CHECK (type IN ('CHECKING', 'SAVINGS')),
                          status       VARCHAR(10) NOT NULL CHECK (status IN ('ACTIVE', 'CLOSED')),
                          balance      NUMERIC(15, 2) NOT NULL DEFAULT 0.00 CHECK (balance >= 0),
                          created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);


-- TRANSACTIONS
CREATE TABLE transactions (
                              id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              account_id          UUID NOT NULL REFERENCES accounts(id),
                              type                VARCHAR(15) NOT NULL
                                  CHECK (type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT')),
                              amount              NUMERIC(15, 2) NOT NULL CHECK (amount > 0),
                              resulting_balance   NUMERIC(15, 2) NOT NULL,
                              timestamp           TIMESTAMP NOT NULL DEFAULT NOW(),
                              related_account_id  UUID REFERENCES accounts(id),
                              description          VARCHAR(255)
);

CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_account_type ON transactions(account_id, type);
CREATE INDEX idx_transactions_account_timestamp ON transactions(account_id, timestamp);