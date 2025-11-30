package me.Athar42.ap2cd.listener;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import me.Athar42.ap2cd.AudioPlayer2CustomDiscs;
import me.Athar42.ap2cd.command.SubCommands.Convert;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Jukebox;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class AutoConvertJukeBox implements Listener {
    AudioPlayer2CustomDiscs plugin = AudioPlayer2CustomDiscs.getInstance();
    private final Logger pluginLogger = plugin.getLogger();
    private final NamespacedKey convertedKeyRetrieval = new NamespacedKey(plugin, "converted");

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInsert(PlayerInteractEvent event) {

        if (AudioPlayer2CustomDiscs.getReverseAutomaticConvertMode()) return;

        if (event.getClickedBlock() == null ||
                event.getClickedBlock().getType() != Material.JUKEBOX) return;

        ItemStack discItemStack = event.getItem();
        if (discItemStack == null || !isVanillaMusicDisc(discItemStack)) return;

        convertIfNeeded(discItemStack);

        if (!event.getItem().hasData(DataComponentTypes.TOOLTIP_DISPLAY) || !event.getItem().getData(DataComponentTypes.TOOLTIP_DISPLAY).hiddenComponents().contains(DataComponentTypes.JUKEBOX_PLAYABLE)) {
            event.getItem().setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.JUKEBOX_PLAYABLE).build());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHopperMove(InventoryMoveItemEvent event) {

        if (AudioPlayer2CustomDiscs.getReverseAutomaticConvertMode()) return;

        if (!(event.getDestination().getHolder() instanceof Jukebox)) return;

        ItemStack discItemStack = event.getItem();
        if (!isVanillaMusicDisc(discItemStack)) return;

        convertIfNeeded(discItemStack);

        if (!event.getItem().hasData(DataComponentTypes.TOOLTIP_DISPLAY) || !event.getItem().getData(DataComponentTypes.TOOLTIP_DISPLAY).hiddenComponents().contains(DataComponentTypes.JUKEBOX_PLAYABLE)) {
            event.getItem().setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.JUKEBOX_PLAYABLE).build());
        }

        event.setItem(discItemStack);
    }

    private void convertIfNeeded(ItemStack disc) {

        ItemMeta discMeta = disc.getItemMeta();
        if (discMeta.getPersistentDataContainer().getOrDefault(convertedKeyRetrieval, PersistentDataType.BOOLEAN, false)) {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-031 - Disc has already been converted.");
            return;
        }

        net.minecraft.world.item.ItemStack itemCopyAsNMS = CraftItemStack.asNMSCopy(disc);
        if (!itemCopyAsNMS.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-032 - Disc has no 'minecraft:custom_data' - Not converting.");
            return;
        }

        CustomData customDataRetrieval = itemCopyAsNMS.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        CompoundTag tagCompoundRetrieval = customDataRetrieval.copyTag();

        UUID uuid = extractUUIDFromTag(tagCompoundRetrieval);
        if (uuid == null) {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-033 - No usable 'CustomSound' or 'audioplayer' found.");
            return;
        }

        Optional<Float> rangeRetrievalOptional = tagCompoundRetrieval.getFloat("CustomSoundRange");
        Float rangeRetrieved = rangeRetrievalOptional.orElse(null);

        File audioFolder = new File(Bukkit.getWorldContainer(), "plugins/CustomDiscs/musicdata");
        String filename = me.Athar42.ap2cd.utils.AP2CDUtils.findSoundFile(audioFolder, uuid);

        if (filename == null) {
            if (isDebugEnabled()) pluginLogger.warning("AP2CD-DEBUG-034 - No file found for UUID " + uuid);
            return;
        }

        applyDiscMeta(disc, filename, rangeRetrieved);

        if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-035 - Music disc converted!");
    }

    private UUID extractUUIDFromTag(CompoundTag tag) {
        if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-036A - UUID extraction process start.");
        if (tag.contains("CustomSound")) {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-036B - UUID extraction from 'CustomSound'.");
            Optional<int[]> customSoundRetrievedOptional = tag.getIntArray("CustomSound");
            int[] ints = customSoundRetrievedOptional.orElse(null);
            if (ints == null || ints.length != 4) return null;

            long msb = ((long) ints[0] << 32) | (ints[1] & 0xffffffffL);
            long lsb = ((long) ints[2] << 32) | (ints[3] & 0xffffffffL);

            UUID customsoundUUID = new UUID(msb, lsb);
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-036C - CustomSound UUID extracted: " + customsoundUUID + ".");
            return customsoundUUID;
        }

        if (tag.contains("audioplayer")) {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-036D - UUID extraction from 'audioplayer'.");
            Optional<String> audioplayerRetrievedJsonOptional = tag.getString("audioplayer");
            String json = audioplayerRetrievedJsonOptional.orElse(null);
            if (json == null) return null;

            String found = Convert.extractUUIDFromAudioPlayer(json);
            if (found == null) return null;

            try {
                UUID audioplayerUUID = UUID.fromString(found);
                if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-036E - audioplayer UUID extracted: " + audioplayerUUID + ".");
                return audioplayerUUID;
            } catch (IllegalArgumentException ignored) {
                if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-036F - audioplayer UUID failed JSON to String retrieval.");
                return null;
            }
        }

        if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-036G - UUID extraction process end.");
        return null;
    }

    private void applyDiscMeta(ItemStack disc, String filename, Float range) {
        if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-037 - Disc meta being applied.");
        ItemMeta discMeta = disc.getItemMeta();
        @Nullable List<Component> itemLore = new ArrayList<>();
        final TextComponent customLoreSong = Component.text().decoration(TextDecoration.ITALIC, false).content("AudioPlayer converted disc").color(NamedTextColor.GRAY).build();
        itemLore.add(customLoreSong);
        discMeta.lore(itemLore);

        PersistentDataContainer discPDCdata = discMeta.getPersistentDataContainer();

        discPDCdata.set(new NamespacedKey("customdiscs","customdisc"), PersistentDataType.STRING, filename);

        if (range != null) {
            discPDCdata.set(new NamespacedKey("customdiscs","range"), PersistentDataType.FLOAT, range);
        }

        discPDCdata.set(convertedKeyRetrieval, PersistentDataType.BOOLEAN, true);

        disc.setItemMeta(discMeta);
    }

    private boolean isVanillaMusicDisc(ItemStack item) {
        return item.getType().toString().startsWith("MUSIC_DISC");
    }

    private boolean isDebugEnabled() { return AudioPlayer2CustomDiscs.getDebugMode(); }

}