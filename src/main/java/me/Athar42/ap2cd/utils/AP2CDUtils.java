package me.Athar42.ap2cd.utils;
import me.Athar42.ap2cd.AudioPlayer2CustomDiscs;

import me.Athar42.ap2cd.command.SubCommands.Convert;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;

public class AP2CDUtils {
    static AudioPlayer2CustomDiscs plugin = AudioPlayer2CustomDiscs.getInstance();
    private static final Logger pluginLogger = plugin.getLogger();

    public static @Nullable String findSoundFile(File folder, UUID uuid) {
        for (String ext : new String[]{"flac", "mp3", "wav"}) {
            File f = new File(folder, uuid + "." + ext);
            if (f.isFile()) return f.getName();
        }
        return null;
    }

    public static boolean convertHornIfNeeded(ItemStack item, Player player, AudioPlayer2CustomDiscs plugin, boolean isMainOffHand) {
        if (item == null || !item.hasItemMeta()) return false;

        PersistentDataContainer itemPDCdata = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey convertedKeyRetrieval = new NamespacedKey(plugin, "converted");

        Boolean isConverted = itemPDCdata.get(convertedKeyRetrieval, PersistentDataType.BOOLEAN);
        if (isConverted != null && isConverted) {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-054 - Horn already converted!");
            return false;
        }

        return new Convert(plugin).convertItemHorn(player, item, isMainOffHand);
    }

    private static boolean isDebugEnabled() { return AudioPlayer2CustomDiscs.getDebugMode(); }

}