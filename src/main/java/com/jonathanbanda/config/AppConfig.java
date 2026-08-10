package com.jonathanbanda.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private final Properties properties = new Properties();

    public AppConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found on classpath");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public DatabaseType getDatabaseType() {
        String type = properties.getProperty("db.type");
        if (type == null) {
            throw new RuntimeException("db.type not set in config.properties");
        }
        return DatabaseType.valueOf(type.trim().toUpperCase());
    }

    public String getPostgresUrl() {
        return properties.getProperty("postgres.url");
    }

    public String getPostgresUsername() {
        return properties.getProperty("postgres.username");
    }

    public String getPostgresPassword() {
        return properties.getProperty("postgres.password");
    }

    public String getMongoUri() {
        return properties.getProperty("mongo.uri");
    }

    public String getMongoDatabase() {
        return properties.getProperty("mongo.database");
    }
}