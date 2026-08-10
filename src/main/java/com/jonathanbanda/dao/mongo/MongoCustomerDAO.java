package com.jonathanbanda.dao.mongo;

import com.jonathanbanda.dao.CustomerDAO;
import com.jonathanbanda.model.Customer;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

public class MongoCustomerDAO implements CustomerDAO {

    private final MongoCollection<Document> collection;

    public MongoCustomerDAO(MongoConnectionManager connectionManager) {
        this.collection = connectionManager.getDatabase().getCollection("customers");
    }

    @Override
    public Customer createCustomer(Customer customer) {
        Date createdAtDate = Date.from(customer.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());

        Document doc = new Document("first_name", customer.getFirstName())
                .append("last_name", customer.getLastName())
                .append("username", customer.getUsername())
                .append("hashed_password", customer.getHashedPassword())
                .append("created_at", createdAtDate);

        collection.insertOne(doc);

        String generatedId = doc.getObjectId("_id").toHexString();

        return new Customer(
                generatedId,
                customer.getFirstName(),
                customer.getLastName(),
                customer.getUsername(),
                customer.getHashedPassword(),
                customer.getCreatedAt()
        );
    }

    @Override
    public Optional<Customer> findById(String id) {
        Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
        return Optional.ofNullable(mapDocumentToCustomer(doc));
    }

    @Override
    public Optional<Customer> findByUsername(String username) {
        Document doc = collection.find(Filters.eq("username", username)).first();
        return Optional.ofNullable(mapDocumentToCustomer(doc));
    }

    @Override
    public void updateCustomer(Customer customer) {
        collection.updateOne(
                Filters.eq("_id", new ObjectId(customer.getId())),
                Updates.combine(
                        Updates.set("first_name", customer.getFirstName()),
                        Updates.set("last_name", customer.getLastName()),
                        Updates.set("username", customer.getUsername()),
                        Updates.set("hashed_password", customer.getHashedPassword())
                )
        );
    }

    @Override
    public boolean existsById(String id) {
        return collection.countDocuments(Filters.eq("_id", new ObjectId(id))) > 0;
    }

    @Override
    public boolean existsByUsername(String username) {
        return collection.countDocuments(Filters.eq("username", username)) > 0;
    }

    private Customer mapDocumentToCustomer(Document doc) {
        if (doc == null) {
            return null;
        }

        return new Customer(
                doc.getObjectId("_id").toHexString(),
                doc.getString("first_name"),
                doc.getString("last_name"),
                doc.getString("username"),
                doc.getString("hashed_password"),
                doc.getDate("created_at").toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
        );
    }
}
