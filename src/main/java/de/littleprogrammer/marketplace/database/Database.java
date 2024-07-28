package de.littleprogrammer.marketplace.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.littleprogrammer.marketplace.files.DatabaseFile;
import de.littleprogrammer.marketplace.utils.ItemUtils;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Database {
    private String uri = new DatabaseFile().getString("mongoDB.uri");
    private String database = new DatabaseFile().getString("mongoDB.database");

    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;
    private static MongoCollection players;
    private static MongoCollection transactions;
    private static MongoCollection items;

    public Database() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(uri);
        }

        if (mongoDatabase == null) {
            mongoDatabase = mongoClient.getDatabase(database);
        }

        players = mongoDatabase.getCollection("players");
        transactions = mongoDatabase.getCollection("transactions");
        items = mongoDatabase.getCollection("items");
    }

    public void addItem(ItemStack item, int price, Player seller) {
        Document document = new Document("itemID", UUID.randomUUID())
                .append("price", price)
                .append("seller", seller.getUniqueId())
                .append("item", ItemUtils.serializeItem(item))
                .append("date", System.currentTimeMillis());

        items.insertOne(document);
    }

}
