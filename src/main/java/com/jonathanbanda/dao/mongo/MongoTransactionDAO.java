package com.jonathanbanda.dao.mongo;

import com.jonathanbanda.dao.TransactionDAO;
import com.jonathanbanda.model.*;
import com.mongodb.MongoException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoTransactionDAO implements TransactionDAO {
    private final MongoCollection<Document> transactionsCollection;
    private final MongoCollection<Document> accountsCollection;
    private final MongoConnectionManager connectionManager;

    public MongoTransactionDAO(MongoConnectionManager connectionManager) {
        this.transactionsCollection = connectionManager.getDatabase().getCollection("transactions");
        this.accountsCollection = connectionManager.getDatabase().getCollection("accounts");
        this.connectionManager = connectionManager;
    }


    @Override
    public Transaction create(Transaction transaction) {
        Date timestampDate = Date.from(transaction.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());

        Document doc = new Document("account_id", transaction.getAccountId())
                .append("type", transaction.getType().name())
                .append("amount", new Decimal128(transaction.getAmount()))
                .append("resulting_balance", new Decimal128(transaction.getResultingBalance()))
                .append("timestamp", timestampDate)
                .append("related_account_id", transaction.getRelatedAccountId())
                .append("description", transaction.getDescription());

        transactionsCollection.insertOne(doc);

        String generatedId = doc.getObjectId("_id").toHexString();

        return new Transaction(
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

    @Override
    public List<Transaction> findByAccountId(String accountId) {
        List<Transaction> transactions = new ArrayList<>();

        for (Document doc : transactionsCollection.find(Filters.eq("account_id", accountId))){
            transactions.add(mapDocumentToTransaction(doc));
        }

        return transactions;
    }

    @Override
    public List<Transaction> findByAccountIdAndType(String accountId, TransactionType type) {
        List<Transaction> transactions = new ArrayList<>();

        for (Document doc : transactionsCollection.find(
                Filters.and(
                        Filters.eq("account_id", accountId),
                        Filters.eq("type", type.name())
                )
        )){
            transactions.add(mapDocumentToTransaction(doc));
        }

        return transactions;
    }

    @Override
    public List<Transaction> findByAccountIdAndDateRange(String accountId, LocalDateTime start, LocalDateTime end) {
        List<Transaction> transactions = new ArrayList<>();

        Date startDate = Date.from(start.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(end.atZone(ZoneId.systemDefault()).toInstant());

        for (Document doc : transactionsCollection.find(
                Filters.and(
                        Filters.eq("account_id", accountId),
                        Filters.gte("timestamp", startDate),
                        Filters.lte("timestamp", endDate)
                )
        )){
            transactions.add(mapDocumentToTransaction(doc));
        }

        return transactions;
    }

    @Override
    public void executeTransfer(String fromAccountId, String toAccountId, BigDecimal amount,
                                BigDecimal fromResultingBalance, BigDecimal toResultingBalance) {

        try (ClientSession session = connectionManager.getClient().startSession()) {
            try {
                session.startTransaction();

                LocalDateTime now = LocalDateTime.now();
                Date nowDate = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());

                // 1. Update sender's balance
                accountsCollection.updateOne(
                        session,
                        Filters.eq("_id", new ObjectId(fromAccountId)),
                        Updates.set("balance", new Decimal128(fromResultingBalance))
                );

                // 2. Update receiver's balance
                accountsCollection.updateOne(
                        session,
                        Filters.eq("_id", new ObjectId(toAccountId)),
                        Updates.set("balance", new Decimal128(toResultingBalance))
                );

                // 3. Insert TRANSFER_OUT record
                Document transferOut = new Document("account_id", fromAccountId)
                        .append("type", TransactionType.TRANSFER_OUT.name())
                        .append("amount", new Decimal128(amount))
                        .append("resulting_balance", new Decimal128(fromResultingBalance))
                        .append("timestamp", nowDate)
                        .append("related_account_id", toAccountId)
                        .append("description", "Transfer to account " + toAccountId);
                transactionsCollection.insertOne(session, transferOut);

                // 4. Insert TRANSFER_IN record
                Document transferIn = new Document("account_id", toAccountId)
                        .append("type", TransactionType.TRANSFER_IN.name())
                        .append("amount", new Decimal128(amount))
                        .append("resulting_balance", new Decimal128(toResultingBalance))
                        .append("timestamp", nowDate)
                        .append("related_account_id", fromAccountId)
                        .append("description", "Transfer from account " + fromAccountId);
                transactionsCollection.insertOne(session, transferIn);

                session.commitTransaction();

            } catch (MongoException e) {
                session.abortTransaction();
                throw new RuntimeException("Transfer failed, rolled back", e);
            }
        }
    }

    private Transaction mapDocumentToTransaction(Document doc) {
        if (doc == null) {
            return null;
        }

        return new Transaction(
                doc.getObjectId("_id").toHexString(),
                doc.getString("account_id"),
                TransactionType.valueOf(doc.getString("type")),
                doc.get("amount", Decimal128.class).bigDecimalValue(),
                doc.get("resulting_balance", Decimal128.class).bigDecimalValue(),
                doc.getDate("timestamp").toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime(),
                doc.getString("related_account_id"),
                doc.getString("description")
        );
    }
}
