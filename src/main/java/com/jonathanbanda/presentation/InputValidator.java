package com.jonathanbanda.presentation;

import com.jonathanbanda.model.AccountType;

import java.math.BigDecimal;
import java.util.Scanner;

public class InputValidator {

    private final Scanner scanner;

    public InputValidator(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {
                return input;
            }

            System.out.println("Input cannot be empty. Please try again.");
        }
    }

    public BigDecimal readPositiveAmount(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                BigDecimal amount = new BigDecimal(input);

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("Amount must be greater than zero. Please try again.");
                    continue;
                }

                return amount;

            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a valid number (e.g. 100.00).");
            }
        }
    }

    public int readMenuOption(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);

                if (choice < min || choice > max) {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                    continue;
                }

                return choice;

            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    public AccountType readAccountType(String prompt) {
        while (true) {
            System.out.print(prompt + " (CHECKING/SAVINGS): ");
            String input = scanner.nextLine().trim().toUpperCase();

            try {
                return AccountType.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid account type. Please enter CHECKING or SAVINGS.");
            }
        }
    }

    public boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt + " (Y/N): ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("Y")) {
                return true;
            } else if (input.equals("N")) {
                return false;
            }

            System.out.println("Please enter Y or N.");
        }
    }
}