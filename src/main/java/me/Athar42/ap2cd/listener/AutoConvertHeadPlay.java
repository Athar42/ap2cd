package me.Athar42.ap2cd.listener;

import me.Athar42.ap2cd.AudioPlayer2CustomDiscs;
import me.Athar42.ap2cd.command.SubCommands.Convert;
import me.Athar42.ap2cd.utils.AP2CDUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class AutoConvertHeadPlay implements Listener {
    AudioPlayer2CustomDiscs plugin = AudioPlayer2CustomDiscs.getInstance();
    private final Logger pluginLogger = plugin.getLogger();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNotePlay(NotePlayEvent event) throws IOException {
        if (AudioPlayer2CustomDiscs.getReverseAutomaticConvertMode()) return;

        Block noteBlock = event.getBlock();

        if (noteBlock.getType() != Material.NOTE_BLOCK) return;

        Block headBlock = noteBlock.getRelative(BlockFace.UP);
        if (headBlock.getType() != Material.PLAYER_HEAD) return;

        Skull headSkull = (Skull) headBlock.getState();
        PersistentDataContainer headPDCdata = headSkull.getPersistentDataContainer();

        NamespacedKey convertedPDCKey = new NamespacedKey(plugin, "converted");
        Boolean isPDCConverted = headPDCdata.get(convertedPDCKey, PersistentDataType.BOOLEAN);
        if (isPDCConverted != null && isPDCConverted) {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-008 - Head Block PDC already converted.");
            return;
        }

        ItemStack skullItemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skullItemStack.getItemMeta();
        if (headSkull.getProfile() != null && headSkull.getProfile().name() != null && !headSkull.getProfile().name().isEmpty()) {
            String playerName = headSkull.getProfile().name();
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
        } else if (headSkull.getProfile() != null && headSkull.getProfile().uuid() != null) {
            UUID playerUUID = headSkull.getProfile().uuid();
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerUUID));
        } else {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-009 - Skull profile is invalid or missing a name/UUID.");
        }
        skullItemStack.setItemMeta(skullMeta);

        if (isDebugEnabled()) debugPrintBlockEntityNBT(headBlock, pluginLogger);
        CompoundTag headTagCompound = getBlockEntityNBT(headBlock);

        if (headTagCompound == null || !headTagCompound.contains("components")) {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-010 - No components found in block entity.");
            return;
        }

        Optional<CompoundTag> headComponentsOptional = headTagCompound.getCompound("components");
        if (headComponentsOptional.isEmpty()) {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-011 - No components found in block entity.");
            return;
        }
        CompoundTag headComponents = headComponentsOptional.get();

        Optional<CompoundTag> headCustomDataOptional = headComponents.getCompound("minecraft:custom_data");
        if (headCustomDataOptional.isEmpty()) {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-012 - No 'minecraft:custom_data' component found.");
            return;
        }
        CompoundTag headCustomData = headCustomDataOptional.get();

        boolean headHasCustomSound = headCustomData.contains("CustomSound");
        boolean headHasAudioPlayer = headCustomData.contains("audioplayer");

        if (!headHasCustomSound && !headHasAudioPlayer) {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-013 - Skull block has no CustomSound nor audioplayer.");
            return;
        }

        UUID uuid = null;

        if (headHasCustomSound) {
            Optional<int[]> customSoundIntOptional = headCustomData.getIntArray("CustomSound");
            int[] customSoundInts = customSoundIntOptional.orElse(null);
            if (customSoundInts == null || customSoundInts.length != 4) {
                if (isDebugEnabled()) pluginLogger.warning("AP2CD-DEBUG-014 - Invalid CustomSound format.");
                return;
            }

            long msb = ((long) customSoundInts[0] << 32) | (customSoundInts[1] & 0xFFFFFFFFL);
            long lsb = ((long) customSoundInts[2] << 32) | (customSoundInts[3] & 0xFFFFFFFFL);
            uuid = new UUID(msb, lsb);
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-015 - CustomSound UUID: " + uuid + ".");
        } else {
            Optional<String> audioplayerJsonOptional = headCustomData.getString("audioplayer");
            String audioplayerJson = audioplayerJsonOptional.orElse(null);
            if (audioplayerJson != null) {
                if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-016A - Found audioplayer JSON: " + audioplayerJson + ".");
                String extractedUUIDString = Convert.extractUUIDFromAudioPlayer(audioplayerJson);
                if (extractedUUIDString != null) {
                    try {
                        uuid = UUID.fromString(extractedUUIDString);
                    } catch (IllegalArgumentException ex) {
                        if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-017A - Invalid UUID format from audioplayer JSON.");
                        return;
                    }
                } else {
                    if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-017B - Could not extract UUID from audioplayer JSON.");
                    return;
                }
            } else {
                if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-016B - 'audioplayer' tag exists but is not a StringTag.");
                return;
            }
        }

        Optional<Float> customSoundRangeTagOptional = headCustomData.getFloat("CustomSoundRange");

        Float retrievedRangeValue = null;
        if (customSoundRangeTagOptional.isPresent()) {
            retrievedRangeValue = customSoundRangeTagOptional.get();
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-018A - Found CustomSoundRange: " + retrievedRangeValue + ".");
        } else {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-018B - This item has no 'CustomSoundRange' value.");
        }

        File audioPlayerFolder = new File(Bukkit.getWorldContainer(), "plugins/CustomDiscs/musicdata");
        String convertedFilename = AP2CDUtils.findSoundFile(audioPlayerFolder, uuid);

        if (convertedFilename == null) {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-019 - Error: No matching sound file found for UUID " + uuid + ".");
            return;
        }

        setHeadMeta(headBlock, convertedFilename, retrievedRangeValue);
        if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-020 - Item has been converted!");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHeadPlace(BlockPlaceEvent event) {
        if (AudioPlayer2CustomDiscs.getReverseAutomaticConvertMode()) return;

        ItemStack item = event.getItemInHand();

        if (item.getType() != Material.PLAYER_HEAD) return;
        if (!(item.getItemMeta() instanceof SkullMeta meta)) return;

        net.minecraft.world.item.ItemStack itemCopyAsNMS = CraftItemStack.asNMSCopy(item);

        if (!itemCopyAsNMS.has(DataComponents.CUSTOM_DATA)) {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-021 - This item has no 'minecraft:custom_data' - Not converting.");
            return;
        }

        if (!itemCopyAsNMS.has(DataComponents.BLOCK_ENTITY_DATA)) {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-057 - No BLOCK_ENTITY_DATA");
        } else {
            TypedEntityData<BlockEntityType<?>> dataIsConverted = itemCopyAsNMS.get(DataComponents.BLOCK_ENTITY_DATA);
            CompoundTag tagCompoundIsConverted = dataIsConverted.copyTagWithBlockEntityId();
            Optional<CompoundTag> pdcRetrievalOptional = tagCompoundIsConverted.getCompound("PublicBukkitValues");
            if (pdcRetrievalOptional.isEmpty()) {
                if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-058 - No PublicBukkitValues inside this BLOCK_ENTITY_DATA.");
            } else {
                CompoundTag publicBukkitValues = pdcRetrievalOptional.get();
                String pdcConvertedKey = new NamespacedKey(plugin, "converted").toString();
                if (publicBukkitValues.contains(pdcConvertedKey)) {
                    Optional<Boolean> convertedRetrievalOptional = publicBukkitValues.getBoolean(pdcConvertedKey);
                    boolean converted = convertedRetrievalOptional.orElse(false);

                    if (converted) {
                        if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-056 - Head Block PDC already converted.");
                        return;
                    }
                }
            }
        }

        CustomData customDataRetrieval = itemCopyAsNMS.get(DataComponents.CUSTOM_DATA);
        CompoundTag tagCompoundRetrieval = customDataRetrieval.copyTag();

        UUID uuid = null;

        if (tagCompoundRetrieval.contains("CustomSound")) {
            Tag tagSoundRetrieval = tagCompoundRetrieval.get("CustomSound");
            if (tagSoundRetrieval instanceof IntArrayTag intArrayTag) {
                int[] ints = intArrayTag.getAsIntArray();
                if (ints.length == 4) {
                    long msb = ((long) ints[0] << 32) | (ints[1] & 0xFFFFFFFFL);
                    long lsb = ((long) ints[2] << 32) | (ints[3] & 0xFFFFFFFFL);
                    uuid = new UUID(msb, lsb);
                } else {
                    if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-022 - The 'CustomSound' format is invalid - Not converting.");
                    return;
                }
            } else {
                if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-023 - The 'CustomSound' tag exists but is not an IntArrayTag - Not converting.");
                return;
            }
        } else if (tagCompoundRetrieval.contains("audioplayer")) {
            Tag audioplayerTagRetrieval = tagCompoundRetrieval.get("audioplayer");
            if (audioplayerTagRetrieval instanceof StringTag stringTag) {
                String json = stringTag.toString();
                String extractedUUIDString = Convert.extractUUIDFromAudioPlayer(json);
                if (extractedUUIDString != null) {
                    try {
                        uuid = UUID.fromString(extractedUUIDString);
                    } catch (IllegalArgumentException ex) {
                        if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-024A - Invalid UUID format from audioplayer JSON.");
                        return;
                    }
                } else {
                    if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-024B - Could not extract UUID from audioplayer JSON.");
                    return;
                }
            } else {
                if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-025 - 'audioplayer' tag exists but is not a StringTag.");
                return;
            }
        } else {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-026 - Item has no 'CustomSound' or 'audioplayer' value - Not converting.");
            return;
        }

        Tag tagSoundRangeRetrieval = customDataRetrieval.copyTag().get("CustomSoundRange");
        Float retrievedRangeValue;

        if (tagSoundRangeRetrieval instanceof FloatTag floatTag) {
            retrievedRangeValue = floatTag.floatValue();
        } else {
            retrievedRangeValue = null;
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-027 - This item has no 'CustomSoundRange' value.");
        }

        File audioPlayerFolder = new File(Bukkit.getWorldContainer(), "plugins/CustomDiscs/musicdata");
        String convertedFilename = AP2CDUtils.findSoundFile(audioPlayerFolder, uuid);

        if (convertedFilename == null) {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-028 - Error: No matching sound file found for UUID " + uuid + ".");
            return;
        }

        Block block = event.getBlockPlaced();
        Bukkit.getRegionScheduler().runDelayed(plugin, block.getLocation(), task -> {
            if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD) return;

            setHeadMeta(block, convertedFilename, retrievedRangeValue);

            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-029 - Item has been converted!");
        }, 1L);
    }

    public static CompoundTag getBlockEntityNBT(Block block) {
        Level level = ((CraftWorld) block.getWorld()).getHandle();
        BlockPos position = new BlockPos(block.getX(), block.getY(), block.getZ());

        BlockEntity blockEntityData = level.getBlockEntity(position);
        if (blockEntityData == null) return null;

        RegistryAccess registries = level.registryAccess();

        return blockEntityData.saveWithFullMetadata(registries);
    }

    public void setHeadMeta(Block skullBlock, String convertedFilename, Float retrievedRangeValue) {
        if (!(skullBlock.getState() instanceof Skull skull)) {
            if (isDebugEnabled()) pluginLogger.info("AP2CD-DEBUG-030 - The block is not a skull, cannot set metadata.");
            return;
        }

        PersistentDataContainer dataHead = skull.getPersistentDataContainer();

        dataHead.set(new NamespacedKey("customdiscs", "customhead"), PersistentDataType.STRING, convertedFilename);

        final Component customLoreHead = Component.text().decoration(TextDecoration.ITALIC, false).content("AudioPlayer converted head").color(NamedTextColor.GRAY).build();
        String serialized = GsonComponentSerializer.gson().serialize(customLoreHead);
        dataHead.set(new NamespacedKey("customdiscs", "headlore"), PersistentDataType.STRING, serialized);

        if (retrievedRangeValue != null) {
            dataHead.set(new NamespacedKey("customdiscs", "range"), PersistentDataType.FLOAT, retrievedRangeValue);
        }

        dataHead.set(new NamespacedKey(plugin, "converted"), PersistentDataType.BOOLEAN, true);

        skull.update(true, true);
    }

    public static void debugPrintBlockEntityNBT(Block block, Logger logger) {
        logger.info("=== DEBUG BlockEntity NBT START ===");
        logger.info("Block at " + block.getX() + " " + block.getY() + " " + block.getZ());
        logger.info("Type: " + block.getType());

        Level level = ((CraftWorld) block.getWorld()).getHandle();
        BlockPos position = new BlockPos(block.getX(), block.getY(), block.getZ());

        BlockEntity blockEntityData = level.getBlockEntity(position);
        if (blockEntityData == null) {
            logger.info("BlockEntity = null");
            logger.info("=== DEBUG BE NBT END ===");
            return;
        }

        RegistryAccess registries = level.registryAccess();

        CompoundTag fullTag = blockEntityData.saveWithFullMetadata(registries);
        logger.info("saveWithFullMetadata: " + fullTag);

        CompoundTag updateTag = blockEntityData.getUpdateTag(registries);
        logger.info("getUpdateTag: " + updateTag);

        logger.info("=== DEBUG BE NBT END ===");
    }

    private boolean isDebugEnabled() { return AudioPlayer2CustomDiscs.getDebugMode(); }

}