package com.jonathanbanda.testutil;

import com.jonathanbanda.dao.AccountDAO;
import com.jonathanbanda.model.Account;
import com.jonathanbanda.model.AccountStatus;

import java.math.BigDecimal;
import java.util.*;

public class FakeAccountDAO implements AccountDAO {
    private final Map<String, Account> accounts = new HashMap<>();

    @Override
    public Account createAccount(Account account) {
        String id = UUID.randomUUID().toString();

        Account newAccount = new Account(
                id,
                account.getCustomerId(),
                account.getType(),
                account.getBalance(),
                account.getCreatedAt(),
                account.getStatus()
        );

        accounts.put(id, newAccount);
        return newAccount;
    }

    @Override
    public Optional<Account> findAccountById(String id) {
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public List<Account> findAllAccounts(String customerId) {
        List<Account> allAccounts = new ArrayList<>();

        for (Account account : accounts.values()) {
            if (account.getCustomerId().equals(customerId)) {
                allAccounts.add(account);
            }
        }

        return allAccounts;
    }

    @Override
    public void updateAccountBalance(String accountId, BigDecimal balance) {
        Account account = accounts.get(accountId);
        account.setBalance(balance);
    }

    @Override
    public void updateStatus(String accountId, AccountStatus status) {
        Account account = accounts.get(accountId);
        account.setStatus(status);
    }
}
