package com.jonathanbanda.service;

import com.jonathanbanda.exception.DuplicateCustomerException;
import com.jonathanbanda.exception.UnauthorizedAccessException;
import com.jonathanbanda.model.Customer;
import com.jonathanbanda.testutil.FakeCustomerDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerServiceTest {

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        FakeCustomerDAO fakeCustomerDAO = new FakeCustomerDAO();
        customerService = new CustomerService(fakeCustomerDAO);
    }

    @Test
    void register_withValidData_createsCustomer() throws DuplicateCustomerException {
        Customer customer = customerService.register("Jane", "Doe", "janedoe", "password123");

        assertNotNull(customer.getId());
        assertEquals("Jane", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
        assertEquals("janedoe", customer.getUsername());
        assertNotEquals("password123", customer.getHashedPassword()); // must be hashed, not plaintext
    }

    @Test
    void register_withDuplicateUsername_throwsException() throws DuplicateCustomerException {
        customerService.register("Jane", "Doe", "janedoe", "password123");

        assertThrows(DuplicateCustomerException.class, () ->
                customerService.register("John", "Smith", "janedoe", "differentPassword"));
    }

    @Test
    void login_withCorrectCredentials_returnsCustomer() throws DuplicateCustomerException, UnauthorizedAccessException {
        customerService.register("Jane", "Doe", "janedoe", "password123");

        Customer loggedIn = customerService.login("janedoe", "password123");

        assertEquals("janedoe", loggedIn.getUsername());
    }

    @Test
    void login_withWrongPassword_throwsException() throws DuplicateCustomerException {
        customerService.register("Jane", "Doe", "janedoe", "password123");

        assertThrows(UnauthorizedAccessException.class, () ->
                customerService.login("janedoe", "wrongPassword"));
    }

    @Test
    void login_withNonexistentUsername_throwsException() {
        assertThrows(UnauthorizedAccessException.class, () ->
                customerService.login("ghostuser", "anyPassword"));
    }

    @Test
    void updateProfile_withValidId_updatesFields() throws DuplicateCustomerException, UnauthorizedAccessException {
        Customer customer = customerService.register("Jane", "Doe", "janedoe", "password123");

        customerService.updateProfile(customer.getId(), "Janet", "Doerson");

        Customer updated = customerService.login("janedoe", "password123");

        assertEquals("Janet", updated.getFirstName());
        assertEquals("Doerson", updated.getLastName());
    }
}