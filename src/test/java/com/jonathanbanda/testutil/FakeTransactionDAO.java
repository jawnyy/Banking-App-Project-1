package com.jonathanbanda.testutil;

import com.jonathanbanda.dao.TransactionDAO;
import com.jonathanbanda.model.Transaction;
import com.jonathanbanda.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class FakeTransactionDAO implements TransactionDAO {
    private final Map<String,Transaction> transactions = new HashMap<>();

    @Override
    public Transaction create(Transaction transaction) {
        String id = UUID.randomUUID().toString();

        Transaction newTransaction = new Transaction(
                id,
                transaction.getAccountId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getResultingBalance(),
                transaction.getTimestamp(),
                transaction.getRelatedAccountId(),
                transaction.getDescription()
        );

        transactions.put(id, newTransaction);
        return newTransaction;
    }

    @Override
    public List<Transaction> findByAccountId(String accountId) {
        List<Transaction> allTransactions = new ArrayList<>();

        for (Transaction transaction : transactions.values()) {
            if (transaction.getAccountId().equals(accountId)) {
                allTransactions.add(transaction);
            }
        }

        return allTransactions;
    }

    @Override
    public List<Transaction> findByAccountIdAndType(String accountId, TransactionType type) {
        List<Transaction> allTransactions = new ArrayList<>();

        for (Transaction transaction : transactions.values()) {
            if (transaction.getAccountId().equals(accountId) && transaction.getType().equals(type)) {
                allTransactions.add(transaction);
            }
        }

        return allTransactions;
    }

    @Override
    public List<Transaction> findByAccountIdAndDateRange(String accountId, LocalDateTime start, LocalDateTime end) {
        List<Transaction> allTransactions = new ArrayList<>();

        for (Transaction transaction : transactions.values()) {
            boolean inRange = !transaction.getTimestamp().isBefore(start) && !transaction.getTimestamp().isAfter(end);

            if (transaction.getAccountId().equals(accountId) && inRange) {
                allTransactions.add(transaction);
            }
        }

        return allTransactions;
    }

    @Override
    public void executeTransfer(String fromAccountId, String toAccountId, BigDecimal amount,
                                BigDecimal fromResultingBalance, BigDecimal toResultingBalance) {

        LocalDateTime now = LocalDateTime.now();

        Transaction transferOut = new Transaction(
                UUID.randomUUID().toString(),
                fromAccountId,
                TransactionType.TRANSFER_OUT,
                amount,
                fromResultingBalance,
                now,
                toAccountId,
                "Transfer to account " + toAccountId
        );

        Transaction transferIn = new Transaction(
                UUID.randomUUID().toString(),
                toAccountId,
                TransactionType.TRANSFER_IN,
                amount,
                toResultingBalance,
                now,
                fromAccountId,
                "Transfer from account " + fromAccountId
        );

        transactions.put(transferOut.getId(), transferOut);
        transactions.put(transferIn.getId(), transferIn);
    }
}
