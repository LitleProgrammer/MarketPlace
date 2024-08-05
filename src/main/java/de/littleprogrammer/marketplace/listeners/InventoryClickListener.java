package de.littleprogrammer.marketplace.listeners;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.TransactionBody;
import de.littleprogrammer.marketplace.database.Database;
import de.littleprogrammer.marketplace.database.DatabaseTransaction;
import de.littleprogrammer.marketplace.files.ConfigFile;
import de.littleprogrammer.marketplace.files.DatabaseFile;
import de.littleprogrammer.marketplace.files.LanguageFile;
import de.littleprogrammer.marketplace.guis.ConfirmPurchaseGUI;
import de.littleprogrammer.marketplace.guis.MarketPlaceGUI;
import de.littleprogrammer.marketplace.utils.DiscordWebhookUtils;
import de.littleprogrammer.marketplace.utils.ItemUtils;
import de.littleprogrammer.marketplace.vault.VaultHandler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.*;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class InventoryClickListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        LanguageFile languageFile = new LanguageFile();

        if (clicked == null) return;
        if (!clicked.hasItemMeta()) {return;}

        String localizedName = ItemUtils.getPdc(clicked);
        if (localizedName.contains("marketplaceItem")) {
            event.setCancelled(true);
            new ConfirmPurchaseGUI(player, clicked);
            return;
        }

        if (localizedName.contains("fillItem")) {
            event.setCancelled(true);
            return;
        }

        if (localizedName.contains("acceptPurchase")) {
            event.setCancelled(true);

            ItemStack soldItem = event.getInventory().getItem(13);
            String soldItemLocalizedName = ItemUtils.getPdc(soldItem);


            String[] cutName = soldItemLocalizedName.split(":");
            int price = Integer.parseInt(cutName[1]);
            UUID sellerUUID = UUID.fromString(cutName[2]);
            UUID itemID = UUID.fromString(cutName[3]);
            boolean blackMarket = soldItemLocalizedName.contains("blackmarket");

            if (sellerUUID.equals(player.getUniqueId())) {
                player.sendMessage(languageFile.getString("messages.cannotBuyOwn"));
                return;
            }

            Database database = new Database();
            if (database.getItem(itemID) == null) {
                player.sendMessage(languageFile.getString("messages.couldntFind"));
                return;
            }

            DatabaseTransaction transaction = new DatabaseTransaction(price, player.getUniqueId(), sellerUUID, soldItem, new Date(), blackMarket);

            try (MongoClient mongoClient = MongoClients.create(new DatabaseFile().getString("mongoDB.uri"))) {
                ClientSession session = mongoClient.startSession();
                TransactionBody<String> transactionBody = () -> {
                    database.removeItem(itemID);
                    try {
                        database.addTransaction(transaction);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    return "Done transaction";
                };

                try {
                    session.withTransaction(transactionBody);

                    if (new ConfigFile().getBoolean("useVault") && VaultHandler.hasBalance(player, price)) {
                        VaultHandler.removeBalance(player, price);

                        Player seller = Bukkit.getPlayer(sellerUUID);
                        if (seller != null) {
                            VaultHandler.addBalance(seller, (blackMarket ? price * 4 : price));

                            if (seller.isOnline()) {
                                seller.sendMessage(languageFile.getInsertedString("messages.sellerNotification", "%player%", player.getName(), "%price%", (blackMarket ? price * 4 : price)));
                            }
                        }
                    }

                    player.sendMessage(languageFile.getInsertedString("messages.buyerNotification", "%player%", Bukkit.getOfflinePlayer(sellerUUID).getName(), "%price%", price));
                    player.closeInventory();
                    player.getInventory().addItem(reforamtItem(soldItem));

                    notifyDiscord(transaction);
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(languageFile.getString("messages.transactionError"));
                } finally {
                    session.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(languageFile.getString("messages.transactionError"));
            }
        }

        if (localizedName.contains("leavePurchase")) {
            event.setCancelled(true);
            player.closeInventory();
        }


    }

    private ItemStack reforamtItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore != null) {
            lore.remove(lore.size() - 1);
        }
        meta.setLore(lore);
        item.setItemMeta(meta);

        return ItemUtils.removePdc(item);
    }

    private void notifyDiscord(DatabaseTransaction databaseTransaction) {
        ConfigFile configFile = new ConfigFile();
        LanguageFile languageFile = new LanguageFile();

        if (configFile.getString("discord.webhookURL") != null && !configFile.getString("discord.webhookURL").isEmpty()) {
            String url = configFile.getString("discord.webhookURL");
            String iconURL = configFile.getString("discord.iconURL");
            String username = configFile.getString("discord.username");
            String colorHex = configFile.getString("discord.color");

            System.out.println("Seinding webhook to: " + url + "    " + iconURL + "    " + username + "    " + colorHex);

            try {
                new DiscordWebhookUtils(url)
                        .setAvatarUrl(iconURL)
                        .setUsername(username)
                        .setTts(false)
                        .addEmbed(new DiscordWebhookUtils.EmbedObject()
                                .setTitle(languageFile.getString("discord.title"))
                                .setDescription(languageFile.getString("discord.description"))
                                .setColor(Color.decode(colorHex))
                                .addField(languageFile.getString("discord.fields.buyerFieldTitle"), Bukkit.getOfflinePlayer(databaseTransaction.getBuyer()).getName(), true)
                                .addField(languageFile.getString("discord.fields.sellerFieldTitle"), Bukkit.getOfflinePlayer(databaseTransaction.getSeller()).getName(), true)
                                .addField(languageFile.getString("discord.fields.itemFieldTitle"), databaseTransaction.getItem().getType().toString(), true)
                                .addField(languageFile.getString("discord.fields.priceFieldTitle"), "$" + String.valueOf(databaseTransaction.getPrice()), true)
                                .setFooter("Marketplace plugin ● " + databaseTransaction.getDate().toString(), ""))
                        .execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
