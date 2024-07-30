package de.littleprogrammer.marketplace.database;

import com.mongodb.client.*;
import de.littleprogrammer.marketplace.files.DatabaseFile;
import de.littleprogrammer.marketplace.utils.ItemUtils;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
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

    public void getItem(UUID uuid) {

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




    private List<Document> parseJson(String json) {
        List<Document> documents = new ArrayList<>();
        BsonArray bsonArray = BsonArray.parse(json);
        for (BsonValue bsonValue : bsonArray) {
            documents.add(Document.parse(bsonValue.asDocument().toJson()));
        }
        return documents;
    }
}
