package com.jonathanbanda.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private String id;
    private String accountId;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal resultingBalance;
    private LocalDateTime timestamp;
    private String relatedAccountId;
    private String description;

    public Transaction(String id, String accountId, TransactionType type, BigDecimal amount,
                       BigDecimal resultingBalance, LocalDateTime timestamp, String relatedAccountId,
                       String description) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.resultingBalance = resultingBalance;
        this.timestamp = timestamp;
        this.relatedAccountId = relatedAccountId;
        this.description = description;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public TransactionType getType() {
        return type;
    }
    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getResultingBalance() {
        return resultingBalance;
    }
    public void setResultingBalance(BigDecimal resultingBalance) {
        this.resultingBalance = resultingBalance;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getRelatedAccountId() {
        return relatedAccountId;
    }
    public void setRelatedAccountId(String relatedAccountId) {
        this.relatedAccountId = relatedAccountId;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + type + " of " + amount + " on account " + accountId
                + " (balance after: " + resultingBalance + ")";
    }
}
