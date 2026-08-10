package com.jonathanbanda.testutil;

import com.jonathanbanda.dao.CustomerDAO;
import com.jonathanbanda.model.Customer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeCustomerDAO implements CustomerDAO {

    private final Map<String, Customer> customers = new HashMap<>();

    @Override
    public Customer createCustomer(Customer customer) {
        String id = UUID.randomUUID().toString();

        Customer newCustomer = new Customer(
                id,
                customer.getFirstName(),
                customer.getLastName(),
                customer.getUsername(),
                customer.getHashedPassword(),
                customer.getCreatedAt()
        );

        customers.put(id, newCustomer);
        return newCustomer;
    }

    @Override
    public Optional<Customer> findById(String id) {
        return Optional.ofNullable(customers.get(id));
    }

    @Override
    public Optional<Customer> findByUsername(String username) {
        return customers.values().stream()
                .filter(c -> c.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public void updateCustomer(Customer customer) {
        customers.put(customer.getId(), customer);
    }

    @Override
    public boolean existsById(String id) {
        return customers.containsKey(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return customers.values().stream()
                .anyMatch(c -> c.getUsername().equals(username));
    }
}