package com.jonathanbanda.dao.postgres;

import com.jonathanbanda.dao.CustomerDAO;
import com.jonathanbanda.model.Customer;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class PostgresCustomerDAO implements CustomerDAO {
    private final PostgresConnectionManager connectionManager;

    public PostgresCustomerDAO(PostgresConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Customer createCustomer(Customer customer) {
        Customer newCustomer = null;
        String sql = "INSERT INTO customers (first_name, last_name, username, hashed_password, created_at) VALUES (?, ?, ?, ?, ?);";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getLastName());
            ps.setString(3, customer.getUsername());
            ps.setString(4, customer.getHashedPassword());
            ps.setTimestamp(5, Timestamp.valueOf(customer.getCreatedAt()));

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    String generatedId = keys.getString(1);
                    newCustomer = new Customer(
                            generatedId,
                            customer.getFirstName(),
                            customer.getLastName(),
                            customer.getUsername(),
                            customer.getHashedPassword(),
                            customer.getCreatedAt()
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return newCustomer;
    }

    @Override
    public Optional<Customer> findById(String id) {
        Customer customer = null;
        String sql = "SELECT * FROM customers WHERE id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, UUID.fromString(id));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                customer = new Customer(
                        rs.getString("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("username"),
                        rs.getString("hashed_password"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(customer);
    }

    @Override
    public Optional<Customer> findByUsername(String username) {
        Customer customer = null;
        String sql = "SELECT * FROM customers WHERE username = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                customer = new Customer(
                        rs.getString("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("username"),
                        rs.getString("hashed_password"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(customer);
    }

    @Override
    public void updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET first_name = ?, last_name = ?, username = ?, hashed_password = ? WHERE id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getLastName());
            ps.setString(3, customer.getUsername());
            ps.setString(4, customer.getHashedPassword());
            ps.setObject(5, UUID.fromString(customer.getId()));

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean existsById(String id) {
        boolean exists = false;
        String sql = "SELECT * FROM customers WHERE id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                exists = true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exists;
    }

    @Override
    public boolean existsByUsername(String username) {
        boolean exists = false;
        String sql = "SELECT * FROM customers WHERE username = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                exists = true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exists;
    }
}
