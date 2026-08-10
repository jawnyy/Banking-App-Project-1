package com.jonathanbanda.service;

import com.jonathanbanda.exception.AccountNotFoundException;
import com.jonathanbanda.exception.InsufficientFundsException;
import com.jonathanbanda.exception.InvalidTransactionException;
import com.jonathanbanda.exception.UnauthorizedAccessException;
import com.jonathanbanda.model.Account;
import com.jonathanbanda.model.AccountType;
import com.jonathanbanda.model.Transaction;
import com.jonathanbanda.model.TransactionType;
import com.jonathanbanda.testutil.FakeAccountDAO;
import com.jonathanbanda.testutil.FakeTransactionDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest {

    private TransactionService transactionService;
    private FakeAccountDAO fakeAccountDAO;
    private FakeTransactionDAO fakeTransactionDAO;

    private static final String CUSTOMER_ID = "customer-1";
    private static final String OTHER_CUSTOMER_ID = "customer-2";

    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        fakeAccountDAO = new FakeAccountDAO();
        fakeTransactionDAO = new FakeTransactionDAO();
        transactionService = new TransactionService(fakeTransactionDAO, fakeAccountDAO);

        AccountService accountService = new AccountService(fakeAccountDAO);
        fromAccount = accountService.openAccount(CUSTOMER_ID, AccountType.CHECKING);
        toAccount = accountService.openAccount(CUSTOMER_ID, AccountType.SAVINGS);

        fakeAccountDAO.updateAccountBalance(fromAccount.getId(), new BigDecimal("100.00"));
    }

    // ---------- deposit ----------

    @Test
    void deposit_withValidAmount_updatesBalanceAndCreatesTransaction()
            throws AccountNotFoundException, UnauthorizedAccessException, InvalidTransactionException {

        Transaction transaction = transactionService.deposit(fromAccount.getId(), CUSTOMER_ID, new BigDecimal("50.00"));

        assertEquals(TransactionType.DEPOSIT, transaction.getType());
        assertEquals(0, transaction.getResultingBalance().compareTo(new BigDecimal("150.00")));

        Account updated = fakeAccountDAO.findAccountById(fromAccount.getId()).orElseThrow();
        assertEquals(0, updated.getBalance().compareTo(new BigDecimal("150.00")));
    }

    @Test
    void deposit_withNegativeAmount_throwsException() {
        assertThrows(InvalidTransactionException.class, () ->
                transactionService.deposit(fromAccount.getId(), CUSTOMER_ID, new BigDecimal("-10.00")));
    }

    @Test
    void deposit_withZeroAmount_throwsException() {
        assertThrows(InvalidTransactionException.class, () ->
                transactionService.deposit(fromAccount.getId(), CUSTOMER_ID, BigDecimal.ZERO));
    }

    @Test
    void deposit_withWrongOwner_throwsException() {
        assertThrows(UnauthorizedAccessException.class, () ->
                transactionService.deposit(fromAccount.getId(), OTHER_CUSTOMER_ID, new BigDecimal("50.00")));
    }

    // ---------- withdraw ----------

    @Test
    void withdraw_withSufficientFunds_updatesBalanceAndCreatesTransaction() throws Exception {
        Transaction transaction = transactionService.withdraw(fromAccount.getId(), CUSTOMER_ID, new BigDecimal("40.00"));

        assertEquals(TransactionType.WITHDRAWAL, transaction.getType());
        assertEquals(0, transaction.getResultingBalance().compareTo(new BigDecimal("60.00")));

        Account updated = fakeAccountDAO.findAccountById(fromAccount.getId()).orElseThrow();
        assertEquals(0, updated.getBalance().compareTo(new BigDecimal("60.00")));
    }

    @Test
    void withdraw_withInsufficientFunds_throwsException() {
        assertThrows(InsufficientFundsException.class, () ->
                transactionService.withdraw(fromAccount.getId(), CUSTOMER_ID, new BigDecimal("500.00")));
    }

    @Test
    void withdraw_withNegativeAmount_throwsException() {
        assertThrows(InvalidTransactionException.class, () ->
                transactionService.withdraw(fromAccount.getId(), CUSTOMER_ID, new BigDecimal("-10.00")));
    }

    @Test
    void withdraw_withWrongOwner_throwsException() {
        assertThrows(UnauthorizedAccessException.class, () ->
                transactionService.withdraw(fromAccount.getId(), OTHER_CUSTOMER_ID, new BigDecimal("10.00")));
    }

    // ---------- transfer ----------

    @Test
    void transfer_withSufficientFunds_updatesBothBalances() throws Exception {
        transactionService.transfer(fromAccount.getId(), toAccount.getId(), CUSTOMER_ID, new BigDecimal("30.00"));

        Account updatedFrom = fakeAccountDAO.findAccountById(fromAccount.getId()).orElseThrow();
        Account updatedTo = fakeAccountDAO.findAccountById(toAccount.getId()).orElseThrow();

        assertEquals(0, updatedFrom.getBalance().compareTo(new BigDecimal("70.00")));
        assertEquals(0, updatedTo.getBalance().compareTo(new BigDecimal("30.00")));
    }

    @Test
    void transfer_withInsufficientFunds_throwsException() {
        assertThrows(InsufficientFundsException.class, () ->
                transactionService.transfer(fromAccount.getId(), toAccount.getId(), CUSTOMER_ID, new BigDecimal("500.00")));
    }

    @Test
    void transfer_withWrongOwner_throwsException() {
        assertThrows(UnauthorizedAccessException.class, () ->
                transactionService.transfer(fromAccount.getId(), toAccount.getId(), OTHER_CUSTOMER_ID, new BigDecimal("10.00")));
    }

    @Test
    void transfer_withNonexistentDestinationAccount_throwsException() {
        assertThrows(AccountNotFoundException.class, () ->
                transactionService.transfer(fromAccount.getId(), "fake-id", CUSTOMER_ID, new BigDecimal("10.00")));
    }

    // ---------- transaction history ----------

    @Test
    void getTransactionHistory_returnsAllTransactionsForAccount() throws Exception {
        transactionService.deposit(fromAccount.getId(), CUSTOMER_ID, new BigDecimal("20.00"));
        transactionService.withdraw(fromAccount.getId(), CUSTOMER_ID, new BigDecimal("10.00"));

        List<Transaction> history = transactionService.getTransactionHistory(fromAccount.getId(), CUSTOMER_ID);

        assertEquals(2, history.size());
    }

    @Test
    void getTransactionHistory_withWrongOwner_throwsException() {
        assertThrows(UnauthorizedAccessException.class, () ->
                transactionService.getTransactionHistory(fromAccount.getId(), OTHER_CUSTOMER_ID));
    }

    @Test
    void getTransactionHistoryByType_returnsOnlyMatchingType() throws Exception {
        transactionService.deposit(fromAccount.getId(), CUSTOMER_ID, new BigDecimal("20.00"));
        transactionService.withdraw(fromAccount.getId(), CUSTOMER_ID, new BigDecimal("10.00"));

        List<Transaction> deposits =
                transactionService.getTransactionHistoryByType(fromAccount.getId(), CUSTOMER_ID, TransactionType.DEPOSIT);

        assertEquals(1, deposits.size());
        assertEquals(TransactionType.DEPOSIT, deposits.get(0).getType());
    }

    @Test
    void getTransactionHistoryByDateRange_returnsTransactionsWithinRange() throws Exception {
        transactionService.deposit(fromAccount.getId(), CUSTOMER_ID, new BigDecimal("20.00"));

        LocalDateTime start = LocalDateTime.now().minusMinutes(5);
        LocalDateTime end = LocalDateTime.now().plusMinutes(5);

        List<Transaction> history =
                transactionService.getTransactionHistoryByDateRange(fromAccount.getId(), CUSTOMER_ID, start, end);

        assertEquals(1, history.size());
    }
}