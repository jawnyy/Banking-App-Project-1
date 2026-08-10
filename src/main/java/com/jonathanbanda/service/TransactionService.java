package com.jonathanbanda.service;

import com.jonathanbanda.dao.AccountDAO;
import com.jonathanbanda.dao.TransactionDAO;
import com.jonathanbanda.exception.AccountNotFoundException;
import com.jonathanbanda.exception.InsufficientFundsException;
import com.jonathanbanda.exception.InvalidTransactionException;
import com.jonathanbanda.exception.UnauthorizedAccessException;
import com.jonathanbanda.model.Account;
import com.jonathanbanda.model.AccountStatus;
import com.jonathanbanda.model.Transaction;
import com.jonathanbanda.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TransactionService {
    private final TransactionDAO transactionDAO;
    private final AccountDAO accountDAO;

    public TransactionService(TransactionDAO transactionDAO, AccountDAO accountDAO) {
        this.transactionDAO = transactionDAO;
        this.accountDAO = accountDAO;
    }


    public Transaction deposit(String accountId, String customerId, BigDecimal amount)
            throws AccountNotFoundException, UnauthorizedAccessException, InvalidTransactionException {
        Account account = accountDAO.findAccountById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account was not found!"));

        if (!account.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Account was not found!");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransactionException("Cannot deposit into a closed account.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be bigger than zero!");
        }

        BigDecimal newBalance = account.getBalance().add(amount);
        accountDAO.updateAccountBalance(accountId, newBalance);

        Transaction newTransaction = new Transaction(
                null,
                accountId,
                TransactionType.DEPOSIT,
                amount,
                newBalance,
                LocalDateTime.now(),
                null,
                "Deposited $" + amount
        );

        return transactionDAO.create(newTransaction);
    }

    public Transaction withdraw(String accountId, String customerId, BigDecimal amount)
            throws AccountNotFoundException, UnauthorizedAccessException, InsufficientFundsException, InvalidTransactionException {
        Account account = accountDAO.findAccountById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account was not found!"));

        if (!account.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Account was not found!");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransactionException("Cannot deposit into a closed account.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be bigger than zero!");
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException("Insufficient funds in account!");
        }
        accountDAO.updateAccountBalance(accountId, newBalance);

        Transaction newTransaction = new Transaction(
                null,
                accountId,
                TransactionType.WITHDRAWAL,
                amount,
                newBalance,
                LocalDateTime.now(),
                null,
                "Withdrew $" + amount
        );

        return transactionDAO.create(newTransaction);
    }

    public void transfer(String fromAccountId, String toAccountId, String customerId, BigDecimal amount)
            throws AccountNotFoundException, UnauthorizedAccessException, InsufficientFundsException, InvalidTransactionException {
        Account fromAccount = accountDAO.findAccountById(fromAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Account was not found!"));

        if (!fromAccount.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Account was not found!");
        }

        Account toAccount = accountDAO.findAccountById(toAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Account was not found!"));

        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransactionException("Cannot transfer from a closed account!");
        }

        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransactionException("Cannot transfer to a closed account!");
        }

        if (fromAccountId.equals(toAccountId)) {
            throw new InvalidTransactionException("Cannot transfer to the same account!");
        }


        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be bigger than zero!");
        }

        BigDecimal sendingBalance = fromAccount.getBalance().subtract(amount);
        if (sendingBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException("Insufficient funds in account!");
        }
        BigDecimal receivingBalance = toAccount.getBalance().add(amount);

        accountDAO.updateAccountBalance(fromAccountId, sendingBalance);
        accountDAO.updateAccountBalance(toAccountId, receivingBalance);

        transactionDAO.executeTransfer(fromAccountId, toAccountId, amount, sendingBalance, receivingBalance);
    }

    public List<Transaction> getTransactionHistory(String accountId, String customerId)
            throws AccountNotFoundException, UnauthorizedAccessException {
        Account account = accountDAO.findAccountById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account was not found!"));

        if (!account.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Account was not found!");
        }

        return transactionDAO.findByAccountId(accountId);
    }

    public List<Transaction> getTransactionHistoryByType(String accountId, String customerId, TransactionType type)
            throws AccountNotFoundException, UnauthorizedAccessException {
        Account account = accountDAO.findAccountById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account was not found!"));

        if (!account.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Account was not found!");
        }

        return transactionDAO.findByAccountIdAndType(accountId, type);
    }

    public List<Transaction> getTransactionHistoryByDateRange(String accountId, String customerId, LocalDateTime start, LocalDateTime end)
            throws AccountNotFoundException, UnauthorizedAccessException {
        Account account = accountDAO.findAccountById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account was not found!"));

        if (!account.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Account was not found!");
        }

        return transactionDAO.findByAccountIdAndDateRange(accountId, start, end);
    }
}
