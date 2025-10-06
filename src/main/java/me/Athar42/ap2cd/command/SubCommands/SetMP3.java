package me.Athar42.ap2cd.command.SubCommands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import me.Athar42.ap2cd.AudioPlayer2CustomDiscs;
import me.Athar42.ap2cd.utils.TypeChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SetMP3 extends CommandAPICommand {
	private final AudioPlayer2CustomDiscs plugin;

	public SetMP3(AudioPlayer2CustomDiscs plugin) {
		super("setmp3");
		this.plugin = plugin;
		
		this.withFullDescription(NamedTextColor.GRAY + "Change the extension of the audio file to .mp3");
		this.withUsage("/ap2cd setmp3");
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

        if (!TypeChecker.isConverted(item, plugin)) {
            player.sendMessage(Component.text("AP2CD - This item hasn't been converted yet."));
            return 0;
        }

        if (resultIsMusicDisc) {
            ItemStack disc = new ItemStack(player.getInventory().getItemInMainHand());
            ItemMeta meta = disc.getItemMeta();
            PersistentDataContainer data = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey("customdiscs", "customdisc");
            String currentValue = data.get(key, PersistentDataType.STRING);
            if (currentValue != null) {
                String newValue = currentValue.replaceFirst("\\.[^.]+$", ".mp3");
                data.set(key, PersistentDataType.STRING, newValue);
                player.sendMessage(Component.text("AP2CD - New filename is: " + newValue));
            } else {
                player.sendMessage(Component.text("AP2CD - No customdisc value found to change."));
            }
            player.getInventory().getItemInMainHand().setItemMeta(meta);
        } else if (resultIsHorn) {
            ItemStack disc = new ItemStack(player.getInventory().getItemInMainHand());
            ItemMeta meta = disc.getItemMeta();
            PersistentDataContainer data = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey("customdiscs", "customhorn");
            String currentValue = data.get(key, PersistentDataType.STRING);
            if (currentValue != null) {
                String newValue = currentValue.replaceFirst("\\.[^.]+$", ".mp3");
                data.set(key, PersistentDataType.STRING, newValue);
                player.sendMessage(Component.text("AP2CD - New filename is: " + newValue));
            } else {
                player.sendMessage(Component.text("AP2CD - No customhorn value found to change."));
            }
            player.getInventory().getItemInMainHand().setItemMeta(meta);
        } else {
            ItemStack disc = new ItemStack(player.getInventory().getItemInMainHand());
            ItemMeta meta = disc.getItemMeta();
            PersistentDataContainer data = meta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey("customdiscs", "customhead");
            String currentValue = data.get(key, PersistentDataType.STRING);
            if (currentValue != null) {
                String newValue = currentValue.replaceFirst("\\.[^.]+$", ".mp3");
                data.set(key, PersistentDataType.STRING, newValue);
                player.sendMessage(Component.text("AP2CD - New filename is: " + newValue));
            } else {
                player.sendMessage(Component.text("AP2CD - No customhead value found to change."));
            }
            player.getInventory().getItemInMainHand().setItemMeta(meta);
        }
		return 1;
	}
	
	private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
		executor.sendMessage(NamedTextColor.RED + "AP2CD - Only players can use this command : '"+arguments+"'!");
		return 1;
	}

}