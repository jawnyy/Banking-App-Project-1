package com.jonathanbanda.dao;

import com.jonathanbanda.model.Customer;

import java.util.Optional;

public interface CustomerDAO {
    Customer createCustomer(Customer customer);
    Optional<Customer> findById(String id);
    Optional<Customer> findByUsername(String username);
    void updateCustomer(Customer customer);
    boolean existsById(String id);
    boolean existsByUsername(String username);
}
