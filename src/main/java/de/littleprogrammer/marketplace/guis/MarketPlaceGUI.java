package de.littleprogrammer.marketplace.guis;

import de.littleprogrammer.marketplace.files.LanguageFile;
import de.littleprogrammer.marketplace.utils.ItemUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MarketPlaceGUI {
    private Player player;
    private List<Document> items;
    private boolean blackMarket;
    private LanguageFile languageFile = new LanguageFile();

    public MarketPlaceGUI(Player player, List<Document> items, boolean blackMarket) {
        this.player = player;
        this.items = items;
        this.blackMarket = blackMarket;

        openGUI();
    }

    private void openGUI() {
        Inventory inv = Bukkit.createInventory(null, 54, (blackMarket ? languageFile.getString("guis.marketplace.blackMarket") : languageFile.getString("guis.marketplace.title")));

        for (Document item : items) {
            /*int num = new Random().nextInt(2);
            boolean show = num > 0;
            System.out.println(num + " " + show);*/
            if (blackMarket && new Random().nextBoolean()) {
                //Skip this item if black market is on and 50/50 chance is right
                continue;
            }

            // Twice as much whn blackmarket
            int price = (blackMarket ? item.getInteger("price") / 2 : item.getInteger("price"));
            UUID seller = UUID.fromString(item.getString("seller"));
            ItemStack stack = ItemUtils.deserializeItem(item.getString("item"));

            ItemMeta meta = stack.getItemMeta();
            List<String> lore = new ArrayList<>();
            if (meta.getLore() != null && !meta.getLore().isEmpty()) {
                lore.addAll(meta.getLore());
            }
            lore.add(languageFile.getInsertedString("guis.marketplace.lore", "%price%", price));
            meta.setLore(lore);
            stack.setItemMeta(meta);

            ItemUtils.setPdc(stack, "marketplaceItem:" + price + ":" + seller.toString() + ":" + item.getString("itemID") + ":" + (blackMarket ? "blackmarket" : ""));
            inv.addItem(stack);
        }

        player.openInventory(inv);
    }

}
