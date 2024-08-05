package de.littleprogrammer.marketplace.database;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import de.littleprogrammer.marketplace.files.DatabaseFile;
import de.littleprogrammer.marketplace.utils.ItemUtils;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Database {
    private String uri = new DatabaseFile().getString("mongoDB.uri");
    private String database = new DatabaseFile().getString("mongoDB.database");

    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;
    private static MongoCollection playersCollection;
    private static MongoCollection transactionsCollection;
    private static MongoCollection itemsCollection;

    public Database() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(uri);
        }

        if (mongoDatabase == null) {
            mongoDatabase = mongoClient.getDatabase(database);
        }

        playersCollection = mongoDatabase.getCollection("players");
        transactionsCollection = mongoDatabase.getCollection("transactions");
        itemsCollection = mongoDatabase.getCollection("items");
    }

    public void addItem(ItemStack item, int price, Player seller) {
        UUID itemID = UUID.randomUUID();
        Document document = new Document("itemID", itemID.toString())
                .append("price", price)
                .append("seller", seller.getUniqueId().toString())
                .append("item", ItemUtils.serializeItem(item))
                .append("date", System.currentTimeMillis());

        itemsCollection.insertOne(document);

        RedisManager redisManager = RedisManager.getInstance();
        redisManager.set("marketplace:items:" + itemID, document.toJson());
        redisManager.lpush("marketplace:items", itemID.toString());
    }

    public Document getItem(UUID uuid) {
        RedisManager redisManager = RedisManager.getInstance();

        String item = redisManager.get("marketplace:items:" + uuid.toString());
        if (item != null) {
            Document document = Document.parse(item);
            return document;
        }

        MongoCursor<Document> cursor = itemsCollection.find(Filters.eq("itemID", uuid.toString())).cursor();
        if (cursor.hasNext()) {
            Document document = Document.parse(cursor.next().toJson());
            redisManager.set("marketplace:items:" + uuid.toString(), document.toString());
            return document;
        }

        return null;
    }

    public List<Document> getAllItems() {
        List<String> cachedItemIDs = RedisManager.getInstance().lrange("marketplace:items", 0, -1);
        List<Document> items = new ArrayList<>();

        if (cachedItemIDs != null) {
            System.out.println("Getting from cache :D");
            for (String itemID : cachedItemIDs) {
                String itemJSON = RedisManager.getInstance().get("marketplace:items:" + itemID);
                if (itemJSON != null) {
                    items.add(Document.parse(itemJSON));
                }
            }
            return items;
        } else {
            System.out.println("Getting from mongo D:");
            BsonArray bsonArray = new BsonArray();
            MongoCursor<Document> cursor = itemsCollection.find().iterator();
            try {
                while (cursor.hasNext()) {
                    items.add(cursor.next());
                    bsonArray.add(BsonDocument.parse(cursor.next().toJson()));
                }
            } finally {
                cursor.close();
            }

            if (!items.isEmpty()) {
                RedisManager.getInstance().set("marketplace:items", bsonArray.toString());
            }

            return items;
        }
    }

    public void removeItem(UUID uuid) {
        itemsCollection.deleteOne(new Document("itemID", uuid.toString()));

        RedisManager.getInstance().lrem("marketplace:items", uuid.toString());
        RedisManager.getInstance().del("marketplace:items:" + uuid.toString());
    }

    public void addTransaction(DatabaseTransaction transaction) throws Exception {
        RedisManager redisManager = RedisManager.getInstance();
        UUID transactionID = UUID.randomUUID();
        Document document = new Document("transactionID", transactionID.toString())
                .append("price", transaction.getPrice())
                .append("buyer", transaction.getBuyer().toString())
                .append("seller", transaction.getSeller().toString())
                .append("date", transaction.getDate())
                .append("item", ItemUtils.serializeItem(transaction.getItem()))
                .append("blackMarket", transaction.isBlackMarket());

        try (ClientSession session = mongoClient.startSession()) {
            session.startTransaction();
            transactionsCollection.insertOne(session, document);

            updatePlayerHistory(session, transaction.getBuyer(), transactionID);
            updatePlayerHistory(session, transaction.getSeller(), transactionID);

            session.commitTransaction();

            redisManager.set("marketplace:transactions:" + transactionID, document.toJson());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error while adding transaction");
        }
    }

    public DatabaseTransaction getTransaction(UUID transactionID) {
        RedisManager redisManager = RedisManager.getInstance();
        String transaction = redisManager.get("marketplace:transactions:" + transactionID.toString());
        if (transaction != null) {
            Document transactionDoc = Document.parse(transaction);
            return new DatabaseTransaction(transactionDoc.getInteger("price"), UUID.fromString(transactionDoc.getString("buyer")), UUID.fromString(transactionDoc.getString("seller")), ItemUtils.deserializeItem(transactionDoc.getString("item")), transactionDoc.getDate("date"), transactionDoc.getBoolean("blackMarket"));
        } else {
            MongoCursor<Document> cursor = transactionsCollection.find(Filters.eq("transactionID", transactionID)).cursor();
            if (cursor.hasNext()) {
                Document transactionDoc = cursor.next();

                redisManager.set("marketplace:transactions:" + transactionID, transactionDoc.toJson());
                return new DatabaseTransaction(transactionDoc.getInteger("price"), UUID.fromString(transactionDoc.getString("buyer")), UUID.fromString(transactionDoc.getString("seller")), ItemUtils.deserializeItem(transactionDoc.getString("item")), transactionDoc.getDate("date"), transactionDoc.getBoolean("blackMarket"));
            }
            return null;
        }
    }

    public List<DatabaseTransaction> getPlayerTransactions(UUID playerUUID) {
        List<DatabaseTransaction> transactions = new ArrayList<>();

        DatabasePlayer databasePlayer = getPlayerByUUID(playerUUID);
        for (String transaction : databasePlayer.getHistory()) {
            DatabaseTransaction databaseTransaction = getTransaction(UUID.fromString(transaction));
            if (databaseTransaction != null) {
                transactions.add(databaseTransaction);
            }
        }
        return transactions;
    }

    public DatabasePlayer getPlayerByUUID(UUID uuid) {
        RedisManager redisManager = RedisManager.getInstance();
        String player = redisManager.get("marketplace:players:" + uuid.toString());
        if (player != null) {
            Document playerDoc = Document.parse(player);
            return new DatabasePlayer(uuid, playerDoc.getList("history", String.class));
        } else {
            MongoCursor<Document> cursor = playersCollection.find(Filters.eq("playerID", uuid.toString())).cursor();
            if (cursor.hasNext()) {
                Document playerDoc = cursor.next();

                redisManager.set("marketplace:players:" + playerDoc.getString("playerID"), playerDoc.toJson());
                return new DatabasePlayer(uuid, playerDoc.getList("history", String.class));
            }
            return null;
        }
    }

    public void addPlayer(ClientSession session, DatabasePlayer databasePlayer) throws Exception {
        Document playerDoc = new Document("playerID", databasePlayer.getUuid().toString())
                .append("history", databasePlayer.getHistory());

        try {
            playersCollection.insertOne(session, playerDoc);

            RedisManager redisManager = RedisManager.getInstance();
            redisManager.set("marketplace:players:" + databasePlayer.getUuid().toString(), playerDoc.toJson());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error while adding player");
        }
    }

    private void updatePlayerHistory(ClientSession session, UUID playerUUID, UUID transactionID) throws Exception {
        DatabasePlayer databasePlayer = getPlayerByUUID(playerUUID);
        if (databasePlayer != null) {
            List<String> transactions = databasePlayer.getHistory() != null ? databasePlayer.getHistory() : new ArrayList<>();
            transactions.add(transactionID.toString());

            Bson updates = Updates.set("history", transactions);
            try {
                playersCollection.updateOne(session, Filters.eq("playerID", databasePlayer.getUuid().toString()), updates);
                Document playerDoc = new Document("playerID", databasePlayer.getUuid().toString())
                        .append("history", transactions);

                RedisManager.getInstance().set("marketplace:players:" + databasePlayer.getUuid().toString(), playerDoc.toJson());
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("Error while updating player history");
            }
        } else {
            List<String> transactions = new ArrayList<>();
            transactions.add(transactionID.toString());

            try {
                addPlayer(session, new DatabasePlayer(playerUUID, transactions));
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("Error while updating player history");
            }
        }
    }

    private List<Document> parseJson(String json) {
        List<Document> documents = new ArrayList<>();
        BsonArray bsonArray = BsonArray.parse(json);
        for (BsonValue bsonValue : bsonArray) {
            documents.add(Document.parse(bsonValue.asDocument().toJson()));
        }
        return documents;
    }
}
