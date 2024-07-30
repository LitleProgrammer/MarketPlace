package de.littleprogrammer.marketplace.listeners;

import de.littleprogrammer.marketplace.database.Database;
import de.littleprogrammer.marketplace.files.ConfigFile;
import de.littleprogrammer.marketplace.files.LanguageFile;
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

            String[] cutName = localizedName.split(":");
            int price = Integer.parseInt(cutName[1]);
            UUID sellerUUID = UUID.fromString(cutName[2]);
            UUID itemID = UUID.fromString(cutName[3]);
            boolean blackMarket = localizedName.contains("blackmarket");

            if (sellerUUID.equals(player.getUniqueId())) {
                player.sendMessage(languageFile.getString("messages.cannotBuyOwn"));
                return;
            }

            Player seller = Bukkit.getPlayer(sellerUUID);
            if (seller != null) {
                if (new ConfigFile().getBoolean("useVault")) {
                    VaultHandler.addBalance(seller, (blackMarket ? price * 4: price));
                }
                if (seller.isOnline()) {
                    seller.sendMessage(languageFile.getInsertedString("messages.sellerNotification", "%player%", player.getName(), "%price%", (blackMarket ? price * 4: price)));
                }
            }

            if (new ConfigFile().getBoolean("useVault")) {
                VaultHandler.removeBalance(player, price);
            }
            new Database().removeItem(itemID);

            player.sendMessage(languageFile.getInsertedString("messages.buyerNotification", "%player%", Bukkit.getOfflinePlayer(sellerUUID).getName(), "%price%", price));
            player.closeInventory();
            player.getInventory().addItem(reforamtItem(event.getCurrentItem()));
            System.out.println("Click: " + localizedName);
        }
        if (localizedName.contains("fillItem")) {
            event.setCancelled(true);
            return;
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
        return item;
    }
}
