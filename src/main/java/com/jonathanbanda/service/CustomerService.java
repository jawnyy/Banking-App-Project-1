package com.jonathanbanda.service;

import com.jonathanbanda.dao.CustomerDAO;
import com.jonathanbanda.exception.DuplicateCustomerException;
import com.jonathanbanda.exception.UnauthorizedAccessException;
import com.jonathanbanda.model.Customer;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;

public class CustomerService {

    private final CustomerDAO customerDAO;

    public CustomerService(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    public Customer register(String firstName, String lastName, String username, String password)
            throws DuplicateCustomerException {

        if (customerDAO.existsByUsername(username)) {
            throw new DuplicateCustomerException("Username already taken: " + username);
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        Customer newCustomer = new Customer(
                null,
                firstName,
                lastName,
                username,
                hashedPassword,
                LocalDateTime.now()
        );

        return customerDAO.createCustomer(newCustomer);
    }

    public Customer login(String username, String plainPassword) throws UnauthorizedAccessException {
        Customer customer = customerDAO.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedAccessException("Invalid username or password"));

        if (!BCrypt.checkpw(plainPassword, customer.getHashedPassword())) {
            throw new UnauthorizedAccessException("Invalid username or password");
        }

        return customer;
    }

    public void updateProfile(String customerId, String firstName, String lastName) {
        Customer customer = customerDAO.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("No customer found with id: " + customerId));

        customer.setFirstName(firstName);
        customer.setLastName(lastName);

        customerDAO.updateCustomer(customer);
    }
}