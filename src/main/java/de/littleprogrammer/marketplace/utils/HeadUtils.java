package de.littleprogrammer.marketplace.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class HeadUtils {

    public ItemStack makeHead(String code) {

        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();

        PlayerProfile profile = Bukkit.getServer().createPlayerProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();
        try {
            textures.setSkin(new URL("https://textures.minecraft.net/texture/" + code));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        profile.setTextures(textures);
        headMeta.setOwnerProfile(profile);

        head.setItemMeta(headMeta);

        return head;

    }

}
