package com.jonathanbanda.presentation;

import com.jonathanbanda.exception.DuplicateCustomerException;
import com.jonathanbanda.exception.UnauthorizedAccessException;
import com.jonathanbanda.model.Customer;
import com.jonathanbanda.service.CustomerService;

public class AuthMenu {

    private final CustomerService customerService;
    private final InputValidator inputValidator;

    public AuthMenu(CustomerService customerService, InputValidator inputValidator) {
        this.customerService = customerService;
        this.inputValidator = inputValidator;
    }

    public Customer showMenu() {
        while (true) {
            System.out.println("\n=== Welcome ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");

            int choice = inputValidator.readMenuOption("Choose an option: ", 1, 3);

            switch (choice) {
                case 1 -> {
                    Customer registered = handleRegister();
                    if (registered != null) {
                        return registered;
                    }
                }
                case 2 -> {
                    Customer loggedIn = handleLogin();
                    if (loggedIn != null) {
                        return loggedIn;
                    }
                }
                case 3 -> {
                    return null;
                }
            }
        }
    }

    private Customer handleRegister() {
        System.out.println("\n--- Register ---");

        String firstName = inputValidator.readNonEmptyString("First name: ");
        String lastName = inputValidator.readNonEmptyString("Last name: ");
        String username = inputValidator.readNonEmptyString("Username: ");
        String password = inputValidator.readNonEmptyString("Password: ");

        try {
            Customer customer = customerService.register(firstName, lastName, username, password);
            System.out.println("Registration successful! Welcome, " + customer.getFirstName() + ".");
            return customer;
        } catch (DuplicateCustomerException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    private Customer handleLogin() {
        System.out.println("\n--- Login ---");

        String username = inputValidator.readNonEmptyString("Username: ");
        String password = inputValidator.readNonEmptyString("Password: ");

        try {
            Customer customer = customerService.login(username, password);
            System.out.println("Login successful! Welcome back, " + customer.getFirstName() + ".");
            return customer;
        } catch (UnauthorizedAccessException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }
}