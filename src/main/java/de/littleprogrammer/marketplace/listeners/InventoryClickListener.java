package de.littleprogrammer.marketplace.listeners;

import de.littleprogrammer.marketplace.database.Database;
import de.littleprogrammer.marketplace.database.DatabaseTransaction;
import de.littleprogrammer.marketplace.files.ConfigFile;
import de.littleprogrammer.marketplace.files.LanguageFile;
import de.littleprogrammer.marketplace.guis.ConfirmPurchaseGUI;
import de.littleprogrammer.marketplace.utils.DiscordWebhookUtils;
import de.littleprogrammer.marketplace.utils.ItemUtils;
import de.littleprogrammer.marketplace.vault.VaultHandler;
import org.bson.Document;
import org.bukkit.Bukkit;
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

        if (clicked == null || !clicked.hasItemMeta()) return;

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
            processPurchase(player, event.getInventory().getItem(13), languageFile);
        }

        if (localizedName.contains("leavePurchase")) {
            event.setCancelled(true);
            player.closeInventory();
        }
    }

    private void processPurchase(Player player, ItemStack soldItem, LanguageFile languageFile) {
        if (soldItem == null || !soldItem.hasItemMeta()) {
            player.sendMessage(languageFile.getString("messages.couldntFind"));
            return;
        }

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

        try {
            Database database = new Database();
            Document item = database.getItem(itemID);
            if (item == null) {
                player.sendMessage(languageFile.getString("messages.couldntFind"));
                return;
            }

            DatabaseTransaction transaction = new DatabaseTransaction(price, player.getUniqueId(), sellerUUID, soldItem, new Date(), blackMarket);

            database.removeItem(itemID);
            database.addTransaction(transaction);

            processVaultTransaction(player, sellerUUID, price, blackMarket, languageFile);

            player.sendMessage(languageFile.getInsertedString("messages.buyerNotification", "%player%", Bukkit.getOfflinePlayer(sellerUUID).getName(), "%price%", price));
            player.closeInventory();
            player.getInventory().addItem(reforamtItem(soldItem));
            notifyDiscord(transaction);

        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(languageFile.getString("messages.transactionError"));
        }
    }




    private void processVaultTransaction(Player player, UUID sellerUUID, int price, boolean blackMarket, LanguageFile languageFile) {
        if (new ConfigFile().getBoolean("useVault")) {
            if (VaultHandler.hasBalance(player, price)) {
                VaultHandler.removeBalance(player, price);

                Player seller = Bukkit.getPlayer(sellerUUID);
                if (seller != null) {
                    VaultHandler.addBalance(seller, (blackMarket ? price * 4 : price));
                    if (seller.isOnline()) {
                        seller.sendMessage(languageFile.getInsertedString("messages.sellerNotification", "%player%", player.getName(), "%price%", (blackMarket ? price * 4 : price)));
                    }
                }
            } else {
                player.sendMessage(languageFile.getString("messages.insufficientFunds"));
            }
        }
    }


    private ItemStack reforamtItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore != null && !lore.isEmpty()) {
            lore.remove(lore.size() - 1);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
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
                                .addField(languageFile.getString("discord.fields.priceFieldTitle"), "$" + databaseTransaction.getPrice(), true)
                                .setFooter("Marketplace plugin ● " + databaseTransaction.getDate().toString(), ""))
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
