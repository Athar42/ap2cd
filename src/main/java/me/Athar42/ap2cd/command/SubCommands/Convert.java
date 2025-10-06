package me.Athar42.ap2cd.command.SubCommands;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import me.Athar42.ap2cd.AudioPlayer2CustomDiscs;
import me.Athar42.ap2cd.utils.TypeChecker;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.minecraft.nbt.Tag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.UUID;

import java.util.*;

public class Convert extends CommandAPICommand {
	private final AudioPlayer2CustomDiscs plugin;
	
	public Convert(AudioPlayer2CustomDiscs plugin) {
		super("convert");
		this.plugin = plugin;
		
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

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        if (!nmsItem.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
            player.sendMessage(Component.text("AP2CD - This item has no 'minecraft:custom_data' - Not converting."));
            return 0;
        }

        CustomData data = nmsItem.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        Tag tagSound = data.copyTag().get("CustomSound");

        if (!(tagSound instanceof IntArrayTag intArrayTag)) {
            player.sendMessage(Component.text("AP2CD -  item has no 'CustomSound' value - Not converting."));
            return 0;
        }

        Tag tagSoundRange = data.copyTag().get("CustomSoundRange");
        Float retrievedRangeValue = null;

        if (tagSoundRange instanceof FloatTag floatTag) {
            retrievedRangeValue = floatTag.floatValue();
        } else {
            player.sendMessage(Component.text("AP2CD - (info) This item has no 'CustomSoundRange' value."));
        }

        int[] ints = intArrayTag.getAsIntArray();
        if (ints.length != 4) {
            player.sendMessage(Component.text("AP2CD - The 'CustomSound' format is invalid - Not converting."));
            return 0;
        }

        // Convert the IntArray to UUID (so we can get the filename from it)
        long msb = ((long) ints[0] << 32) | (ints[1] & 0xFFFFFFFFL);
        long lsb = ((long) ints[2] << 32) | (ints[3] & 0xFFFFFFFFL);
        UUID uuid = new UUID(msb, lsb);

        String convertedFilename = uuid + ".mp3";

		if (resultIsMusicDisc) {
			ItemStack disc = new ItemStack(player.getInventory().getItemInMainHand());
			disc.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.JUKEBOX_PLAYABLE).build());
			ItemMeta meta = disc.getItemMeta();
			@Nullable List<Component> itemLore = new ArrayList<>();
			final TextComponent customLoreSong = Component.text().decoration(TextDecoration.ITALIC, false).content("AudioPlayer converted disc").color(NamedTextColor.GRAY).build();
			itemLore.add(customLoreSong);
			meta.lore(itemLore);

			PersistentDataContainer dataDisc = meta.getPersistentDataContainer();
            dataDisc.set(new NamespacedKey("customdiscs", "customdisc"), PersistentDataType.STRING, convertedFilename);
            if (retrievedRangeValue != null) {
                dataDisc.set(new NamespacedKey("customdiscs", "range"), PersistentDataType.FLOAT, retrievedRangeValue);
            }
            dataDisc.set(new NamespacedKey(this.plugin, "converted"), PersistentDataType.BOOLEAN, true);
			player.getInventory().getItemInMainHand().setItemMeta(meta);
		} else if (resultIsHorn) {
			ItemStack disc = new ItemStack(player.getInventory().getItemInMainHand());
			ItemMeta meta = disc.getItemMeta();
			PersistentDataContainer dataHorn = meta.getPersistentDataContainer();
            dataHorn.set(new NamespacedKey("customdiscs", "customhorn"), PersistentDataType.STRING, convertedFilename);
            dataHorn.set(new NamespacedKey("customdiscs", "goat_horn_cooldown"), PersistentDataType.INTEGER, 20);
            if (retrievedRangeValue != null) {
                dataHorn.set(new NamespacedKey("customdiscs", "range"), PersistentDataType.FLOAT, retrievedRangeValue);
            }
            dataHorn.set(new NamespacedKey(this.plugin, "converted"), PersistentDataType.BOOLEAN, true);
			player.getInventory().getItemInMainHand().setItemMeta(meta);
		} else {
			final Component customLoreHead = Component.text().decoration(TextDecoration.ITALIC, false).content("AudioPlayer converted head").color(NamedTextColor.GRAY).build();
			String serialized = GsonComponentSerializer.gson().serialize(customLoreHead);

			ItemStack disc = new ItemStack(player.getInventory().getItemInMainHand());
			ItemMeta meta = disc.getItemMeta();
			@Nullable List<Component> itemLore = new ArrayList<>();
			final TextComponent customLoreSong = Component.text().decoration(TextDecoration.ITALIC, false).content("AudioPlayer converted head").color(NamedTextColor.GRAY).build();
			itemLore.add(customLoreSong);
			meta.lore(itemLore);

			PersistentDataContainer dataHead = meta.getPersistentDataContainer();
            dataHead.set(new NamespacedKey("customdiscs", "customhead"), PersistentDataType.STRING, convertedFilename);
            dataHead.set(new NamespacedKey("customdiscs", "headlore"), PersistentDataType.STRING, serialized);
            if (retrievedRangeValue != null) {
                dataHead.set(new NamespacedKey("customdiscs", "range"), PersistentDataType.FLOAT, retrievedRangeValue);
            }
            dataHead.set(new NamespacedKey(this.plugin, "converted"), PersistentDataType.BOOLEAN, true);
			player.getInventory().getItemInMainHand().setItemMeta(meta);
		}
        player.sendMessage(Component.text("AP2CD - Item has been converted!"));
		return 1;
	}
	
	private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
		executor.sendMessage(NamedTextColor.RED + "AP2CD - Only players can use this command : '"+arguments+"'!");
		return 1;
	}

}