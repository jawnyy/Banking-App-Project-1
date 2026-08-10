package com.jonathanbanda.config;

import com.jonathanbanda.dao.AccountDAO;
import com.jonathanbanda.dao.CustomerDAO;
import com.jonathanbanda.dao.TransactionDAO;
import com.jonathanbanda.dao.mongo.MongoAccountDAO;
import com.jonathanbanda.dao.mongo.MongoConnectionManager;
import com.jonathanbanda.dao.mongo.MongoCustomerDAO;
import com.jonathanbanda.dao.mongo.MongoTransactionDAO;
import com.jonathanbanda.dao.postgres.PostgresAccountDAO;
import com.jonathanbanda.dao.postgres.PostgresConnectionManager;
import com.jonathanbanda.dao.postgres.PostgresCustomerDAO;
import com.jonathanbanda.dao.postgres.PostgresTransactionDAO;

public class DAOFactory {
    private final AppConfig config;

    public DAOFactory(AppConfig config) {
        this.config = config;
    }

    public CustomerDAO getCustomerDAO() {
        return switch (config.getDatabaseType()) {
            case POSTGRES -> new PostgresCustomerDAO(new PostgresConnectionManager(config));
            case MONGO -> new MongoCustomerDAO(new MongoConnectionManager(config));
        };
    }

    public AccountDAO getAccountDAO() {
        return switch (config.getDatabaseType()) {
            case POSTGRES -> new PostgresAccountDAO(new PostgresConnectionManager(config));
            case MONGO -> new MongoAccountDAO(new MongoConnectionManager(config));
        };
    }

    public TransactionDAO getTransactionDAO() {
        return switch (config.getDatabaseType()) {
            case POSTGRES -> new PostgresTransactionDAO(new PostgresConnectionManager(config));
            case MONGO -> new MongoTransactionDAO(new MongoConnectionManager(config));
        };
    }
}