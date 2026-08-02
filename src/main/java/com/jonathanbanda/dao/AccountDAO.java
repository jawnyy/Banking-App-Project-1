package com.jonathanbanda.dao;

import com.jonathanbanda.exception.DuplicateCustomerException;
import com.jonathanbanda.model.Account;
import com.jonathanbanda.model.AccountStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountDAO {
    Account createAccount(Account account) throws DuplicateCustomerException;
    Optional<Account> findAccountById(String id);
    List<Account> findAllAccounts(String customerId);
    void updateAccountBalance(String accountId, BigDecimal balance);
    void updateStatus(String accountId, AccountStatus status);
}
