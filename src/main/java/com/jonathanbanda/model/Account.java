package com.jonathanbanda.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {
    private String id;
    private String customerId;
    private AccountType type;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private AccountStatus status;

    public Account(String id, String customerId, AccountType type, BigDecimal balance, LocalDateTime createdAt,
                   AccountStatus status) {
        this.id = id;
        this.customerId = customerId;
        this.type = type;
        this.balance = balance;
        this.createdAt = createdAt;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public AccountStatus getStatus() {
        return status;
    }
    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Account id: " + id + ", Account type: " + type + ", balance: " + balance + ", status " + status;
    }
}
