package de.littleprogrammer.marketplace.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InvUtils {

    public static void switchInv(Inventory inv, Player player) {
        Inventory currInv = player.getOpenInventory().getTopInventory();
        currInv.clear();

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            currInv.setItem(i, item);
        }
    }
}
