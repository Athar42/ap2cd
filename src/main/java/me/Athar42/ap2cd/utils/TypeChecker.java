package me.Athar42.ap2cd.utils;

import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.Set;

public class TypeChecker {

    private static final Set<Material> HEAD_LIST_MATERIALS = EnumSet.of(
            Material.PLAYER_HEAD,
            Material.WITHER_SKELETON_SKULL,
            Material.SKELETON_SKULL,
            Material.ZOMBIE_HEAD,
            Material.CREEPER_HEAD,
            Material.PIGLIN_HEAD,
            Material.DRAGON_HEAD
    );

    private static final Set<Material> HEAD_WALL_LIST_MATERIALS = EnumSet.of(
            Material.PLAYER_WALL_HEAD,
            Material.WITHER_SKELETON_WALL_SKULL,
            Material.SKELETON_WALL_SKULL,
            Material.ZOMBIE_WALL_HEAD,
            Material.CREEPER_WALL_HEAD,
            Material.PIGLIN_WALL_HEAD,
            Material.DRAGON_WALL_HEAD
    );

    public static boolean isMusicDisc(ItemStack item) {
        return MaterialTags.MUSIC_DISCS.isTagged(item.getType());
    }

    public static boolean isGoatHornPlayer(Player p) {
        return p.getInventory().getItemInMainHand().getType().equals(Material.GOAT_HORN) || p.getInventory().getItemInOffHand().getType().equals(Material.GOAT_HORN);
    }

    public static boolean isHead(Material material) {
        return HEAD_LIST_MATERIALS.contains(material);
    }

    public static boolean isWallHead(Material material) {
        return HEAD_WALL_LIST_MATERIALS.contains(material);
    }

    public static boolean isHeadPlayer(Player p) {
        return isHead(p.getInventory().getItemInMainHand().getType()) || isHead(p.getInventory().getItemInOffHand().getType());
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