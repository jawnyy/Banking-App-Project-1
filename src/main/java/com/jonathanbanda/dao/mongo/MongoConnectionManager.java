package com.jonathanbanda.dao.mongo;

import com.jonathanbanda.config.AppConfig;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoConnectionManager {

    private final MongoClient mongoClient;
    private final String databaseName;

    public MongoConnectionManager(AppConfig config) {
        this.mongoClient = MongoClients.create(config.getMongoUri());
        this.databaseName = config.getMongoDatabase();
        verifyConnection();
    }

    public MongoClient getClient() {
        return mongoClient;
    }

    public MongoDatabase getDatabase() {
        return mongoClient.getDatabase(databaseName);
    }

    private void verifyConnection() {
        try {
            MongoDatabase adminDatabase = mongoClient.getDatabase("admin");
            adminDatabase.runCommand(new Document("ping", 1));
            System.out.println("Successfully connected to MongoDB.");
        } catch (MongoException e) {
            throw new RuntimeException("Failed to connect to MongoDB: " + e.getMessage(), e);
        }
    }
}