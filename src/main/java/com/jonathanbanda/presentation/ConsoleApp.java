package com.jonathanbanda.presentation;

import com.jonathanbanda.model.Customer;
import com.jonathanbanda.service.AccountService;
import com.jonathanbanda.service.CustomerService;
import com.jonathanbanda.service.TransactionService;

import java.util.Scanner;

public class ConsoleApp {

    private final AuthMenu authMenu;
    private final AccountMenu accountMenu;
    private final TransactionMenu transactionMenu;
    private final InputValidator inputValidator;

    public ConsoleApp(CustomerService customerService, AccountService accountService,
                      TransactionService transactionService) {

        Scanner scanner = new Scanner(System.in);
        this.inputValidator = new InputValidator(scanner);

        this.authMenu = new AuthMenu(customerService, inputValidator);
        this.accountMenu = new AccountMenu(accountService, inputValidator);
        this.transactionMenu = new TransactionMenu(transactionService, inputValidator);
    }

    public void run() {
        System.out.println("==================================");
        System.out.println("      Banking Application");
        System.out.println("==================================");

        Customer currentCustomer = authMenu.showMenu();

        if (currentCustomer == null) {
            System.out.println("Goodbye!");
            return;
        }

        showMainMenu(currentCustomer);
    }

    private void showMainMenu(Customer customer) {
        boolean loggedOut = false;

        while (!loggedOut) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("Logged in as: " + customer.getFirstName() + " " + customer.getLastName());
            System.out.println("1. Accounts");
            System.out.println("2. Transactions");
            System.out.println("3. Log out");

            int choice = inputValidator.readMenuOption("Choose an option: ", 1, 3);

            switch (choice) {
                case 1 -> accountMenu.showMenu(customer.getId());
                case 2 -> transactionMenu.showMenu(customer.getId());
                case 3 -> loggedOut = true;
            }
        }

        System.out.println("Logged out. Goodbye!");
    }
}