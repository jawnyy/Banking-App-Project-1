package com.jonathanbanda.dao;

import com.jonathanbanda.model.Transaction;
import com.jonathanbanda.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionDAO {
    Transaction create(Transaction transaction);
    List<Transaction> findByAccountId(String accountId);
    List<Transaction> findByAccountIdAndType(String accountId, TransactionType type);
    List<Transaction> findByAccountIdAndDateRange(String accountId, LocalDateTime start, LocalDateTime end);
    void executeTransfer(String fromAccountId, String toAccountId, BigDecimal amount, BigDecimal fromResultingBalance, BigDecimal toResultingBalance);
}
