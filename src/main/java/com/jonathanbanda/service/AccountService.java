package com.jonathanbanda.service;

import com.jonathanbanda.dao.AccountDAO;
import com.jonathanbanda.exception.AccountNotFoundException;
import com.jonathanbanda.exception.InvalidTransactionException;
import com.jonathanbanda.exception.UnauthorizedAccessException;
import com.jonathanbanda.model.Account;
import com.jonathanbanda.model.AccountStatus;
import com.jonathanbanda.model.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AccountService {
    private final AccountDAO accountDAO;

    public AccountService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    public Account openAccount(String customerId, AccountType type) {
        BigDecimal balance = BigDecimal.ZERO;

        Account newAccount = new Account(
                null,
                customerId,
                type,
                balance,
                LocalDateTime.now(),
                AccountStatus.ACTIVE
        );

        return accountDAO.createAccount(newAccount);
    }

    public List<Account> getAccountsForCustomer(String customerId) {
        return accountDAO.findAllAccounts(customerId);
    }

    public BigDecimal getBalance(String accountId, String customerId) throws AccountNotFoundException,
            UnauthorizedAccessException {
        Account acc = accountDAO.findAccountById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account was not found!"));

        if (!acc.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Account was not found!");
        }

        return acc.getBalance();
    }

    public void closeAccount(String accountId, String customerId) throws AccountNotFoundException,
            UnauthorizedAccessException, InvalidTransactionException {
        Account acc = accountDAO.findAccountById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account was not found!"));

        if (!acc.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Account was not found!");
        }

        if (acc.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new InvalidTransactionException("Account must have a zero balance before it can be closed.");
        }

        accountDAO.updateStatus(accountId, AccountStatus.CLOSED);
    }
}
