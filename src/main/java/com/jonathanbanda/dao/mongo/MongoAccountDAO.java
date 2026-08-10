package com.jonathanbanda.dao.mongo;

import com.jonathanbanda.dao.AccountDAO;
import com.jonathanbanda.model.Account;
import com.jonathanbanda.model.AccountStatus;
import com.jonathanbanda.model.AccountType;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class MongoAccountDAO implements AccountDAO {
    private final MongoCollection<Document> collection;

    public MongoAccountDAO(MongoConnectionManager connectionManager) {
        this.collection = connectionManager.getDatabase().getCollection("accounts");
    }

    @Override
    public Account createAccount(Account account) {
        Date createdAtDate = Date.from(account.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());

        Document doc = new Document("customer_id", account.getCustomerId())
                .append("type", account.getType().name())
                .append("status", account.getStatus().name())
                .append("balance", new Decimal128(account.getBalance()))
                .append("created_at", createdAtDate);

        collection.insertOne(doc);

        String generatedId = doc.getObjectId("_id").toHexString();

        return new Account(
                generatedId,
                account.getCustomerId(),
                account.getType(),
                account.getBalance(),
                account.getCreatedAt(),
                account.getStatus()
        );
    }

    @Override
    public Optional<Account> findAccountById(String id) {
        Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
        return Optional.ofNullable(mapDocumentToAccount(doc));
    }

    @Override
    public List<Account> findAllAccounts(String customerId) {
        List<Account> accounts = new ArrayList<>();

        for (Document doc : collection.find(Filters.eq("customer_id", customerId))) {
            accounts.add(mapDocumentToAccount(doc));
        }

        return accounts;
    }

    @Override
    public void updateAccountBalance(String accountId, BigDecimal balance) {
        collection.updateOne(
                Filters.eq("_id", new ObjectId(accountId)),
                Updates.set("balance", new Decimal128(balance))
        );
    }

    @Override
    public void updateStatus(String accountId, AccountStatus status) {
        collection.updateOne(
                Filters.eq("_id", new ObjectId(accountId)),
                Updates.set("status", status.name())
        );
    }

    private Account mapDocumentToAccount(Document doc) {
        if (doc == null) {
            return null;
        }

        return new Account(
                doc.getObjectId("_id").toHexString(),
                doc.getString("customer_id"),
                AccountType.valueOf(doc.getString("type")),
                doc.get("balance", Decimal128.class).bigDecimalValue(),
                doc.getDate("created_at").toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime(),
                AccountStatus.valueOf(doc.getString("status"))
        );
    }
}
