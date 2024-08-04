package de.littleprogrammer.marketplace.utils;

import de.littleprogrammer.marketplace.Main;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ItemUtils {

    public static String serializeItem(ItemStack item) {
        try
        {
            final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            final BukkitObjectOutputStream objectOutputStream = new BukkitObjectOutputStream(arrayOutputStream);
            objectOutputStream.writeObject(item);
            return Base64Coder.encodeLines(arrayOutputStream.toByteArray());
        }
        catch (final Exception exception)
        {
            throw new RuntimeException("Failed to convert item into base64", exception);
        }
    }

    public static ItemStack deserializeItem(String base64) {
        try
        {
            final ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            final BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(arrayInputStream);
            return (ItemStack) objectInputStream.readObject();
        }
        catch (final Exception exception)
        {
            throw new RuntimeException("Failed to convert bas64 to a valid itemStack", exception);
        }
    }

    public static String getPdc(ItemStack itemStack) {
        if (itemStack != null && itemStack.hasItemMeta()) {
            NamespacedKey key = new NamespacedKey(Main.getInstance(), "localizedName");

            PersistentDataContainer pdc = itemStack.getItemMeta().getPersistentDataContainer();
            return (pdc.has(key, PersistentDataType.STRING) ? pdc.get(key, PersistentDataType.STRING) : "");
        }
        return "";
    }

    public static ItemStack removePdc(ItemStack itemStack) {
        if (itemStack != null && itemStack.hasItemMeta()) {
            NamespacedKey key = new NamespacedKey(Main.getInstance(), "localizedName");

            ItemMeta meta = itemStack.getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            if (pdc.has(key, PersistentDataType.STRING)) {
                pdc.remove(key);
                itemStack.setItemMeta(meta);
                return itemStack;
            }
        }
        return itemStack;
    }

    public static ItemStack setPdc(ItemStack itemStack, String name) {
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "localizedName");
        ItemMeta meta = itemStack.getItemMeta();

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.STRING, name);
        itemStack.setItemMeta(meta);
        return itemStack;
    }


}
