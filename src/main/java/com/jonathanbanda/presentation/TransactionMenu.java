package com.jonathanbanda.presentation;

import com.jonathanbanda.exception.AccountNotFoundException;
import com.jonathanbanda.exception.InsufficientFundsException;
import com.jonathanbanda.exception.InvalidTransactionException;
import com.jonathanbanda.exception.UnauthorizedAccessException;
import com.jonathanbanda.model.Transaction;
import com.jonathanbanda.model.TransactionType;
import com.jonathanbanda.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TransactionMenu {

    private final TransactionService transactionService;
    private final InputValidator inputValidator;

    public TransactionMenu(TransactionService transactionService, InputValidator inputValidator) {
        this.transactionService = transactionService;
        this.inputValidator = inputValidator;
    }

    public void showMenu(String customerId) {
        boolean back = false;

        while (!back) {
            System.out.println("\n=== Transactions ===");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Transfer");
            System.out.println("4. View transaction history");
            System.out.println("5. Filter history by type");
            System.out.println("6. Filter history by date range");
            System.out.println("7. Back");

            int choice = inputValidator.readMenuOption("Choose an option: ", 1, 7);

            switch (choice) {
                case 1 -> handleDeposit(customerId);
                case 2 -> handleWithdraw(customerId);
                case 3 -> handleTransfer(customerId);
                case 4 -> handleViewHistory(customerId);
                case 5 -> handleViewHistoryByType(customerId);
                case 6 -> handleViewHistoryByDateRange(customerId);
                case 7 -> back = true;
            }
        }
    }

    private void handleDeposit(String customerId) {
        System.out.println("\n--- Deposit ---");

        String accountId = inputValidator.readNonEmptyString("Account ID: ");
        BigDecimal amount = inputValidator.readPositiveAmount("Deposit amount: $");

        try {
            Transaction transaction = transactionService.deposit(accountId, customerId, amount);
            System.out.println("Deposit successful! New balance: $" + transaction.getResultingBalance());
        } catch (AccountNotFoundException | UnauthorizedAccessException | InvalidTransactionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleWithdraw(String customerId) {
        System.out.println("\n--- Withdraw ---");

        String accountId = inputValidator.readNonEmptyString("Account ID: ");
        BigDecimal amount = inputValidator.readPositiveAmount("Withdrawal amount: $");

        try {
            Transaction transaction = transactionService.withdraw(accountId, customerId, amount);
            System.out.println("Withdrawal successful! New balance: $" + transaction.getResultingBalance());
        } catch (AccountNotFoundException | UnauthorizedAccessException
                 | InsufficientFundsException | InvalidTransactionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleTransfer(String customerId) {
        System.out.println("\n--- Transfer ---");

        String fromAccountId = inputValidator.readNonEmptyString("From account ID: ");
        String toAccountId = inputValidator.readNonEmptyString("To account ID: ");
        BigDecimal amount = inputValidator.readPositiveAmount("Transfer amount: $");

        try {
            transactionService.transfer(fromAccountId, toAccountId, customerId, amount);
            System.out.println("Transfer successful!");
        } catch (AccountNotFoundException | UnauthorizedAccessException
                 | InsufficientFundsException | InvalidTransactionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleViewHistory(String customerId) {
        System.out.println("\n--- Transaction History ---");

        String accountId = inputValidator.readNonEmptyString("Account ID: ");

        try {
            List<Transaction> history = transactionService.getTransactionHistory(accountId, customerId);
            printHistory(history);
        } catch (AccountNotFoundException | UnauthorizedAccessException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleViewHistoryByType(String customerId) {
        System.out.println("\n--- Filter by Type ---");

        String accountId = inputValidator.readNonEmptyString("Account ID: ");

        System.out.println("Types: 1. (DEPOSIT)  2. (WITHDRAWAL)  3. (TRANSFER_IN)  4. (TRANSFER_OUT)");
        int typeChoice = inputValidator.readMenuOption("Choose a type: ", 1, 4);
        TransactionType type = switch (typeChoice) {
            case 1 -> TransactionType.DEPOSIT;
            case 2 -> TransactionType.WITHDRAWAL;
            case 3 -> TransactionType.TRANSFER_IN;
            default -> TransactionType.TRANSFER_OUT;
        };

        try {
            List<Transaction> history = transactionService.getTransactionHistoryByType(accountId, customerId, type);
            printHistory(history);
        } catch (AccountNotFoundException | UnauthorizedAccessException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleViewHistoryByDateRange(String customerId) {
        System.out.println("\n--- Filter by Date Range ---");

        String accountId = inputValidator.readNonEmptyString("Account ID: ");
        int daysBack = inputValidator.readMenuOption("Show activity from how many days ago? (1-365): ", 1, 365);

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(daysBack);

        try {
            List<Transaction> history = transactionService.getTransactionHistoryByDateRange(accountId, customerId, start, end);
            printHistory(history);
        } catch (AccountNotFoundException | UnauthorizedAccessException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void printHistory(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        for (Transaction t : transactions) {
            System.out.println(t.getTimestamp()
                    + " | " + t.getType()
                    + " | $" + t.getAmount()
                    + " | Balance after: $" + t.getResultingBalance()
                    + (t.getDescription() != null ? " | " + t.getDescription() : ""));
        }
    }
}