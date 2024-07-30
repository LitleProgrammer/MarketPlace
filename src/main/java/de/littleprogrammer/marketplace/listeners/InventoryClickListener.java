package de.littleprogrammer.marketplace.listeners;

import de.littleprogrammer.marketplace.utils.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;
        if (!clicked.hasItemMeta()) {return;}

        String localizedName = ItemUtils.getPdc(clicked);
        if (localizedName.contains("marketplaceItem")) {
            event.setCancelled(true);
            System.out.println("Click: " + localizedName);
        }
        if (localizedName.contains("fillItem")) {
            event.setCancelled(true);
            return;
        }

    }
}
