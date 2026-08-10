package com.jonathanbanda.presentation;

import com.jonathanbanda.exception.AccountNotFoundException;
import com.jonathanbanda.exception.InvalidTransactionException;
import com.jonathanbanda.exception.UnauthorizedAccessException;
import com.jonathanbanda.model.Account;
import com.jonathanbanda.model.AccountType;
import com.jonathanbanda.service.AccountService;

import java.math.BigDecimal;
import java.util.List;

public class AccountMenu {

    private final AccountService accountService;
    private final InputValidator inputValidator;

    public AccountMenu(AccountService accountService, InputValidator inputValidator) {
        this.accountService = accountService;
        this.inputValidator = inputValidator;
    }

    public void showMenu(String customerId) {
        boolean back = false;

        while (!back) {
            System.out.println("\n=== Accounts ===");
            System.out.println("1. Open a new account");
            System.out.println("2. View my accounts");
            System.out.println("3. View account balance");
            System.out.println("4. Close an account");
            System.out.println("5. Back");

            int choice = inputValidator.readMenuOption("Choose an option: ", 1, 5);

            switch (choice) {
                case 1 -> handleOpenAccount(customerId);
                case 2 -> handleViewAccounts(customerId);
                case 3 -> handleViewBalance(customerId);
                case 4 -> handleCloseAccount(customerId);
                case 5 -> back = true;
            }
        }
    }

    private void handleOpenAccount(String customerId) {
        System.out.println("\n--- Open Account ---");

        AccountType type = inputValidator.readAccountType("Account type");

        Account newAccount = accountService.openAccount(customerId, type);

        System.out.println("Account created successfully!");
        System.out.println("Account ID: " + newAccount.getId());
        System.out.println("Type: " + newAccount.getType());
        System.out.println("Balance: $" + newAccount.getBalance());
    }

    private void handleViewAccounts(String customerId) {
        System.out.println("\n--- My Accounts ---");

        List<Account> accounts = accountService.getAccountsForCustomer(customerId);

        if (accounts.isEmpty()) {
            System.out.println("You don't have any accounts yet.");
            return;
        }

        for (Account account : accounts) {
            System.out.println("ID: " + account.getId()
                    + " | Type: " + account.getType()
                    + " | Status: " + account.getStatus()
                    + " | Balance: $" + account.getBalance());
        }
    }

    private void handleViewBalance(String customerId) {
        System.out.println("\n--- View Balance ---");

        String accountId = inputValidator.readNonEmptyString("Account ID: ");

        try {
            BigDecimal balance = accountService.getBalance(accountId, customerId);
            System.out.println("Current balance: $" + balance);
        } catch (AccountNotFoundException | UnauthorizedAccessException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleCloseAccount(String customerId) {
        System.out.println("\n--- Close Account ---");

        String accountId = inputValidator.readNonEmptyString("Account ID: ");

        boolean confirm = inputValidator.readYesNo("Are you sure you want to close this account?");
        if (!confirm) {
            System.out.println("Close cancelled.");
            return;
        }

        try {
            accountService.closeAccount(accountId, customerId);
            System.out.println("Account closed successfully.");
        } catch (AccountNotFoundException | UnauthorizedAccessException | InvalidTransactionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}