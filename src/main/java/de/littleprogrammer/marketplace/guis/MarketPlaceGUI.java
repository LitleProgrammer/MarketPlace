package de.littleprogrammer.marketplace.guis;

import de.littleprogrammer.marketplace.files.LanguageFile;
import de.littleprogrammer.marketplace.utils.ItemUtils;
import de.littleprogrammer.marketplace.utils.PagedInv;
import org.bson.Document;
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
        List<ItemStack> itemStacks = new ArrayList<>();

        for (Document item : items) {
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
            itemStacks.add(stack);
        }

        Inventory pagedInv = new PagedInv().buildPagedInv(0, itemStacks, new int[]{9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 ,21 ,22 ,23 ,24 ,25 ,26 ,27 ,28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44}, 4, (blackMarket ? languageFile.getString("guis.marketplace.blackMarket") : languageFile.getString("guis.marketplace.title")), true, player.getUniqueId(),null);

        player.openInventory(pagedInv);
    }

}
