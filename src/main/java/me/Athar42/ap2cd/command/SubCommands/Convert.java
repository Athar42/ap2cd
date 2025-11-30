package me.Athar42.ap2cd.command.SubCommands;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import me.Athar42.ap2cd.AudioPlayer2CustomDiscs;
import me.Athar42.ap2cd.utils.AP2CDUtils;
import me.Athar42.ap2cd.utils.TypeChecker;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.io.File;
import java.util.UUID;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Convert extends CommandAPICommand {
    AudioPlayer2CustomDiscs plugin = AudioPlayer2CustomDiscs.getInstance();

	public Convert(AudioPlayer2CustomDiscs plugin) {
		super("convert");

		this.withFullDescription(NamedTextColor.GRAY + "Convert from AudioPlayer to CustomDiscs format.");
		this.withUsage("/ap2cd convert");
		//this.withPermission("audioplayer2customdiscs.convert");

		this.executesPlayer(this::onCommandPlayer);
		this.executesConsole(this::onCommandConsole);
	}

    private int onCommandPlayer(Player player, CommandArguments arguments) {
        ItemStack item = player.getInventory().getItemInMainHand();
        boolean resultIsMusicDisc = TypeChecker.isMusicDisc(item);
        boolean resultIsHorn = TypeChecker.isGoatHornPlayer(player);
        boolean resultIsHead = TypeChecker.isHeadPlayer(player);

        if (!resultIsMusicDisc && !resultIsHorn && !resultIsHead) {
            player.sendMessage(Component.text("AP2CD - Not holding a music disc, goat horn or player head!"));
            return 0;
        }

        PersistentDataContainer itemPDCdata = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey convertedKeyRetrieval = new NamespacedKey(plugin, "converted");

        Boolean isConverted = itemPDCdata.get(convertedKeyRetrieval, PersistentDataType.BOOLEAN);
        if (isConverted != null && isConverted) {
            player.sendMessage(Component.text("AP2CD - Item is already converted."));
            return 0;
        }

        net.minecraft.world.item.ItemStack itemCopyAsNMS = CraftItemStack.asNMSCopy(item);

        if (!itemCopyAsNMS.has(DataComponents.CUSTOM_DATA)) {
            if (isDebugEnabled()) player.sendMessage(Component.text("AP2CD-DEBUG-038 - This item has no 'minecraft:custom_data' - Not converting."));
            return 0;
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
                    if (isDebugEnabled()) player.sendMessage(Component.text("AP2CD-DEBUG-039 - The 'CustomSound' format is invalid - Not converting."));
                    return 0;
                }
            } else {
                if (isDebugEnabled()) player.sendMessage(Component.text("AP2CD-DEBUG-040 - The 'CustomSound' tag exists but is not an IntArrayTag - Not converting."));
                return 0;
            }

        } else if (tagCompoundRetrieval.contains("audioplayer")) {
            Tag audioplayerTagRetrieval = tagCompoundRetrieval.get("audioplayer");
            if (audioplayerTagRetrieval instanceof StringTag stringTag) {
                String json = stringTag.toString();
                String extractedUUIDString = extractUUIDFromAudioPlayer(json);
                if (extractedUUIDString != null) {
                    try {
                        uuid = UUID.fromString(extractedUUIDString);
                    } catch (IllegalArgumentException ex) {
                        if (isDebugEnabled()) player.sendMessage(Component.text("AP2CD-DEBUG-041 - Invalid UUID format from audioplayer JSON."));
                        return 0;
                    }
                } else {
                    if (isDebugEnabled()) player.sendMessage(Component.text("AP2CD-DEBUG-042 - Could not extract UUID from audioplayer JSON."));
                    return 0;
                }
            } else {
                if (isDebugEnabled()) player.sendMessage(Component.text("AP2CD-DEBUG-043 - 'audioplayer' tag exists but is not a StringTag."));
                return 0;
            }
        } else {
            if (isDebugEnabled()) player.sendMessage(Component.text("AP2CD-DEBUG-044 - Item has no 'CustomSound' or 'audioplayer' value - Not converting."));
            return 0;
        }

        Tag tagSoundRangeRetrieval = customDataRetrieval.copyTag().get("CustomSoundRange");
        Float retrievedRangeValue = null;

        if (tagSoundRangeRetrieval instanceof FloatTag floatTag) {
            retrievedRangeValue = floatTag.floatValue();
        } else {
            if (isDebugEnabled()) player.sendMessage(Component.text("AP2CD-DEBUG-045 - This item has no 'CustomSoundRange' value."));
        }

        File audioPlayerFolder = new File(Bukkit.getWorldContainer(), "plugins/CustomDiscs/musicdata");
        String convertedFilename = AP2CDUtils.findSoundFile(audioPlayerFolder, uuid);

        if (convertedFilename == null) {
            player.sendMessage(Component.text("AP2CD - ERROR : No matching sound file found for UUID " + uuid + "."));
            return 0;
        }

        if (resultIsMusicDisc) {
            setDiscMeta(player, true, convertedFilename, retrievedRangeValue);
        } else if (resultIsHorn) {
            setHornMeta(player, true, convertedFilename, retrievedRangeValue);
        } else {
            setHeadMeta(player, true, convertedFilename, retrievedRangeValue);
        }
        player.sendMessage(Component.text("AP2CD - Item has been converted!"));
        return 1;
    }

    public boolean convertItemHorn(Player player, ItemStack item, boolean isMainOffHand) {
        boolean resultIsHorn = TypeChecker.isGoatHornPlayer(player);
        if (!resultIsHorn) {
            player.sendMessage(Component.text("AP2CD - Not holding a goat horn!"));
            return false;
        }

        net.minecraft.world.item.ItemStack itemCopyAsNMS = CraftItemStack.asNMSCopy(item);

        if (!itemCopyAsNMS.has(DataComponents.CUSTOM_DATA)) {
            if (isDebugEnabled()) player.sendMessage(Component.text("AP2CD-DEBUG-046 - This item has no 'minecraft:custom_data' - Not converting."));
            return false;
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
                    if (isDebugEnabled()) player.sendMessage(Component.text("AP2CD-DEBUG-047 - The 'CustomSound' format is invalid - Not converting."));
                    return false;
                }
            } else {
                if (isDebugEnabled()) player.sendMessage(Component.text("AP2CD-DEBUG-048 - The 'CustomSound' tag exists but is not an IntArrayTag - Not converting."));
                return false;
            }
        } else if (tagCompoundRetrieval.contains("audioplayer")) {
            Tag audioplayerTagRetrieval = tagCompoundRetrieval.get("audioplayer");
            if (audioplayerTagRetrieval instanceof StringTag stringTag) {
                String json = stringTag.toString();
                String extractedUUIDString = extractUUIDFromAudioPlayer(json);
                if (extractedUUIDString != null) {
                    try {
                        uuid = UUID.fromString(extractedUUIDString);
                    } catch (IllegalArgumentException ex) {
                        if (isDebugEnabled()) player.sendMessage(Component.text("AP2CD-DEBUG-049 - Invalid UUID format from audioplayer JSON."));
                        return false;
                    }
                } else {
                    if (isDebugEnabled()) player.sendMessage(Component.text("AP2CD-DEBUG-050 - Could not extract UUID from audioplayer JSON."));
                    return false;
                }
            } else {
                if (isDebugEnabled()) player.sendMessage(Component.text("AP2CD-DEBUG-051 - 'audioplayer' tag exists but is not a StringTag."));
                return false;
            }
        } else {
            if (isDebugEnabled()) player.sendMessage(Component.text("AP2CD-DEBUG-052 - Item has no 'CustomSound' or 'audioplayer' value - Not converting."));
            return false;
        }

        Tag tagSoundRangeRetrieval = customDataRetrieval.copyTag().get("CustomSoundRange");
        Float retrievedRangeValue = null;

        if (tagSoundRangeRetrieval instanceof FloatTag floatTag) {
            retrievedRangeValue = floatTag.floatValue();
        } else {
            if (isDebugEnabled()) player.sendMessage(Component.text("AP2CD-DEBUG-053 - This item has no 'CustomSoundRange' value."));
        }

        File audioPlayerFolder = new File(Bukkit.getWorldContainer(), "plugins/CustomDiscs/musicdata");
        String convertedFilename = AP2CDUtils.findSoundFile(audioPlayerFolder, uuid);

        if (convertedFilename == null) {
            player.sendMessage(Component.text("AP2CD - ERROR : No matching sound file found for UUID " + uuid + "."));
            return false;
        }

        setHornMeta(player, isMainOffHand, convertedFilename, retrievedRangeValue);
        player.sendMessage(Component.text("AP2CD - Item has been converted!"));
        return true;
    }

    public void setDiscMeta(Player player, boolean isMainOffHand, String convertedFilename, Float retrievedRangeValue) {
        ItemStack discItemStack;
        if (isMainOffHand) {
            discItemStack = new ItemStack(player.getInventory().getItemInMainHand());
        } else {
            discItemStack = new ItemStack(player.getInventory().getItemInOffHand());
        }
        discItemStack.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.JUKEBOX_PLAYABLE).build());
        ItemMeta discMeta = discItemStack.getItemMeta();
        @Nullable List<Component> itemLore = new ArrayList<>();
        final TextComponent customLoreSong = Component.text().decoration(TextDecoration.ITALIC, false).content("AudioPlayer converted disc").color(NamedTextColor.GRAY).build();
        itemLore.add(customLoreSong);
        discMeta.lore(itemLore);

        PersistentDataContainer discPDCdata = discMeta.getPersistentDataContainer();
        discPDCdata.set(new NamespacedKey("customdiscs", "customdisc"), PersistentDataType.STRING, convertedFilename);
        if (retrievedRangeValue != null) {
            discPDCdata.set(new NamespacedKey("customdiscs", "range"), PersistentDataType.FLOAT, retrievedRangeValue);
        }
        discPDCdata.set(new NamespacedKey(plugin, "converted"), PersistentDataType.BOOLEAN, true);
        if (isMainOffHand) {
            player.getInventory().getItemInMainHand().setItemMeta(discMeta);
        } else {
            player.getInventory().getItemInOffHand().setItemMeta(discMeta);
        }
    }

    public void setHornMeta(Player player, boolean isMainOffHand, String convertedFilename, Float retrievedRangeValue) {
        ItemStack hornItemStack;
        if (isMainOffHand) {
            hornItemStack = new ItemStack(player.getInventory().getItemInMainHand());
        } else {
            hornItemStack = new ItemStack(player.getInventory().getItemInOffHand());
        }
        ItemMeta hornMeta = hornItemStack.getItemMeta();
        PersistentDataContainer hornPDCdata = hornMeta.getPersistentDataContainer();
        hornPDCdata.set(new NamespacedKey("customdiscs", "customhorn"), PersistentDataType.STRING, convertedFilename);
        hornPDCdata.set(new NamespacedKey("customdiscs", "goat_horn_cooldown"), PersistentDataType.INTEGER, 20);
        if (retrievedRangeValue != null) {
            hornPDCdata.set(new NamespacedKey("customdiscs", "range"), PersistentDataType.FLOAT, retrievedRangeValue);
        }
        hornPDCdata.set(new NamespacedKey(plugin, "converted"), PersistentDataType.BOOLEAN, true);
        if (isMainOffHand) {
            player.getInventory().getItemInMainHand().setItemMeta(hornMeta);
        } else {
            player.getInventory().getItemInOffHand().setItemMeta(hornMeta);
        }
    }

    public void setHeadMeta(Player player, boolean isMainOffHand, String convertedFilename, Float retrievedRangeValue) {
        final Component headCustomLore = Component.text().decoration(TextDecoration.ITALIC, false).content("AudioPlayer converted head").color(NamedTextColor.GRAY).build();
        String serializedComponent = GsonComponentSerializer.gson().serialize(headCustomLore);

        ItemStack headItemStack;
        if (isMainOffHand) {
            headItemStack = new ItemStack(player.getInventory().getItemInMainHand());
        } else {
            headItemStack = new ItemStack(player.getInventory().getItemInOffHand());
        }
        ItemMeta headMeta = headItemStack.getItemMeta();
        @Nullable List<Component> itemLore = new ArrayList<>();
        final TextComponent customLoreSong = Component.text().decoration(TextDecoration.ITALIC, false).content("AudioPlayer converted head").color(NamedTextColor.GRAY).build();
        itemLore.add(customLoreSong);
        headMeta.lore(itemLore);

        PersistentDataContainer headPDCdata = headMeta.getPersistentDataContainer();
        headPDCdata.set(new NamespacedKey("customdiscs", "customhead"), PersistentDataType.STRING, convertedFilename);
        headPDCdata.set(new NamespacedKey("customdiscs", "headlore"), PersistentDataType.STRING, serializedComponent);
        if (retrievedRangeValue != null) {
            headPDCdata.set(new NamespacedKey("customdiscs", "range"), PersistentDataType.FLOAT, retrievedRangeValue);
        }
        headPDCdata.set(new NamespacedKey(plugin, "converted"), PersistentDataType.BOOLEAN, true);
        if (isMainOffHand) {
            player.getInventory().getItemInMainHand().setItemMeta(headMeta);
        } else {
            player.getInventory().getItemInOffHand().setItemMeta(headMeta);
        }
    }

    public static String extractUUIDFromAudioPlayer(String json) {
        Pattern patternLookup = Pattern.compile("\"id\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");
        Matcher matcherLookup = patternLookup.matcher(json);
        return matcherLookup.find() ? matcherLookup.group(1) : null;
    }

	private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
		executor.sendMessage(NamedTextColor.RED + "AP2CD - Only players can use this command : '"+arguments+"'!");
		return 1;
	}

    private boolean isDebugEnabled() { return AudioPlayer2CustomDiscs.getDebugMode(); }

}