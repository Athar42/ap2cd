package me.Athar42.ap2cd.utils;

import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class TypeChecker {

    public static boolean isMusicDisc(ItemStack item) {
        return MaterialTags.MUSIC_DISCS.isTagged(item.getType());
    }

    public static boolean isGoatHornPlayer(Player p) {
        return p.getInventory().getItemInMainHand().getType().equals(Material.GOAT_HORN) || p.getInventory().getItemInOffHand().getType().equals(Material.GOAT_HORN);
    }

    public static boolean isHeadPlayer(Player p) {
        return p.getInventory().getItemInMainHand().getType().equals(Material.PLAYER_HEAD) || p.getInventory().getItemInOffHand().getType().equals(Material.PLAYER_HEAD);
    }

    public static boolean isConverted(ItemStack item, JavaPlugin plugin) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "converted");

        Boolean value = data.get(key, PersistentDataType.BOOLEAN);
        return Boolean.TRUE.equals(value);
    }

}