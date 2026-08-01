package com.jonathanbanda.model;

import java.time.LocalDateTime;

public class Customer {
    private String id;
    private String firstName;
    private String lastName;
    private String username;
    private String hashedPassword;
    private LocalDateTime date;

    public Customer(String id, String firstName, String lastName, String username, String hashedPassword,
                       LocalDateTime date) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.date = date;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Customer id: " + id + ", First name: " + firstName + ", Last name: " + lastName +
                ", username: " + username;
    }
}
