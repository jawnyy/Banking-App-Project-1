package com.jonathanbanda.service;

import com.jonathanbanda.exception.AccountNotFoundException;
import com.jonathanbanda.exception.InvalidTransactionException;
import com.jonathanbanda.exception.UnauthorizedAccessException;
import com.jonathanbanda.model.Account;
import com.jonathanbanda.model.AccountType;
import com.jonathanbanda.testutil.FakeAccountDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    private AccountService accountService;
    private FakeAccountDAO fakeAccountDAO;

    private static final String CUSTOMER_ID = "customer-1";
    private static final String OTHER_CUSTOMER_ID = "customer-2";

    @BeforeEach
    void setUp() {
        fakeAccountDAO = new FakeAccountDAO();
        accountService = new AccountService(fakeAccountDAO);
    }

    @Test
    void openAccount_createsAccountWithZeroBalance() {
        Account account = accountService.openAccount(CUSTOMER_ID, AccountType.CHECKING);

        assertNotNull(account.getId());
        assertEquals(CUSTOMER_ID, account.getCustomerId());
        assertEquals(AccountType.CHECKING, account.getType());
        assertEquals(0, account.getBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void getAccountsForCustomer_returnsOnlyThatCustomersAccounts() {
        accountService.openAccount(CUSTOMER_ID, AccountType.CHECKING);
        accountService.openAccount(CUSTOMER_ID, AccountType.SAVINGS);
        accountService.openAccount(OTHER_CUSTOMER_ID, AccountType.CHECKING);

        List<Account> accounts = accountService.getAccountsForCustomer(CUSTOMER_ID);

        assertEquals(2, accounts.size());
    }

    @Test
    void getBalance_withValidOwner_returnsBalance() throws AccountNotFoundException, UnauthorizedAccessException {
        Account account = accountService.openAccount(CUSTOMER_ID, AccountType.CHECKING);
        fakeAccountDAO.updateAccountBalance(account.getId(), new BigDecimal("100.00"));

        BigDecimal balance = accountService.getBalance(account.getId(), CUSTOMER_ID);

        assertEquals(0, balance.compareTo(new BigDecimal("100.00")));
    }

    @Test
    void getBalance_withNonexistentAccount_throwsException() {
        assertThrows(AccountNotFoundException.class, () ->
                accountService.getBalance("fake-id", CUSTOMER_ID));
    }

    @Test
    void getBalance_withWrongOwner_throwsException() {
        Account account = accountService.openAccount(CUSTOMER_ID, AccountType.CHECKING);

        assertThrows(UnauthorizedAccessException.class, () ->
                accountService.getBalance(account.getId(), OTHER_CUSTOMER_ID));
    }

    @Test
    void closeAccount_withZeroBalance_closesSuccessfully() throws Exception {
        Account account = accountService.openAccount(CUSTOMER_ID, AccountType.CHECKING);

        accountService.closeAccount(account.getId(), CUSTOMER_ID);

        Account closed = fakeAccountDAO.findAccountById(account.getId()).orElseThrow();
        assertEquals(com.jonathanbanda.model.AccountStatus.CLOSED, closed.getStatus());
    }

    @Test
    void closeAccount_withNonzeroBalance_throwsException() {
        Account account = accountService.openAccount(CUSTOMER_ID, AccountType.CHECKING);
        fakeAccountDAO.updateAccountBalance(account.getId(), new BigDecimal("50.00"));

        assertThrows(InvalidTransactionException.class, () ->
                accountService.closeAccount(account.getId(), CUSTOMER_ID));
    }

    @Test
    void closeAccount_withWrongOwner_throwsException() {
        Account account = accountService.openAccount(CUSTOMER_ID, AccountType.CHECKING);

        assertThrows(UnauthorizedAccessException.class, () ->
                accountService.closeAccount(account.getId(), OTHER_CUSTOMER_ID));
    }

    @Test
    void closeAccount_withNonexistentAccount_throwsException() {
        assertThrows(AccountNotFoundException.class, () ->
                accountService.closeAccount("fake-id", CUSTOMER_ID));
    }
}