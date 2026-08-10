package com.jonathanbanda.dao.postgres;

import com.jonathanbanda.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgresConnectionManager {

    private final AppConfig config;

    public PostgresConnectionManager(AppConfig config) {
        this.config = config;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                config.getPostgresUrl(),
                config.getPostgresUsername(),
                config.getPostgresPassword()
        );
    }
}