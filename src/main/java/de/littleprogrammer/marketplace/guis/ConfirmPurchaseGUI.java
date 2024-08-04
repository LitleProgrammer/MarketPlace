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
            if (i % 9 > 5) {
                //Red
                ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta redMeta = redPane.getItemMeta();
                redMeta.setDisplayName(languageFile.getString("guis.confirmation.leave"));
                redPane.setItemMeta(redMeta);
                ItemUtils.setPdc(redPane, "leavePurchase");

                inv.setItem(i, redPane);
                continue;
            }

            if (i == 13) {
                inv.setItem(i, item);
                continue;
            }

            if (i % 9 > 2) {
                //Gray
                ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta grayMeta = grayPane.getItemMeta();
                grayMeta.setDisplayName(" ");
                grayPane.setItemMeta(grayMeta);
                ItemUtils.setPdc(grayPane, "fillItem");

                inv.setItem(i, grayPane);
                continue;
            }

            if (i % 9 < 3) {
                //Green
                ItemStack greenPane = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                ItemMeta greenMeta = greenPane.getItemMeta();
                greenMeta.setDisplayName(languageFile.getString("guis.confirmation.buy"));
                greenPane.setItemMeta(greenMeta);
                ItemUtils.setPdc(greenPane, "acceptPurchase");

                inv.setItem(i, greenPane);
            }
        }

        player.openInventory(inv);
    }

}
