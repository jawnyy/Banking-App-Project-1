package com.jonathanbanda.dao.postgres;

import com.jonathanbanda.dao.AccountDAO;
import com.jonathanbanda.model.Account;
import com.jonathanbanda.model.AccountStatus;
import com.jonathanbanda.model.AccountType;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PostgresAccountDAO implements AccountDAO {
    private final PostgresConnectionManager connectionManager;

    public PostgresAccountDAO(PostgresConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Account createAccount(Account account) {
        Account newAccount = null;
        String sql = "INSERT INTO accounts (customer_id, type, balance, created_at, status) VALUES (?, ?, ?, ?, ?);";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, UUID.fromString(account.getCustomerId()));
            ps.setString(2, account.getType().name());
            ps.setBigDecimal(3, account.getBalance());
            ps.setTimestamp(4, Timestamp.valueOf(account.getCreatedAt()));
            ps.setString(5, account.getStatus().name());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    String generatedId = keys.getString(1);
                    newAccount = new Account(
                            generatedId,
                            account.getCustomerId(),
                            account.getType(),
                            account.getBalance(),
                            account.getCreatedAt(),
                            account.getStatus()
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return newAccount;
    }

    @Override
    public Optional<Account> findAccountById(String id) {
        Account account = null;
        String sql = "SELECT * FROM accounts WHERE id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, UUID.fromString(id));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    account = new Account(
                            rs.getString("id"),
                            rs.getString("customer_id"),
                            AccountType.valueOf(rs.getString("type")),
                            rs.getBigDecimal("balance"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            AccountStatus.valueOf(rs.getString("status"))
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(account);
    }

    @Override
    public List<Account> findAllAccounts(String customerId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE customer_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, UUID.fromString(customerId));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Account acc = new Account(
                            rs.getString("id"),
                            rs.getString("customer_id"),
                            AccountType.valueOf(rs.getString("type")),
                            rs.getBigDecimal("balance"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            AccountStatus.valueOf(rs.getString("status"))
                    );
                    accounts.add(acc);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    @Override
    public void updateAccountBalance(String accountId, BigDecimal balance) {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, balance);
            ps.setObject(2, UUID.fromString(accountId));

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateStatus(String accountId, AccountStatus status) {
        String sql = "UPDATE accounts SET status = ? WHERE id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setObject(2, UUID.fromString(accountId));

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
