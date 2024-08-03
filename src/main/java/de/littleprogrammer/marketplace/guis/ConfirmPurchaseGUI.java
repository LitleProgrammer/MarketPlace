package de.littleprogrammer.marketplace.guis;

import de.littleprogrammer.marketplace.files.LanguageFile;
import de.littleprogrammer.marketplace.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ConfirmPurchaseGUI {

    public ConfirmPurchaseGUI(Player player, ItemStack item) {
        LanguageFile languageFile = new LanguageFile();

        Inventory inv = Bukkit.createInventory(null, 27, languageFile.getString("guis.confirmation.title"));
        for (int i = 0; i < 27; i++) {
            if (i % 9 < 4 && i % 9 > 0) {
                //Green
                ItemStack greenPane = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                ItemMeta greenMeta = greenPane.getItemMeta();
                greenMeta.setDisplayName(languageFile.getString("guis.confirmation.buy"));
                greenPane.setItemMeta(greenMeta);
                ItemUtils.setPdc(greenPane, "acceptPurchase");

                inv.setItem(i, greenPane);
            } else if (i == 13) {
                inv.setItem(i, item);
            } else if (i % 9 < 7 && i % 9 > 3) {
                //Gray
                ItemStack grayPane = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                ItemMeta grayMeta = grayPane.getItemMeta();
                grayMeta.setDisplayName(" ");
                grayPane.setItemMeta(grayMeta);
                ItemUtils.setPdc(grayPane, "fillItem");

                inv.setItem(i, grayPane);
            } else if (i % 9 > 6) {
                //Red
                ItemStack redPane = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                ItemMeta redMeta = redPane.getItemMeta();
                redMeta.setDisplayName(languageFile.getString("guis.confirmation.buy"));
                redPane.setItemMeta(redMeta);
                ItemUtils.setPdc(redPane, "leavePurchase");

                inv.setItem(i, redPane);
            }
        }

        player.openInventory(inv);
    }

}
