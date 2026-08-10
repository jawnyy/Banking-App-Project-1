package com.jonathanbanda.dao.postgres;

import com.jonathanbanda.dao.TransactionDAO;
import com.jonathanbanda.model.Transaction;
import com.jonathanbanda.model.TransactionType;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostgresTransactionDAO implements TransactionDAO {
    private final PostgresConnectionManager connectionManager;

    public PostgresTransactionDAO(PostgresConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Transaction create(Transaction transaction) {
        Transaction newTransaction = null;
        String sql = "INSERT INTO transactions (account_id, type, amount, resulting_balance, timestamp, related_account_id, description) VALUES (?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, UUID.fromString(transaction.getAccountId()));
            ps.setString(2, transaction.getType().name());
            ps.setBigDecimal(3, transaction.getAmount());
            ps.setBigDecimal(4, transaction.getResultingBalance());
            ps.setTimestamp(5, Timestamp.valueOf(transaction.getTimestamp()));

            if (transaction.getRelatedAccountId() != null) {
                ps.setObject(6, UUID.fromString(transaction.getRelatedAccountId()));
            } else {
                ps.setNull(6, Types.OTHER);
            }

            ps.setString(7, transaction.getDescription());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    String generatedId = keys.getString(1);
                    newTransaction = new Transaction(
                            generatedId,
                            transaction.getAccountId(),
                            transaction.getType(),
                            transaction.getAmount(),
                            transaction.getResultingBalance(),
                            transaction.getTimestamp(),
                            transaction.getRelatedAccountId(),
                            transaction.getDescription()

                    );
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newTransaction;
    }

    @Override
    public List<Transaction> findByAccountId(String accountId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, UUID.fromString(accountId));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    @Override
    public List<Transaction> findByAccountIdAndType(String accountId, TransactionType type) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ? AND type = ?;";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, UUID.fromString(accountId));
            ps.setString(2, type.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    @Override
    public List<Transaction> findByAccountIdAndDateRange(String accountId, LocalDateTime start, LocalDateTime end) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ? AND timestamp between ? and ?;";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, UUID.fromString(accountId));
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ps.setTimestamp(3, Timestamp.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    @Override
    public void executeTransfer(String fromAccountId, String toAccountId, BigDecimal amount,
                                BigDecimal fromResultingBalance, BigDecimal toResultingBalance) {
        String updateBalanceSql = "UPDATE accounts SET balance = ? WHERE id = ?";
        String insertTransactionSql = "INSERT INTO transactions (account_id, type, amount, resulting_balance, timestamp, related_account_id, description) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connectionManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                LocalDateTime now = LocalDateTime.now();
                UUID fromId = UUID.fromString(fromAccountId);
                UUID toId = UUID.fromString(toAccountId);

                // 1. Update sender's balance
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setBigDecimal(1, fromResultingBalance);
                    ps.setObject(2, fromId);
                    ps.executeUpdate();
                }

                // 2. Update receiver's balance
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setBigDecimal(1, toResultingBalance);
                    ps.setObject(2, toId);
                    ps.executeUpdate();
                }

                // 3. Insert TRANSFER_OUT record
                try (PreparedStatement ps = conn.prepareStatement(insertTransactionSql)) {
                    ps.setObject(1, fromId);
                    ps.setString(2, TransactionType.TRANSFER_OUT.name());
                    ps.setBigDecimal(3, amount);
                    ps.setBigDecimal(4, fromResultingBalance);
                    ps.setTimestamp(5, Timestamp.valueOf(now));
                    ps.setObject(6, toId);
                    ps.setString(7, "Transfer to account " + toAccountId);
                    ps.executeUpdate();
                }

                // 4. Insert TRANSFER_IN record
                try (PreparedStatement ps = conn.prepareStatement(insertTransactionSql)) {
                    ps.setObject(1, toId);
                    ps.setString(2, TransactionType.TRANSFER_IN.name());
                    ps.setBigDecimal(3, amount);
                    ps.setBigDecimal(4, toResultingBalance);
                    ps.setTimestamp(5, Timestamp.valueOf(now));
                    ps.setObject(6, fromId);
                    ps.setString(7, "Transfer from account " + fromAccountId);
                    ps.executeUpdate();
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Transfer failed, rolled back", e);
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Could not obtain connection for transfer", e);
        }
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getString("id"),
                rs.getString("account_id"),
                TransactionType.valueOf(rs.getString("type")),
                rs.getBigDecimal("amount"),
                rs.getBigDecimal("resulting_balance"),
                rs.getTimestamp("timestamp").toLocalDateTime(),
                rs.getString("related_account_id"),
                rs.getString("description")
        );
    }
}